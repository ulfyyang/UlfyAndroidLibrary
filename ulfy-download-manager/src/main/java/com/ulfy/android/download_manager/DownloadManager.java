package com.ulfy.android.download_manager;

import android.media.MediaPlayer;
import android.os.Environment;
import android.widget.Toast;

import com.ulfy.android.bus.BusUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

import static com.ulfy.android.download_manager.DownloadManagerConfig.defaultIdIfEmpty;

/**
 * 下载管理器，模块总入口。回调方法均不保证是在UI线程中运行
 */
public final class DownloadManager {
    private static final Map<String, DownloadManager> downloadManagerMap = new HashMap<>();
    private String downloadManagerId;           //下载管理器标识
    // 下载任务是一个长时间运行的过程，因此不要在单个页面上注册，要在Application中进行全局注册，其它页面需要的话可以派发事件
    private OnItemDownloadCompleteListener onItemDownloadCompleteListener;               // 当任务下载完成之后的全局回调（每个任务完成都会回调一次）
    private OnAllDownloadCompleteListener onAllDownloadCompleteListener;                 // 当任务下载完成之后的全局回调（所有任务下载完成后会回调）

    static {
        // 如果有失效的任务就过滤掉并通知客户端状态已变化（可能是用户手动删除了文件），因为面向所有下载管理器，所以只需要初始化一次
        Observable.interval(1, TimeUnit.SECONDS).subscribe(x -> {
            boolean clearRecord = false;
            if (DownloadingTaskRepository.deleteInvalidRecord()) {
                clearRecord = true;
            }
            if (DownloadedTaskRepository.deleteInvalidRecord()) {
                clearRecord = true;
            }
            if (clearRecord) {
                BusUtils.post(new OnDownloadManagerStateChangeEvent());
            }
        });
    }

    /**
     * 私有化构造方法
     */
    private DownloadManager(String downloadManagerId) {
        this.downloadManagerId = defaultIdIfEmpty(downloadManagerId);
        // 如果下载中的任务有状态更新则通知客户端状态已更新（每秒检查一次）
        Observable.interval(1, TimeUnit.SECONDS).subscribe(x -> {
            boolean stateUpdate = false;
            List<DownloadTaskWrapper> downloadTaskList = DownloadingTaskRepository
                    .getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
            for (DownloadTaskWrapper downloadTask : downloadTaskList) {
                if (downloadTask.isStateUpdated()) {
                    stateUpdate = true;
                }
                downloadTask.stateUpdatePublished();
            }
            if (stateUpdate) {
                BusUtils.post(new OnDownloadManagerStateUpdateEvent());
                DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                        .updateToCache();
            }
        });
    }

    /**
     * 获取默认的下载管理器实例
     */
    public static DownloadManager getInstance() {
        return getInstance(DownloadManagerConfig.DEFAULT_DOWNLOAD_MANAGER_ID);
    }

    /**
     * 根据下载管理ID获取下载管理器
     * @param downloadManagerId 下载管理ID，用户自定义的用于跟踪的KEY
     */
    public static DownloadManager getInstance(String downloadManagerId) {
        DownloadManagerConfig.throwExceptionIfConfigNotConfigured();
        downloadManagerId = defaultIdIfEmpty(downloadManagerId);
        DownloadManager downloadManager = downloadManagerMap.get(downloadManagerId);
        if (downloadManager == null) {
            downloadManager = new DownloadManager(downloadManagerId);
            downloadManagerMap.put(downloadManagerId, downloadManager);
        }
        return downloadManager;
    }

    /*
    ====================================  全局回调相关  =======================================
     */

    public interface OnItemDownloadCompleteListener {
        /**
         * 当一个任务下载完成之后执行该方法
         * @param downloadTask          下载完成的任务
         * @param downloadingCount      下载中仓库中的任务数量
         * @param downloadedCount       下载完成的任务数量
         * @param totalCount            总的下载任务数量（下载中、已完成）
         */
        void onDownloadComplete(DownloadTaskWrapper downloadTask, int downloadingCount, int downloadedCount, int totalCount);
    }

