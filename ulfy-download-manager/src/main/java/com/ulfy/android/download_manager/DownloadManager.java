package com.ulfy.android.download_manager;

import android.media.MediaPlayer;
import android.os.Environment;
import android.widget.Toast;

import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.task_extension.UiTimer;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 下载管理器，模块总入口
 */
public final class DownloadManager {
    private static final DownloadManager instance = new DownloadManager();              // 单例对象
    private final UiTimer netSpeedTimer = new UiTimer(1000);                      // 网速更新定时器，每秒更新以此

    /**
     * 私有化构造方法
     */
    private DownloadManager() {
        netSpeedTimer.setUiTimerExecuteBody(new UiTimer.UiTimerExecuteBody() {
            @Override public void onExecute(UiTimer timer, UiTimer.TimerDriver timerDriver) {
                onNetSpeedTimerExecute();
            }
        });
    }

    private void onNetSpeedTimerExecute() {
        // 如果有失效的任务就过滤掉并通知客户端状态已变化
        boolean clearRecord = false;
        if (DownloadingTaskRepository.getInstance().clearInvalidDownloadRecord()) {
            clearRecord = true;
        }
        if (DownloadedTaskRepository.getInstance().clearInvalidDownloadRecord()) {
            clearRecord = true;
        }
        if (clearRecord) {
            BusUtils.post(new OnDownloadManagerStateChangeEvent());
        }

        // 更新下载中任务的进度
        List<DownloadTask> downloadTaskList = DownloadingTaskRepository.getInstance().provideAllDownloadingTask();
        if (downloadTaskList != null && downloadTaskList.size() > 0) {
            for (DownloadTask downloadTask : downloadTaskList) {
                downloadTask.updateProgressOnTimer();
            }
            BusUtils.post(new OnDownloadManagerStateUpdateEvent());
            DownloadingTaskRepository.getInstance().updateToCache();
        }
    }

    /**
     * 获取下载管理器实例
     */
    public static DownloadManager getInstance() {
        DownloadManagerConfig.throwExceptionIfConfigNotConfigured();
        return instance;
    }

    /**
     * 初始化下载模块
     */
    void init() {
        netSpeedTimer.schedule();
    }

    /**
     * 反初始化下载任务模块
     *      通常下载任务的生命周期是和App的生命周期一样的，因此通常不需要调用
     */
    void deinit() {
        netSpeedTimer.cancel();
    }


    /*
    ====================================  下载中任务相关  =======================================
     */


    /**
     * 提供所有的下载中任务
     */
    public final List<DownloadTask> provideAllDownloadingTask() {
        return DownloadingTaskRepository.getInstance().provideAllDownloadingTask();
    }

    /**
     * 获取下载中任务数量
     */
    public final int getDownloadingTaskCount() {
        return DownloadingTaskRepository.getInstance().getDownloadTaskCount();
    }

    /**
     * 检查指定的下载任务否已经处于下载队列中
     */
    public final boolean checkIfDownloading(DownloadTask downloadTask) {
        return downloadTask != null && !downloadTask.isComplete();
    }

    /**
     * 检查指定的下载任务信是否已经处于下载队列中
     */
    public final boolean checkIfDownloading(DownloadTaskInfo downloadTaskInfo) {
        DownloadTask downloadTask = DownloadingTaskRepository.getInstance().findDownlaodTaskById(downloadTaskInfo.provideUniquelyIdentifies());
        return downloadTask != null && !downloadTask.isComplete();
    }

    /**
     * 获取下载中任务对应文件的大小
     */
    public final long getDownloadingTaskFileSize(DownloadTask downloadTask) {
        return downloadTask == null ? 0 : downloadTask.getTotalLength();
    }

    /**
     * 获取下载中任务信息对应文件的大小
     */
    public final long getDownloadingTaskFileSize(DownloadTaskInfo downloadTaskInfo) {
        DownloadTask downloadTask = DownloadingTaskRepository.getInstance().findDownlaodTaskById(downloadTaskInfo.provideUniquelyIdentifies());
        return downloadTask == null ? 0 : downloadTask.getTotalLength();
    }

