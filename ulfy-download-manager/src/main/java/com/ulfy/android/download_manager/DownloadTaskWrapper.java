package com.ulfy.android.download_manager;

import android.util.Log;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.task.DownloadTask;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

import static com.ulfy.android.download_manager.DownloadManagerConfig.defaultIdIfEmpty;

/**
 * 表示一个具体的下载任务（现在支持的任务状态变更回调：进行中->进度回调->速度回调，执行结束->执行完成
 *      包括下载任务的状态信息
 *      下载引擎的对接
 * 改类的名字不能是DownloadTask，因为Aria框架自动生成的代码中也有DownloadTask，这会引起冲突导致编译错误
 */
public class DownloadTaskWrapper<T extends DownloadTaskInfo> implements Serializable {
    private static final long serialVersionUID = -6410544290427834001L;
    private transient String TAG = getClass().getSimpleName();
    /*
        用于下载的真正数据（需要实现序列化接口）
     */
    private String downloadManagerId;           // 所属的下载管理器
    private final T downloadTaskInfo;
    /*
        任务的状态：已开始，未开始，未开始包括两种状态：
            1）未限制同时下载数量时表示暂停
            2）限制同时下载数量时不允许开始下载的任务表示等待中（默认任务的状态为等待中）
     */
    private transient boolean start = false;
    private transient boolean waiting = true;
    /*
        标记任务是否已经完成
     */
    private boolean complete = false;
    /*
        跟踪下载任务的状态：文件总长度、当前已经下载的长度、上次下载的长度（用于下载速度）、下载速度（单位B）
     */
    private long totalLength = 0, currentOffset = 0;
    private transient long lastOffset = 0, lastSpeed = 0, speed = 0;
    /*
        跟踪任务的状态变化，当状态变化后通过该变量记录。外部通过该状态栏判断是否对外发布更新事件，当发布了
        事件之后，重新设置为未更新状态
     */
    private transient boolean stateUpdateForPublish;

