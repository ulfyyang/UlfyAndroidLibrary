package com.ulfy.android.download_manager;

import android.util.Log;

import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener3;

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
 */
public class DownloadTask<T extends DownloadTaskInfo> implements Serializable {
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
    DownloadTask(String downloadManagerId, T downloadTaskInfo) {
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
        stopDownloadInner();
        getTargetFile().delete();
        start = false; waiting = false; complete = false;
        totalLength = 0; currentOffset = 0;
        lastOffset = 0; lastSpeed = 0; speed = 0;
        stateUpdateForPublish = true;
        downloadTaskInner = null; downloadListenerInner = null;
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
        File dir = !complete ? DownloadManagerConfig.Config.downloadingDirectory :
                DownloadManagerConfig.Config.downloadedDirectory;
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

    private transient com.liulishuo.okdownload.DownloadTask downloadTaskInner;
    private transient DownloadListenerInner downloadListenerInner;
    private transient Disposable speedProgressDetectDisposable;         // 用于跟踪网速探测进程

    private void startDownloadInner() {
        initDownloadTaskIfNeedInner();
        downloadTaskInner.enqueue(downloadListenerInner);
    }

    private void stopDownloadInner() {
        initDownloadTaskIfNeedInner();
        downloadTaskInner.cancel();
    }

    /**
     * 因为采用了序列化机制，当序列化恢复时对应的未序列化的字段会为空，因此需要恢复
     */
    private void initDownloadTaskIfNeedInner() {
        if (downloadTaskInner == null) {
            downloadTaskInner = new com.liulishuo.okdownload.DownloadTask
                    .Builder(downloadTaskInfo.provideDownloadFileLink(), DownloadManagerConfig.Config.downloadingDirectory)
                    .setFilename(downloadTaskInfo.provideDownloadFileName()).setMinIntervalMillisCallbackProcess(50)
                    .setPassIfAlreadyCompleted(false).build();
        }
        if (downloadListenerInner == null) {
            downloadListenerInner = new DownloadListenerInner();
        }
    }

    /**
     * 开始探测下载速度、进度进程（这里的进程指的是一个过程，而不是操作系统的进程）
     */
    private void startDetectDownloadSpeedAndProgress() {
        if (speedProgressDetectDisposable != null) {
            speedProgressDetectDisposable.dispose();
        }
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

    /**
     * 停止探测下载速度（停止探测后下载速度将会变为0）
     */
    private void stopDetectDownloadSpeedProgress() {
        if (speedProgressDetectDisposable != null) {
            speedProgressDetectDisposable.dispose();
        }
        speed = 0;
    }

    /**
     * 当内部任务下载完毕后调用的回调，完成一些后续工作（移动文件位置、重新调整任务队列、执行相应的回调）
     */
    private void onDownloadCompletedInner() {
        // 将下载中文件移动到下载完成目录中（表示下载完成）
        File downloadingFile = new File(DownloadManagerConfig.Config
                .getDownloadingDirectoryById(defaultIdIfEmpty(downloadManagerId)), downloadTaskInfo.provideDownloadFileName());
        File downloadedFile = new File(DownloadManagerConfig.Config.
                getDownloadedDirectoryById(defaultIdIfEmpty(downloadManagerId)), downloadTaskInfo.provideDownloadFileName());
        downloadingFile.renameTo(downloadedFile);       // 两个目录要配置到统一上下文中，避免重命名文件发生文件移动造成的阻塞和不同权限目录中的移动造成的失败
        // 维护下载队列信息
        DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .addTask(DownloadTask.this);
        DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .removeTaskById(downloadTaskInfo.getUniquelyIdentifies());
        complete = true;
        stateUpdateForPublish = true;
        DownloadManager.getInstance(defaultIdIfEmpty(downloadManagerId)).notifyItemDownloadComplete(this);
    }

    /**
     * 下载过程监听，用于对接任务状态DownloadTask。以下方法的声明按照任务生命周期的顺序声明
     *      内部任务是一个持续化的过程，内部任务负责更新外部任务的状态
     */
    private class DownloadListenerInner extends DownloadListener3 {
        @Override protected void started(com.liulishuo.okdownload.DownloadTask task) {
            Log.i(TAG, "任务启动...");
        }
        @Override public void connected(com.liulishuo.okdownload.DownloadTask task, int blockCount, long currentOffset, long totalLength) {
            Log.i(TAG, String.format("任务连接...currentOffset %d totalLength %d", currentOffset, totalLength));
            DownloadTask.this.totalLength = totalLength;
            DownloadTask.this.currentOffset = currentOffset;
            DownloadTask.this.lastOffset = currentOffset;
            DownloadTask.this.lastSpeed = 0; DownloadTask.this.speed = 0;
            DownloadTask.this.startDetectDownloadSpeedAndProgress();
        }
        @Override public void progress(com.liulishuo.okdownload.DownloadTask task, long currentOffset, long totalLength) {
            Log.i(TAG, String.format("任务进行中...currentOffset %d totalLength %d", currentOffset, totalLength));
            DownloadTask.this.currentOffset = currentOffset;
            stateUpdateForPublish = true;
        }
        @Override protected void completed(com.liulishuo.okdownload.DownloadTask task) {
            Log.i(TAG, "任务完成...");
            DownloadTask.this.stopDetectDownloadSpeedProgress();
            DownloadTask.this.onDownloadCompletedInner();
        }
        @Override protected void canceled(com.liulishuo.okdownload.DownloadTask task) {
            Log.i(TAG, "任务取消...");
            DownloadTask.this.stopDetectDownloadSpeedProgress();
        }
        @Override protected void error(com.liulishuo.okdownload.DownloadTask task, Exception e) {
            Log.w(TAG, "任务出错...", e);
            DownloadTask.this.restart();
        }
        @Override protected void warn(com.liulishuo.okdownload.DownloadTask task) { }
        @Override public void retry(com.liulishuo.okdownload.DownloadTask task, ResumeFailedCause cause) {
            Log.w(TAG, "任务重试...");
        }
    }
}