    public interface OnAllDownloadCompleteListener {
        /**
         * 当当前下载管理器中的所有下载任务完成时执行的方法
         */
        void onDownloadComplete();
    }

    /**
     * 当下载任务速度变更时调用这里
     */
    final void notifyItemSpeedChanged(DownloadTaskWrapper downloadTask, long lastSpeed, long currentSpeed) {
        // 这个方法先占个位置，暂时没有具体的业务需求需要
    }

    /**
     * 当下载任务进度变更时调用这里
     */
    final void notifyItemProgressChanged(DownloadTaskWrapper downloadTask, long lastOffset, long currentOffset) {
        // 这个方法先占个位置，暂时没有具体的业务需求需要
    }

    /**
     * 当单个任务下载完成时调用这里
     */
    final void notifyItemDownloadComplete(DownloadTaskWrapper downloadTask) {
        reScheduleDownloadTaskStatus();
        BusUtils.post(new OnDownloadManagerStateChangeEvent());
        int downloadingCount = DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getTaskCount();
        int downloadedCount = DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getTaskCount();
        int totalCount = downloadingCount + downloadedCount;
        if (onItemDownloadCompleteListener != null) {
            onItemDownloadCompleteListener.onDownloadComplete(downloadTask, downloadingCount, downloadedCount, totalCount);
        }
        if (onAllDownloadCompleteListener != null && downloadingCount == 0) {
            onAllDownloadCompleteListener.onDownloadComplete();
        }
    }

    /**
     * 设置当任务下载完成之后的全局回调（每个任务完成都会回调一次）
     */
    public DownloadManager setOnItemDownloadCompleteListener(OnItemDownloadCompleteListener onItemDownloadCompleteListener) {
        this.onItemDownloadCompleteListener = onItemDownloadCompleteListener;
        return this;
    }

    /**
     * 设置当任务下载完成之后的全局回调（所有任务下载完成后会回调）
     */
    public DownloadManager setOnAllDownloadCompleteListener(OnAllDownloadCompleteListener onAllDownloadCompleteListener) {
        this.onAllDownloadCompleteListener = onAllDownloadCompleteListener;
        return this;
    }

    /*
    ====================================  下载中任务相关  =======================================
     */


    /**
     * 提供所有的下载中任务
     */
    public final List<DownloadTaskWrapper> provideAllDownloadingTask() {
        return DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
    }

    /**
     * 获取下载中任务数量
     */
    public final int getDownloadingTaskCount() {
        return DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getTaskCount();
    }

    /**
     * 检查指定的下载任务否已经处于下载队列中
     */
    public final boolean checkIfDownloading(DownloadTaskWrapper downloadTask) {
        return downloadTask != null && !downloadTask.isComplete();
    }

    /**
     * 检查指定的下载任务信是否已经处于下载队列中
     */
    public final boolean checkIfDownloading(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .findTaskById(downloadTaskInfo.getUniquelyIdentifies());
        return downloadTask != null && !downloadTask.isComplete();
    }

    /**
     * 获取下载中任务对应文件的大小
     */
    public final long getDownloadingTaskFileSize(DownloadTaskWrapper downloadTask) {
        return downloadTask == null ? 0 : downloadTask.getTotalLength();
    }

    /**
     * 获取下载中任务信息对应文件的大小
     */
    public final long getDownloadingTaskFileSize(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .findTaskById(downloadTaskInfo.getUniquelyIdentifies());
        return downloadTask == null ? 0 : downloadTask.getTotalLength();
    }

    /**
     * 获取所有下载中任务对应文件的大小
     */
    public final long getAllDownloadingTaskFileSize() {
        long totalSize = 0;
        for (DownloadTaskWrapper downloadTask : DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask()) {
            totalSize += downloadTask == null ? 0 : downloadTask.getTotalLength();
        }
        return totalSize;
    }