    /**
     * 获取所有下载中任务对应文件的大小
     */
    public final long getAllDownloadingTaskFileSize() {
        long totalSize = 0;
        for (DownloadTask downloadTask : DownloadingTaskRepository.getInstance().provideAllDownloadingTask()) {
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
        DownloadTask downloadTask = DownloadingTaskRepository.getInstance().provideDownloadTaskByDownloadTaskInfo(downloadTaskInfo);
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance().canStartDownloadTask()) {
            downloadTask.start();
        }
    }

    /**
     * 启动一个下载任务
     */
    public final synchronized void start(DownloadTask downloadTask) {
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance().canStartDownloadTask()) {
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
    public final synchronized void stop(DownloadTask downloadTask) {
        if (downloadTask.stop()) {
            reScheduleDownloadTaskStatus();
            BusUtils.post(new DownloadManager.OnDownloadManagerStateUpdateEvent());
        }
    }

    /**
     * 重启一个下载任务
     */
    public final synchronized void restart(DownloadTask downloadTask) {
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance().canStartDownloadTask()) {
            if (downloadTask.restart()) {
                BusUtils.post(new DownloadManager.OnDownloadManagerStateUpdateEvent());
            }
        }
    }

    /**
     * 重新排布下载任务的状态
     */
    final synchronized void reScheduleDownloadTaskStatus() {
        // 如果是有下载数量限制的根据条件自动开启任务，没有设置下载次数的话保持原样
        if (DownloadManagerConfig.DownloadLimitConfig.getInstance().getLimitCount() > 0) {
            for (DownloadTask downloadTask : provideAllDownloadingTask()) {
                if (!DownloadManagerConfig.DownloadLimitConfig.getInstance().canStartDownloadTask()) {
                    break;
                }
                /*
                当任务没有启动时
                    1) 如果没有开启优先启动等待中任务则直接启动
                    2) 如果开启了优先启动等待中任务且任务处于等待状态则直接启动
                 */
                if (!downloadTask.isStart() && (!DownloadManagerConfig.Config.startWaitingFirst || downloadTask.isWaiting())) {
                    downloadTask.start();
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
    public final List<DownloadTask> provideAllDownloadedTaskInfo() {
        return DownloadedTaskRepository.getInstance().provideAllDownloadedTask();
    }

    /**
     * 获取已下载任务的数量
     */
    public final int getDownloadedTaskCount() {
        return DownloadedTaskRepository.getInstance().getDownloadTaskCount();
    }

    /**
     * 检查指定的下载任务是否已经被下载了
     */
    public final boolean checkIfDownloaded(DownloadTask downloadTask) {
        return downloadTask != null && downloadTask.isComplete();
    }

    /**
     * 检查指定的下载任务信息是否已经被下载了
     */
    public final boolean checkIfDownloaded(DownloadTaskInfo downloadTaskInfo) {
        DownloadTask downloadTask = DownloadedTaskRepository.getInstance().findDownlaodTaskById(downloadTaskInfo.provideUniquelyIdentifies());
        return downloadTask != null && downloadTask.isComplete();
    }

    /**
     * 根据下载任务获取对应的文件路径
     */
    public final File getDownloadedFile(DownloadTask downloadTask) {
        return downloadTask == null ? null : downloadTask.getTargetFile();
    }

    /**
     * 根据下载任务信息获取对应的文件路径
     */
    public final File getDownloadedFile(DownloadTaskInfo downloadTaskInfo) {
        DownloadTask downloadTask = DownloadedTaskRepository.getInstance().findDownlaodTaskById(downloadTaskInfo.provideUniquelyIdentifies());
        return downloadTask == null ? null : downloadTask.getTargetFile();
    }

    /**
     * 获取已下载任务对应文件的大小
     */
    public final long getDownloadedTaskFileSize(DownloadTask downloadTask) {
        return downloadTask == null ? 0 : downloadTask.getTargetFileSize();
    }

    /**
     * 获取已下载任务信息对应文件的大小
     */
    public final long getDownloadedTaskFileSize(DownloadTaskInfo downloadTaskInfo) {
        DownloadTask downloadTask = DownloadedTaskRepository.getInstance().findDownlaodTaskById(downloadTaskInfo.provideUniquelyIdentifies());
        return downloadTask == null ? 0 : downloadTask.getTargetFileSize();
    }

    /**
     * 获取所有已下载任务对应文件的大小总和
     */
    public final long getAllDownloadedTaskFileSize() {
        long totalSize = 0;
        for (DownloadTask downloadTask : DownloadedTaskRepository.getInstance().provideAllDownloadedTask()) {
            totalSize += downloadTask == null ? 0 : downloadTask.getTargetFileSize();
        }
        return totalSize;
    }


    /*
    ====================================  公共方法  =======================================
     */

    /**
     * 根据任务信息查找到对应的任务
     */
    public final DownloadTask findDownloadTaskByDownloadTaskInfo(DownloadTaskInfo downloadTaskInfo) {
        if (checkIfDownloading(downloadTaskInfo)) {
            return DownloadingTaskRepository.getInstance().findDownlaodTaskById(downloadTaskInfo.provideUniquelyIdentifies());
        } else if (checkIfDownloaded(downloadTaskInfo)) {
            return DownloadedTaskRepository.getInstance().findDownlaodTaskById(downloadTaskInfo.provideUniquelyIdentifies());
        } else {
            return null;
        }
    }

    /**
     * 销毁一个下载任务
     */
    public final synchronized void destroy(DownloadTask downloadTask) {
        if (!downloadTask.isComplete()) {
            DownloadingTaskRepository.getInstance().removeDownloadTaskById(downloadTask.getDownloadTaskInfo().provideUniquelyIdentifies());
            DownloadingTaskRepository.getInstance().updateToCache();
        } else {
            DownloadedTaskRepository.getInstance().removeDownloadTaskById(downloadTask.getDownloadTaskInfo().provideUniquelyIdentifies());
            DownloadedTaskRepository.getInstance().updateToCache();
        }
        downloadTask.destroy();
        reScheduleDownloadTaskStatus();
        BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
    }

    /**
     * 销毁一个下载任务
     */
    public final synchronized void destroy(DownloadTaskInfo downloadTaskInfo) {
        DownloadTask downloadTask = findDownloadTaskByDownloadTaskInfo(downloadTaskInfo);
        if (downloadTask != null) {
            destroy(downloadTask);
        }
    }

    /**
     * 销毁多个下载任务
     */
    public final synchronized void destroyByDownloadTaskList(List<DownloadTask> downloadTaskList) {
        for (DownloadTask downloadTask : downloadTaskList) {
            if (!downloadTask.isComplete()) {
                DownloadingTaskRepository.getInstance().removeDownloadTaskById(downloadTask.getDownloadTaskInfo().provideUniquelyIdentifies());
            } else {
                DownloadedTaskRepository.getInstance().removeDownloadTaskById(downloadTask.getDownloadTaskInfo().provideUniquelyIdentifies());
            }
            downloadTask.destroy();
        }
        DownloadingTaskRepository.getInstance().updateToCache();
        DownloadedTaskRepository.getInstance().updateToCache();
        reScheduleDownloadTaskStatus();
        BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
    }

    /**
     * 销毁多个下载任务
     */
    public final synchronized void destroyByDownloadTaskInfoList(List<DownloadTaskInfo> downloadTaskInfoList) {
        List<DownloadTask> downloadTaskList = new ArrayList<>();
        for (DownloadTaskInfo downloadTaskInfo : downloadTaskInfoList) {
            DownloadTask downloadTask = findDownloadTaskByDownloadTaskInfo(downloadTaskInfo);
            if (downloadTask != null) {
                downloadTaskList.add(downloadTask);
            }
        }
        destroyByDownloadTaskList(downloadTaskList);
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