    /**
     * 构造方法
     */
    DownloadTaskWrapper(String downloadManagerId, T downloadTaskInfo) {
        this.downloadManagerId = defaultIdIfEmpty(downloadManagerId);
        this.downloadTaskInfo = downloadTaskInfo;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 框架内部操作方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 当外部发布了任务的更新状态后调用这里，用于重置更新状态
     */
    final synchronized void stateUpdatePublished() {
        stateUpdateForPublish = false;
    }

    /**
     * 内部状态是否发生了更新
     */
    final synchronized boolean isStateUpdated() {
        return stateUpdateForPublish;
    }

    /**
     * 开始任务
     * @return 该操作是否执行成功
     */
    final synchronized boolean start() {
        if (!start && !complete) {
            start = true; waiting = false;
            stateUpdateForPublish = true;
            startDownloadInner();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 停止任务，任务停止之后不一定是等待状态，如果存在其它的等待任务则其它的任务会开始
     * @return 该操作是否执行成功
     */
    final synchronized boolean stop() {
        if (!complete && start) {
            start = false;
            stateUpdateForPublish = true;
            stopDownloadInner();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 使暂停的任务处于等待中状态
     * @return 该操作是否执行成功
     */
    final synchronized boolean waiting() {
        if (!complete && !start) {
            waiting = true;
            stateUpdateForPublish = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 重启任务
     * @return 该操作是否执行成功
     */
    final synchronized boolean restart() {
        if (!complete) {
            start = true; waiting = false;
            stateUpdateForPublish = true;
            stopDownloadInner();
            startDownloadInner();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 销毁任务
     */
    final synchronized void destroy() {
        if (!complete) {
            cancelDownloadInner();
        }
        getTargetFile().delete();
        start = false; waiting = false; complete = false;
        totalLength = 0; currentOffset = 0;
        lastOffset = 0; lastSpeed = 0; speed = 0;
        stateUpdateForPublish = true;
    }

    /**
     * 获取任务唯一标识
     */
    final String provideUniquelyIdentifies() {
        return downloadTaskInfo.getUniquelyIdentifies();
    }

    /**
     * 当前任务是否还有效（用于用户手动删除文件的情况）
     *      对于下载中任务无论是否文件被移除都有效（移除以后下载进程会报错，然后执行重试，这样就又生成了新的文件）
     *      对于下载中的文件是分片为多个文件的，因为无法确定目标文件的具体路径，也就无法判断是否存在
     *      对于下载完成的任务如果被外部移除则说明该任务失效了
     */
    final boolean isValid() {
        return !complete || getTargetFile().exists();
    }

    /**
     * 获取目标文件的大小（只对已完成的任务有效）
     */
    final long getTargetFileSize() {
        return isValid() ? getTargetFile().length() : 0;
    }

    ///////////////////////////////////////////////////////////////////////////
    // 外部使用的方法（对外部公开的方法）
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取与任务关联的目标文件（该文件会根据当前任务是否完成而返回不同的路径）
     */
    public final File getTargetFile() {
        File dir = !complete ? DownloadManagerConfig.Config.getDownloadingDirectoryById(defaultIdIfEmpty(downloadManagerId)) :
                DownloadManagerConfig.Config.getDownloadedDirectoryById(defaultIdIfEmpty(downloadManagerId));
        return new File(dir, downloadTaskInfo.provideDownloadFileName());
    }

    /**
     * 获取下载任务内部的任务信息（通常是和业务关联的信息）
     */
    public final T getDownloadTaskInfo() {
        return downloadTaskInfo;
    }

    /**
     * 任务是否已经启动了
     */
    public final boolean isStart() {
        return start;
    }

    /**
     * 任务是否处于等待中
     */
    public final boolean isWaiting() {
        return waiting;
    }

    /**
     * 任务是否已经完成
     */
    public final boolean isComplete() {
        return complete;
    }

    /**
     * 获取文件总字节数量
     */
    public final long getTotalLength() {
        return totalLength;
    }

    /**
     * 获取已经下载的字节
     */
    public final long getCurrentOffset() {
        return currentOffset;
    }

    /**
     * 获取下载的百分比进度（0-100）
     * @return  返回百分比的进度
     */
    public final int getProgress() {
        if (totalLength == 0) {
            return 0;
        } else {
            return (int) (currentOffset * 100 / totalLength);
        }
    }

    /**
     * 获取当前下载速度
     */
    public final long getSpeed() {
        return speed;
    }

    /**
     * 获取当前任务的状态：字符串
     */
    public final String getStatusString() {
        if (complete) {
            return DownloadManagerConfig.Config.statusStringComplete;
        } else {
            if (start) {
                return DownloadManagerConfig.Config.statusStringStart;
            } else {
                // 有同时下载数量限制时就会使用等待中状态，否则直接使用已暂停状态
                if (DownloadManagerConfig.DownloadLimitConfig.getInstance(defaultIdIfEmpty(downloadManagerId)).getLimitCount() > 0) {
                    return waiting ? DownloadManagerConfig.Config.statusStringWaiting : DownloadManagerConfig.Config.statusStringPause;
                } else {
                    return DownloadManagerConfig.Config.statusStringPause;
                }
            }
        }
    }

    /*
    ------------------------------- 三方下载模块的相应包装 ---------------------------------
     */

    private transient Disposable speedProgressDetectDisposable;         // 用于跟踪网速探测进程
    private long taskId = 0;    // 0 表示任务没有创建（如果创建失败返回的是-1），为了能在应用被杀死后仍能跟踪任务，需要序列化该字段

    /*
        当任务从序列化回复后，可能执行启动、停止、删除等操作，但是这时候由于没有注册相应的回调不会调用
        因此要在这些方法中都进行注册
        这些注册并不会造成内存泄漏，因为在相应的回调里边会执行反注册操作
     */

    private void startDownloadInner() {
        Aria.download(this).register();
        taskId = Aria.download(this)
                .load(combineDownloadLink())                // 读取下载地址
                .setFilePath(generateDownloadFilePath())    // 设置文件保存的完整路径
                .create();                                  // 创建并启动下载
    }

    private void stopDownloadInner() {
        Aria.download(this).register();
        Aria.download(this).load(taskId).stop();
    }

    private void cancelDownloadInner() {
        Aria.download(this).register();
        Aria.download(this).load(taskId).cancel();
    }

    /*
        Aria会将同一个下载地址对应的多个任务关联起来，这样不利于任务逻辑的维护。通过将下载地址和下载文件名合并起来标识单个任务可以解决这个问题
     */
    private String combineDownloadLink() {
        String connector = downloadTaskInfo.provideDownloadFileLink().contains("?") ? "&" : "?";
        return String.format("%s%sfile=%s", downloadTaskInfo.provideDownloadFileLink(), connector, generateDownloadFilePath());
    }
    private String generateDownloadFilePath() {
        File file = new File(DownloadManagerConfig.Config
                .getDownloadingDirectoryById(defaultIdIfEmpty(downloadManagerId)), downloadTaskInfo.provideDownloadFileName());
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 开始探测下载速度、进度进程（这里的进程指的是一个过程，而不是操作系统的进程）
     */
    private void startDetectDownloadSpeedAndProgress() {
        if (speedProgressDetectDisposable == null) {
            speedProgressDetectDisposable = Observable.interval(1, TimeUnit.SECONDS).subscribe(x -> {
                speed = currentOffset - lastOffset;
                if (lastOffset == 0 || speed < 0) { // 计算网速（若currentOffset是从序列化恢复的，则首次计算网速会非常大，应该过滤掉）
                    speed = 0;
                }
                if (lastSpeed != speed) {
                    DownloadManager.getInstance(defaultIdIfEmpty(downloadManagerId))
                            .notifyItemSpeedChanged(this, lastSpeed, speed);
                    stateUpdateForPublish = true;
                    lastSpeed = speed;
                }
                if (lastOffset != currentOffset) {
                    DownloadManager.getInstance(defaultIdIfEmpty(downloadManagerId))
                            .notifyItemProgressChanged(this, lastOffset, currentOffset);
                    stateUpdateForPublish = true;
                    lastOffset = currentOffset;
                }
            });
        }
    }

    /**
     * 停止探测下载速度（停止探测后下载速度将会变为0）
     */
    private void stopDetectDownloadSpeedProgress() {
        if (speedProgressDetectDisposable != null) {
            speedProgressDetectDisposable.dispose();
            speedProgressDetectDisposable = null;
            speed = 0;
        }
    }

    /**
     * 当内部任务下载完毕后调用的回调，完成一些后续工作（移动文件位置、重新调整任务队列、执行相应的回调）
     */
    private synchronized void onDownloadCompletedInner() {
        complete = true;                    // 设置状态要放到迁移仓库的前面，否则这些状态不会被序列化到
        stateUpdateForPublish = true;
        // 将下载中文件移动到下载完成目录中（表示下载完成）
        File downloadingFile = new File(DownloadManagerConfig.Config
                .getDownloadingDirectoryById(defaultIdIfEmpty(downloadManagerId)), downloadTaskInfo.provideDownloadFileName());
        File downloadedFile = new File(DownloadManagerConfig.Config.
                getDownloadedDirectoryById(defaultIdIfEmpty(downloadManagerId)), downloadTaskInfo.provideDownloadFileName());
        downloadingFile.renameTo(downloadedFile);       // 两个目录要配置到统一上下文中，避免重命名文件发生文件移动造成的阻塞和不同权限目录中的移动造成的失败
        // 维护下载队列信息
        DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .addTaskThenUpdateToCache(DownloadTaskWrapper.this);
        DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .removeTaskByIdThenUpdateToCache(downloadTaskInfo.getUniquelyIdentifies());
        Log.i(TAG, "完成任务迁移，任务记录已更新...");
        DownloadManager.getInstance(defaultIdIfEmpty(downloadManagerId)).notifyItemDownloadComplete(this);
    }

    @Download.onTaskStart protected void taskStart(DownloadTask task) {
        if (task != null && task.getKey() != null && task.getKey().equals(combineDownloadLink())) {
            totalLength = task.getFileSize();
            currentOffset = task.getCurrentProgress();
            lastOffset = currentOffset;
            lastSpeed = 0; speed = 0;
            stateUpdateForPublish = true;
            startDetectDownloadSpeedAndProgress();
            Log.i(TAG, String.format("任务(%d)开始...currentOffset %d totalLength %d", taskId, currentOffset, totalLength));
        }
    }

    @Download.onTaskResume protected void taskResume(DownloadTask task) {
        if (task != null && task.getKey() != null && task.getKey().equals(combineDownloadLink())) {
            stateUpdateForPublish = true;
            startDetectDownloadSpeedAndProgress();
            Log.i(TAG, String.format("任务(%d)恢复...", taskId));
        }
    }

    //在这里处理任务执行中的状态，如进度进度条的刷新
    @Download.onTaskRunning protected void taskRunning(DownloadTask task) {
        if (task != null && task.getKey() != null && task.getKey().equals(combineDownloadLink())) {
            currentOffset = task.getCurrentProgress();
            stateUpdateForPublish = true;
            Log.i(TAG, String.format("任务(%d)进行中...currentOffset %d totalLength %d", taskId, currentOffset, totalLength));
        }
    }

    @Download.onTaskStop protected void taskStop(DownloadTask task) {
        if (task != null && task.getKey() != null && task.getKey().equals(combineDownloadLink())) {
            stateUpdateForPublish = true;
            stopDetectDownloadSpeedProgress();
            Log.i(TAG, String.format("任务(%d)停止...", taskId));
        }
    }

    @Download.onTaskCancel protected void taskCancel(DownloadTask task) {
        if (task != null && task.getKey() != null && task.getKey().equals(combineDownloadLink())) {
            stateUpdateForPublish = true;
            stopDetectDownloadSpeedProgress();
            Aria.download(DownloadTaskWrapper.this).unRegister();
            Log.i(TAG, String.format("任务(%d)取消...", taskId));
        }
    }

    @Download.onTaskFail protected void taskFail(DownloadTask task) {
        if (task != null && task.getKey() != null && task.getKey().equals(combineDownloadLink())) {
            stateUpdateForPublish = true;
            stopDetectDownloadSpeedProgress();
            DownloadTaskWrapper.this.restart();
            Log.i(TAG, String.format("任务(%d)失败...", taskId));
        }
    }

    @Download.onTaskComplete protected void taskComplete(DownloadTask task) {
        if (task != null && task.getKey() != null && task.getKey().equals(combineDownloadLink())) {
            stopDetectDownloadSpeedProgress();
            onDownloadCompletedInner();
            stateUpdateForPublish = true;
            Aria.download(DownloadTaskWrapper.this).unRegister();
            Log.i(TAG, String.format("任务(%d)完成...", taskId));
        }
    }
}