    /**
     * 新建一个下载任务并开始，在一下情况下不会开始并会给出相应的提示
     *      1) 该任务已经下载完成
     *      2) 该任务已经在下载列表中
     */
    public final synchronized void newDownloadingTaskWithTip(DownloadTaskInfo downloadTaskInfo) {
        if (checkIfDownloaded(downloadTaskInfo)) {
            Toast.makeText(DownloadManagerConfig.context, "该任务已经下载完成", Toast.LENGTH_LONG).show();
        } else if (checkIfDownloading(downloadTaskInfo)) {
            newDownloadingTask(downloadTaskInfo);
            Toast.makeText(DownloadManagerConfig.context, "该任务正在下载列表中", Toast.LENGTH_LONG).show();
        } else {
            newDownloadingTask(downloadTaskInfo);
            Toast.makeText(DownloadManagerConfig.context, "开始下载", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 新建一个下载任务并开始
     */
    public final synchronized void newDownloadingTask(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .getDownloadTaskByTaskInfo(downloadTaskInfo);       // 内部会发布状态变化事件
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance(defaultIdIfEmpty(downloadManagerId)).canStartDownloadTask()) {
            downloadTask.start();
        }
    }

    /**
     * 启动一个下载任务
     */
    public final synchronized void start(DownloadTaskWrapper downloadTask) {
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance(defaultIdIfEmpty(downloadManagerId)).canStartDownloadTask()) {
            if (downloadTask.start()) {
                BusUtils.post(new DownloadManager.OnDownloadManagerStateUpdateEvent());
            }
        } else {
            if (downloadTask.waiting()) {
                BusUtils.post(new DownloadManager.OnDownloadManagerStateUpdateEvent());
            }
        }
    }

    /**
     * 停止一个下载任务
     */
    public final synchronized void stop(DownloadTaskWrapper downloadTask) {
        if (downloadTask.stop()) {
            reScheduleDownloadTaskStatus();
            BusUtils.post(new DownloadManager.OnDownloadManagerStateUpdateEvent());
        }
    }

    /**
     * 重启一个下载任务
     */
    public final synchronized void restart(DownloadTaskWrapper downloadTask) {
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance(defaultIdIfEmpty(downloadManagerId)).canStartDownloadTask()) {
            if (downloadTask.restart()) {
                BusUtils.post(new DownloadManager.OnDownloadManagerStateUpdateEvent());
            }
        }
    }

    /**
     * 清空所有下载中的任务
     */
    public final synchronized void clearAllDownloadingTask() {
        List<DownloadTaskWrapper> downloadingTaskList = DownloadingTaskRepository
                .getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
        destroyByDownloadTaskList(downloadingTaskList);
    }

