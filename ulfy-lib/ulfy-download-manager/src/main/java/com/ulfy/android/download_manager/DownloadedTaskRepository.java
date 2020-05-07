package com.ulfy.android.download_manager;

import com.ulfy.android.cache.ICache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 已下载的任务仓库
 */
class DownloadedTaskRepository implements Serializable {
    private static final long serialVersionUID = 1442007240440528193L;
    private Map<String, DownloadTask> downloadTaskMap = new LinkedHashMap<>();              // 存储已下载任务

    /**
     * 私有化构造方法
     */
    private DownloadedTaskRepository() { }

    /**
     * 获取单例对象
     */
    static DownloadedTaskRepository getInstance() {
        ICache cache = DownloadManagerConfig.cache;
        return cache.isCached(DownloadedTaskRepository.class) ? cache.getCache(DownloadedTaskRepository.class) : cache.cache(new DownloadedTaskRepository());
    }

    /**
     * 当没有下载记录时清除掉已经下载好的文件
     *      该情况通常发生于下载的记录信息结构变更导致软件升级时无法找到已经下载过的文件，而这些无法找到的文件会占用空间
     *      因此需要清除掉这些实际上已经丢失的文件
     *      该方法适合在模块初始化或程序的入口处调用
     */
    void clearDownloadedFileIfNoDownloadRecord() {
        if (downloadTaskMap.size() == 0) {
            FileUtils.delete(DownloadManagerConfig.Config.downloadedDirectory);
        }
    }

    /**
     * 清除掉无效的已下载记录
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
     * 根据任务ID找到对应的任务
     */
    DownloadTask findDownlaodTaskById(String uniquelyId) {
        return downloadTaskMap.get(uniquelyId);
    }

    /**
     * 获取全部的下载任务列表
     */
    List<DownloadTask> provideAllDownloadedTask() {
        return new ArrayList<>(downloadTaskMap.values());
    }

    /**
     * 获取已下载任务的数量
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
