package com.ulfy.android.download_manager;

import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener3;
import com.ulfy.android.bus.BusUtils;

import java.io.File;
import java.io.Serializable;

/**
 * 表示一个具体的下载任务
 *      包括下载任务的状态信息
 *      下载引擎的对接
 */
public class DownloadTask<T extends DownloadTaskInfo> implements Serializable {
    private static final long serialVersionUID = -6410544290427834001L;
    /**
     * 用于下载的真正数据
     */
    private T downloadTaskInfo;
    /**
     * 任务的状态：已开始，未开始
     *      未开始包括两种状态：
     *          1）未限制同时下载数量时表示暂停
     *          2）限制同时下载数量时不允许开始下载的任务表示等待中（默认任务的状态为等待中）
     */
    private transient boolean start = false;
    private transient boolean waiting = true;
    /**
     * 标记任务是否已经完成
     */
    private boolean complete = false;
    /**
     * 统计任务的状态
     *      任务信息、任务进度、下载速度等
     */
    private transient long lastOffset = 0;
    private long currentOffset = 0;
    private long totalLength = 0;
    private transient long speed = 0;
    /**
     * 三方下载引擎
     */
    private transient com.liulishuo.okdownload.DownloadTask downloadTaskInner;
    private transient DownloadListenerInner downloadListenerInner;

    /**
     * 构造方法
     */
    DownloadTask(T downloadTaskInfo) {
        this.downloadTaskInfo = downloadTaskInfo;
    }