    /**
     * 重新排布下载任务的状态
     */
    final synchronized void reScheduleDownloadTaskStatus() {
        // 如果是有下载数量限制的根据条件自动开启任务，没有设置下载次数的话保持原样
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance(defaultIdIfEmpty(downloadManagerId)).getLimitCount() > 0) {
            for (DownloadTaskWrapper downloadTask : provideAllDownloadingTask()) {
                if (DownloadManagerConfig.DownloadLimitConfig.getInstance(defaultIdIfEmpty(downloadManagerId)).canStartDownloadTask()) {
                    // 当任务没有启动时 1) 如果没有开启优先启动等待中任务，则直接启动 2) 如果开启了优先启动等待中任务且任务处于等待状态，则直接启动
                    if (!downloadTask.isStart() && (!DownloadManagerConfig.Config.startWaitingFirst || downloadTask.isWaiting())) {
                        downloadTask.start();
                    }
                }
            }
        }
    }


    /*
    ====================================  已下载任务相关  =======================================
     */


    /**
     * 获取全部的下载任务列表
     */
    public final List<DownloadTaskWrapper> provideAllDownloadedTaskInfo() {
        return DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
    }

    /**
     * 获取已下载任务的数量
     */
    public final int getDownloadedTaskCount() {
        return DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getTaskCount();
    }

    /**
     * 检查指定的下载任务是否已经被下载了
     */
    public final boolean checkIfDownloaded(DownloadTaskWrapper downloadTask) {
        return downloadTask != null && downloadTask.isComplete();
    }

    /**
     * 检查指定的下载任务信息是否已经被下载了
     */
    public final boolean checkIfDownloaded(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .findTaskById(downloadTaskInfo.getUniquelyIdentifies());
        return downloadTask != null && downloadTask.isComplete();
    }

    /**
     * 根据下载任务获取对应的文件路径
     */
    public final File getDownloadedFile(DownloadTaskWrapper downloadTask) {
        return downloadTask == null ? null : downloadTask.getTargetFile();
    }

    /**
     * 根据下载任务信息获取对应的文件路径
     */
    public final File getDownloadedFile(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .findTaskById(downloadTaskInfo.getUniquelyIdentifies());
        return downloadTask == null ? null : downloadTask.getTargetFile();
    }

    /**
     * 获取已下载任务对应文件的大小
     */
    public final long getDownloadedTaskFileSize(DownloadTaskWrapper downloadTask) {
        return downloadTask == null ? 0 : downloadTask.getTargetFileSize();
    }

    /**
     * 获取已下载任务信息对应文件的大小
     */
    public final long getDownloadedTaskFileSize(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .findTaskById(downloadTaskInfo.getUniquelyIdentifies());
        return downloadTask == null ? 0 : downloadTask.getTargetFileSize();
    }

    /**
     * 获取所有已下载任务对应文件的大小总和
     */
    public final long getAllDownloadedTaskFileSize() {
        long totalSize = 0;
        for (DownloadTaskWrapper downloadTask : DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask()) {
            totalSize += downloadTask == null ? 0 : downloadTask.getTargetFileSize();
        }
        return totalSize;
    }

    /**
     * 清空所有下载完成的任务
     */
    public final synchronized void clearAllDownloadedTask() {
        List<DownloadTaskWrapper> downloadedTaskList = DownloadedTaskRepository
                .getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
        destroyByDownloadTaskList(downloadedTaskList);
    }

    /*
    ====================================  公共方法  =======================================
     */

    /**
     * 获取所有的下载任务（包括下载中的和已完成的）
     */
    public final List<DownloadTaskWrapper> provideAllDownloadTaskInfo() {
        List<DownloadTaskWrapper> downloadingTaskList = DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
        List<DownloadTaskWrapper> downloadedTaskList = DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
        List<DownloadTaskWrapper> allTaskList = new ArrayList<>();
        allTaskList.addAll(downloadingTaskList); allTaskList.addAll(downloadedTaskList);
        return allTaskList;
    }

    /**
     * 根据任务信息查找到对应的任务
     */
    public final DownloadTaskWrapper findDownloadTaskByDownloadTaskInfo(DownloadTaskInfo downloadTaskInfo) {
        if (checkIfDownloading(downloadTaskInfo)) {
            return DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                    .findTaskById(downloadTaskInfo.getUniquelyIdentifies());
        } else if (checkIfDownloaded(downloadTaskInfo)) {
            return DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                    .findTaskById(downloadTaskInfo.getUniquelyIdentifies());
        } else {
            return null;
        }
    }

    /**
     * 销毁一个下载任务
     */
    public final synchronized void destroy(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = findDownloadTaskByDownloadTaskInfo(downloadTaskInfo);
        if (downloadTask != null) {
            destroy(downloadTask);
        }
    }

    /**
     * 销毁一个下载任务
     */
    public final synchronized void destroy(DownloadTaskWrapper downloadTask) {
        if (!downloadTask.isComplete()) {
            DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                    .removeTaskByIdThenUpdateToCache(downloadTask.provideUniquelyIdentifies());
        } else {
            DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                    .removeTaskByIdThenUpdateToCache(downloadTask.provideUniquelyIdentifies());
        }
        downloadTask.destroy(); reScheduleDownloadTaskStatus();
        BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
    }

    /**
     * 销毁多个下载任务
     */
    public final synchronized void destroyByDownloadTaskList(List<DownloadTaskWrapper> downloadTaskList) {
        for (DownloadTaskWrapper downloadTask : downloadTaskList) {
            downloadTask.destroy();
        }
        DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .removeTaskByTaskListThenUpdateToCache(downloadTaskList);
        DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId))
                .removeTaskByTaskListThenUpdateToCache(downloadTaskList);
        reScheduleDownloadTaskStatus();
        BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
    }

    /**
     * 销毁多个下载任务
     */
    public final synchronized void destroyByDownloadTaskInfoList(List<DownloadTaskInfo> downloadTaskInfoList) {
        List<DownloadTaskWrapper> downloadTaskList = new ArrayList<>();
        for (DownloadTaskInfo downloadTaskInfo : downloadTaskInfoList) {
            DownloadTaskWrapper downloadTask = findDownloadTaskByDownloadTaskInfo(downloadTaskInfo);
            if (downloadTask != null) {
                downloadTaskList.add(downloadTask);
            }
        }
        destroyByDownloadTaskList(downloadTaskList);
    }

    /**
     * 清空所有的任务
     */
    public final synchronized void clearAllDownloadTask() {
        List<DownloadTaskWrapper> downloadingTaskList = DownloadingTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
        List<DownloadTaskWrapper> downloadedTaskList = DownloadedTaskRepository.getInstance(defaultIdIfEmpty(downloadManagerId)).getAllTask();
        List<DownloadTaskWrapper> allTaskList = new ArrayList<>();
        allTaskList.addAll(downloadingTaskList); allTaskList.addAll(downloadedTaskList);
        destroyByDownloadTaskList(allTaskList);
    }

    /*
    ====================================  其它工具方法  =======================================
     */

    /**
     * 获取已经使用的空间
     */
    public long getUsedSpace() {
        return getAllDownloadingTaskFileSize() + getAllDownloadedTaskFileSize();
    }

    /**
     * 获取可用空间
     */
    public long getAvaliableSpace() {
        return Environment.getDataDirectory().getFreeSpace();
    }

    static final long H_UNIT = 60 * 60 * 1000;
    static final long M_UNIT = 60 * 1000;
    static final long S_UNIT = 1000;
    static final DecimalFormat timeDecimalFormat = new DecimalFormat("00");

    /**
     * 转换视频文件时长工具类
     */
    public static int getVideoFileDuration(File file) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 转换视频文件时长工具类
     */
    public static String getVideoFileDurationString(File file) {
        long duration = getVideoFileDuration(file);
        long hour = duration / H_UNIT;
        long minute = duration % H_UNIT / M_UNIT;
        long second = (duration % H_UNIT - minute * M_UNIT) / S_UNIT;
        return String.format("%s:%s:%s", timeDecimalFormat.format(hour),
                timeDecimalFormat.format(minute), timeDecimalFormat.format(second));
    }

    static final long GB_UNIT = 1024 * 1024 * 1024;
    static final long MB_UNIT = 1024 * 1024;
    static final long KB_UNIT = 1024;

    /**
     * 转换字节为人类可读单位
     */
    public static float convertFileSizeToHumanReadable(long size) {
        if (size > GB_UNIT) {                   // GB
            return size * 1.0f / GB_UNIT;
        }
        if (size > MB_UNIT) {                   // MB
            return size * 1.0f / MB_UNIT;
        }
        if (size > KB_UNIT) {                   // KB
            return size * KB_UNIT;
        }
        return size * 1.0f;                     // B
    }

    /**
     * 转换字节为人类可读单位
     *      并追加单位字符串
     */
    public static String convertFileSizeToHumanReadableString(long size) {
        if (size > GB_UNIT) {                           // GB
            return String.format("%.1fGB", size * 1.0f / GB_UNIT);
        }
        if (size > MB_UNIT) {                           // MB
            return String.format("%.1fMB", size * 1.0f / MB_UNIT);
        }
        if (size > KB_UNIT) {                           // KB
            return String.format("%.1fKB", size * 1.0f / KB_UNIT);
        }
        return String.format("%.1fB", size * 1.0f);     // B
    }


    /**
     * 当下载管理器状态发生变化时触发事件（任务数量发生变化）
     *      用于通知下载相关页面更新
     * 发布事件的时机
     *      当新增下载任务时
     *      当下载任务完成时
     *      从缓存文件恢复时
     *      删除缓存任务时
     *      已经缓存的视频被删除时
     */
    public static class OnDownloadManagerStateChangeEvent { }

    /**
     * 当下载管理器状态更新时触发事件（任务数量没有变化）
     *      该事件每秒触发一次，全局触发
     *      当任务本身状态发生变化时也会触发
     */
    public static class OnDownloadManagerStateUpdateEvent { }

}
