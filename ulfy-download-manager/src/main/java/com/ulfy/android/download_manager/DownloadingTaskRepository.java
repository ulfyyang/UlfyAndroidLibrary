package com.ulfy.android.download_manager;

import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.cache.ICache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ulfy.android.download_manager.DownloadManagerConfig.defaultIdIfEmpty;

/**
 * 下载任务仓库，用于管理下载任务
 */
final class DownloadingTaskRepository implements Serializable {
    private static final long serialVersionUID = 185632873962007026L;
    private String downloadManagerId;                                           // 存储仓库所属的下载管理器
    private Map<String, DownloadTaskWrapper> downloadTaskMap = new LinkedHashMap<>();  // 存储下载任务（采用LinkedHashMap是为了保持添加的顺序）

    /**
     * 私有化构造方法
     */
    private DownloadingTaskRepository(String downloadManagerId) {
        this.downloadManagerId = defaultIdIfEmpty(downloadManagerId);
    }

    final static class RepositoryCache implements Serializable {
        private static final long serialVersionUID = 5092917938191265468L;
        Map<String, DownloadingTaskRepository> repositoryMap = new HashMap<>();

        private RepositoryCache() { }

        static RepositoryCache getInstance() {
            ICache cache = DownloadManagerConfig.cache;
            return cache.isCached(RepositoryCache.class) ? cache.getCache(RepositoryCache.class) : cache.cache(new RepositoryCache());
        }
    }

    /**
     * 根据下载管理器ID获取对应的下载中任务仓库
     */
    static DownloadingTaskRepository getInstance(String downloadManagerId) {
        downloadManagerId = defaultIdIfEmpty(downloadManagerId);
        RepositoryCache cache = RepositoryCache.getInstance();
        Map<String, DownloadingTaskRepository> repositoryMap = cache.repositoryMap;
        DownloadingTaskRepository repository = repositoryMap.get(downloadManagerId);
        if (repository == null) {
            repository = new DownloadingTaskRepository(downloadManagerId);
            repositoryMap.put(downloadManagerId, repository);
            DownloadManagerConfig.cache.cache(cache);
        }
        return repository;
    }

    /**
     * 删除掉没有下载记录的文件（在程序首次运行的时候执行，用于清除损坏的文件）
     *      该情况通常发生于下载中的记录信息结构变更导致软件升级无时无法找到对应的下载文件，而这些无法找到的文件会占用空间
     *      因此需要清除掉这些实际上已经丢失的文件
     *      该方法适合在模块初始化或程序的入口处调用
     */
    static void deleteDownloadingFileWithoutRecord() {
        RepositoryCache cache = RepositoryCache.getInstance();
        for (Map.Entry<String, DownloadingTaskRepository> entry : cache.repositoryMap.entrySet()) {
            if (entry.getValue().downloadTaskMap == null || entry.getValue().downloadTaskMap.isEmpty()) {
                FileUtils.delete(DownloadManagerConfig.Config
                        .getDownloadingDirectoryById(entry.getKey()), true);
            }
        }
    }

    /**
     * 清除掉所有的无效下载中记录
     */
    static boolean deleteInvalidRecord() {
        boolean clear = false;
        RepositoryCache cache = RepositoryCache.getInstance();
        for (DownloadingTaskRepository repository : cache.repositoryMap.values()) {
            if (repository.deleteInvalidRecordInner()) {
                clear = true;
            }
        }
        if (clear) {
            DownloadManagerConfig.cache.cache(cache);
        }
        return clear;
    }

    /**
     * 清除掉无效的下载记录（间隔性检查，由DownloadManager调用，当用户手动删除了文件通过该方法清除这些记录）
     *      该情况通常发生在用户通过外力删除掉了下载任务对应的文件导致该下载记录没有对应的文件
     *      由于文件删除可能在任何时刻发生，因此该方法适合定时的执行
     * @return 如果有无效的任务且被处理了则返回true
     */
    private boolean deleteInvalidRecordInner() {
        boolean clear = false;
        Iterator<Map.Entry<String, DownloadTaskWrapper>> iterator = downloadTaskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DownloadTaskWrapper> next = iterator.next();
            if (!next.getValue().isValid()) {
                iterator.remove();
                clear = true;
            }
        }
        return clear;
    }

    /**
     * 根据任务信息获取到对应的任务，如果该任务不存在会创建一个新任务
     */
    final DownloadTaskWrapper getDownloadTaskByTaskInfo(DownloadTaskInfo downloadTaskInfo) {
        DownloadTaskWrapper downloadTask = downloadTaskMap.get(downloadTaskInfo.getUniquelyIdentifies());
        if (downloadTask == null) {
            downloadTask = new DownloadTaskWrapper(downloadManagerId, downloadTaskInfo);
            downloadTaskMap.put(downloadTaskInfo.getUniquelyIdentifies(), downloadTask);
            BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
            updateToCache();
        }
        return downloadTask;
    }

    /**
     * 根据任务ID删除已下载的任务
     */
    final void removeTaskByIdThenUpdateToCache(String id) {
        if (downloadTaskMap.remove(id) != null) {
            updateToCache();
        }
    }

    final void removeTaskByTaskListThenUpdateToCache(List<DownloadTaskWrapper> downloadTaskList) {
        boolean remove = false;
        for (DownloadTaskWrapper downloadTask : downloadTaskList) {
            if (downloadTaskMap.remove(downloadTask.provideUniquelyIdentifies()) != null) {
                remove = true;
            }
        }
        if (remove) {
            updateToCache();
        }
    }

    /**
     * 根据任务ID查找到对应的任务
     */
    final DownloadTaskWrapper findTaskById(String id) {
        return downloadTaskMap.get(id);
    }

    /**
     * 提供所有的下载任务集合
     */
    final List<DownloadTaskWrapper> getAllTask() {
        return new ArrayList<>(downloadTaskMap.values());
    }

    /**
     * 获取下载中任务数量
     */
    final int getDownloadingTaskCount() {
        int count = 0;
        for (DownloadTaskWrapper downloadTask : downloadTaskMap.values()) {
            if (downloadTask.isStart()) {
                count ++;
            }
        }
        return count;
    }

    /**
     * 获取下载任务数量
     */
    final int getTaskCount() {
        return downloadTaskMap.size();
    }

    /**
     * 更新仓库到缓存中
     */
    final void updateToCache() {
        RepositoryCache cache = RepositoryCache.getInstance();
        cache.repositoryMap.put(defaultIdIfEmpty(downloadManagerId), this);
        DownloadManagerConfig.cache.cache(cache);
    }
}