    /**
     * 开始任务
     * @return 该操作是否执行成功
     */
    synchronized boolean start() {
        if (!start && !complete) {
            start = true;
            waiting = false;
            startDownloadInner();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 停止任务
     * @return 该操作是否执行成功
     */
    synchronized boolean stop() {
        if (!complete && start) {
            start = false;
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
    synchronized boolean waiting() {
        if (!complete && !start) {
            waiting = true;
            return true;
        } else {
            return false;
        }
    }

    /**
     * 重启任务
     * @return 该操作是否执行成功
     */
    synchronized boolean restart() {
        if (!complete) {
            start = true;
            waiting = false;
            stopDownloadInner();
            startDownloadInner();
            return true;
        } else {
            return false;
        }
    }

    /**
     * 当下载完成后转移下载中文件到已下载目录中
     * @return 该操作是否执行成功
     */
    synchronized boolean translateToDownloadedDirWhenComplete() {
        if (!complete) {
            File downloadingFile = new File(DownloadManagerConfig.Config.downloadingDirectory, downloadTaskInfo.provideDownloadFileName());
            File downloadedFile = new File(DownloadManagerConfig.Config.downloadedDirectory, downloadTaskInfo.provideDownloadFileName());
            if (downloadingFile.renameTo(downloadedFile)) {
                complete = true;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 销毁任务
     */
    synchronized void destroy() {
        stopDownloadInner();
        getTargetFile().delete();
        downloadTaskInfo = null;
        start = false;
        waiting = false;
        complete = false;
        lastOffset = 0;
        currentOffset = 0;
        totalLength = 0;
        speed = 0;
        downloadTaskInner = null;
        downloadListenerInner = null;
    }

    /**
     * 定时器每次执行更新
     */
    void updateProgressOnTimer() {
        // 计算网速
        speed = currentOffset - lastOffset;
        // 如果是首次计算或速度小于0，则置为0
        if (lastOffset == 0 || speed < 0) {
            speed = 0;
        }
        // 记录重置
        lastOffset = currentOffset;
    }

    /**
     * 获取任务唯一标识
     */
    String provideUniquelyIdentifies() {
        return downloadTaskInfo.provideUniquelyIdentifies();
    }

    /**
     * 当前任务是否还有效
     *      对于下载中任务无论是否文件被移除都有效
     *      对于下载完成的任务如果被外部移除则说明该任务失效了
     */
    boolean isValid() {
        return !complete || getTargetFile().exists();
    }

    /**
     * 获取与任务关联的目标文件
     *      该文件会根据当前任务是否完成而返回不同的路径
     */
    public final File getTargetFile() {
        return new File(!complete ? DownloadManagerConfig.Config.downloadingDirectory :
                DownloadManagerConfig.Config.downloadedDirectory,
                downloadTaskInfo.provideDownloadFileName());
    }

    /**
     * 获取目标文件的大小
     */
    long getTargetFileSize() {
        return isValid() ? getTargetFile().length() : 0;
    }

    /**
     * 获取下载任务内部的任务信息
     */
    public final T getDownloadTaskInfo() {
        return downloadTaskInfo;
    }

    /**
     * 是否已经启动了
     */
    public final boolean isStart() {
        return start;
    }

    /**
     * 是否处于等待中
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
     * 获取当前下载偏移字节
     */
    public final long getCurrentOffset() {
        return currentOffset;
    }

    /**
     * 获取总大小字节
     */
    public final long getTotalLength() {
        return totalLength;
    }

    /**
     * 获取进度
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
                if (DownloadManagerConfig.DownloadLimitConfig.getInstance().getLimitCount() > 0) {
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

    void startDownloadInner() {
        initDownloadTaskIfNeedInner();
        downloadTaskInner.enqueue(downloadListenerInner);
    }

    void stopDownloadInner() {
        initDownloadTaskIfNeedInner();
        downloadTaskInner.cancel();
    }

    /**
     * 因为采用了序列化机制，当序列化恢复时对应的未序列化的字段会为空，因此需要恢复
     */
    void initDownloadTaskIfNeedInner() {
        if (downloadTaskInner == null) {
            downloadTaskInner = new com.liulishuo.okdownload.DownloadTask.Builder(downloadTaskInfo.provideDownloadFileLink(), DownloadManagerConfig.Config.downloadingDirectory)
                    .setFilename(downloadTaskInfo.provideDownloadFileName()).setMinIntervalMillisCallbackProcess(30)
                    .setPassIfAlreadyCompleted(false).build();
        }
        if (downloadListenerInner == null) {
            downloadListenerInner = new DownloadListenerInner(this);
        }
    }

    /**
     * 下载过程监听，用于对接任务状态DownloadTask
     */
    private class DownloadListenerInner extends DownloadListener3 {
        private DownloadTask downloadTask;

        public DownloadListenerInner(DownloadTask downloadTask) {
            this.downloadTask = downloadTask;
        }

        @Override protected void started(com.liulishuo.okdownload.DownloadTask task) { }
        @Override protected void completed(com.liulishuo.okdownload.DownloadTask task) {
            if (downloadTask.translateToDownloadedDirWhenComplete()) {
                DownloadedTaskRepository.getInstance().addDownloadTask(downloadTask);
                DownloadingTaskRepository.getInstance().removeDownloadTaskById(downloadTask.downloadTaskInfo.provideUniquelyIdentifies());
                DownloadManager.getInstance().reScheduleDownloadTaskStatus();
                BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
            }
        }
        @Override protected void canceled(com.liulishuo.okdownload.DownloadTask task) { }
        @Override protected void error(com.liulishuo.okdownload.DownloadTask task, Exception e) {
            restart();
        }
        @Override protected void warn(com.liulishuo.okdownload.DownloadTask task) { }
        @Override public void retry(com.liulishuo.okdownload.DownloadTask task, ResumeFailedCause cause) { }
        @Override public void connected(com.liulishuo.okdownload.DownloadTask task, int blockCount, long currentOffset, long totalLength) {
            DownloadTask.this.lastOffset = currentOffset;
            DownloadTask.this.currentOffset = currentOffset;
            DownloadTask.this.totalLength = totalLength;
            DownloadTask.this.speed = 0;
        }
        @Override public void progress(com.liulishuo.okdownload.DownloadTask task, long currentOffset, long totalLength) {
            DownloadTask.this.currentOffset = currentOffset;
        }
    }
}
