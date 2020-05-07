package com.ulfy.android.download_manager;

import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.cache.ICache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载任务仓库，用于管理下载任务
 */
class DownloadingTaskRepository implements Serializable {
    private static final long serialVersionUID = 185632873962007026L;
    private Map<String, DownloadTask> downloadTaskMap = new LinkedHashMap<>();                          // 存储下载任务

    /**
     * 私有化构造方法
     */
    private DownloadingTaskRepository() { }

    /**
     * 获取单例对象
     */
    static DownloadingTaskRepository getInstance() {
        ICache cache = DownloadManagerConfig.cache;
        return cache.isCached(DownloadingTaskRepository.class) ? cache.getCache(DownloadingTaskRepository.class) : cache.cache(new DownloadingTaskRepository());
    }

    /**
     * 当没有下载记录时清除掉已下载好的文件
     *      该情况通常发生于下载中的记录信息结构变更导致软件升级无时无法找到对应的下载文件，而这些无法找到的文件会占用空间
     *      因此需要清除掉这些实际上已经丢失的文件
     *      该方法适合在模块初始化或程序的入口处调用
     */
    void clearDownloadingFileIfNoDownloadRecord() {
        if (downloadTaskMap.size() == 0) {
            FileUtils.delete(DownloadManagerConfig.Config.downloadingDirectory);
        }
    }

    /**
     * 清除掉无效的下载记录
     *      该情况通常发生在用户通过外力删除掉了下载任务对应的文件导致该下载记录没有对应的文件
     *      由于文件删除可能在任何时刻发生，因此该方法适合定时的执行
     * @return 如果有无效的任务且被处理了则返回true
     */
    boolean clearInvalidDownloadRecord() {
        boolean clear = false;

        Iterator<Map.Entry<String, DownloadTask>> iterator = downloadTaskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DownloadTask> next = iterator.next();

            if (!next.getValue().isValid()) {
                iterator.remove();
                clear = true;
            }
        }

        if (clear) {
            updateToCache();
        }

        return clear;
    }

    /**
     * 根据任务信息获取到对应的任务
     *      如果该任务不存在会创建一个新任务
     */
    DownloadTask provideDownloadTaskByDownloadTaskInfo(DownloadTaskInfo downloadTaskInfo) {
        DownloadTask downloadTask = downloadTaskMap.get(downloadTaskInfo.provideUniquelyIdentifies());

        if (downloadTask == null) {
            downloadTask = new DownloadTask(downloadTaskInfo);
            downloadTaskMap.put(downloadTaskInfo.provideUniquelyIdentifies(), downloadTask);
            BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
            updateToCache();
        }

        return downloadTask;
    }

    /**
     * 添加一个下载任务
     */
    void addDownloadTask(DownloadTask downloadTask) {
        downloadTaskMap.put(downloadTask.provideUniquelyIdentifies(), downloadTask);
        updateToCache();
    }

    /**
     * 根据任务ID删除已下载的任务
     */
    void removeDownloadTaskById(String uniquelyId) {
        downloadTaskMap.remove(uniquelyId);
        updateToCache();
    }

    /**
     * 根据任务ID查找到对应的任务
     */
    DownloadTask findDownlaodTaskById(String uniquelyId) {
        return downloadTaskMap.get(uniquelyId);
    }

    /**
     * 提供所有的下载任务集合
     */
    List<DownloadTask> provideAllDownloadingTask() {
        return new ArrayList<>(downloadTaskMap.values());
    }

    /**
     * 获取下载中任务数量
     */
    int getDownloadingTaskCount() {
        int count = 0;

        for (DownloadTask downloadTask : downloadTaskMap.values()) {
            if (downloadTask.isStart()) {
                count ++;
            }
        }

        return count;
    }

    /**
     * 获取下载任务数量
     */
    int getDownloadTaskCount() {
        return downloadTaskMap.size();
    }

    /**
     * 更新仓库到缓存中
     */
    void updateToCache() {
        DownloadManagerConfig.cache.cache(this);
    }
}
