package com.ulfy.android.download_manager;

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
 * 已下载任务仓库，用于管理下载任务
 */
final class DownloadedTaskRepository implements Serializable {
    private static final long serialVersionUID = 1442007240440528193L;
    private String downloadManagerId;                                           // 存储仓库所属的下载管理器
    private Map<String, DownloadTask> downloadTaskMap = new LinkedHashMap<>();  // 存储已下载任务

    /**
     * 私有化构造方法
     */
    private DownloadedTaskRepository(String downloadManagerId) {
        this.downloadManagerId = defaultIdIfEmpty(downloadManagerId);
    }

    final static class RepositoryCache implements Serializable {
        private static final long serialVersionUID = -8968385756700217272L;
        Map<String, DownloadedTaskRepository> repositoryMap = new HashMap<>();

        private RepositoryCache() { }

        static RepositoryCache getInstance() {
            ICache cache = DownloadManagerConfig.cache;
            return cache.isCached(RepositoryCache.class) ? cache.getCache(RepositoryCache.class) : cache.cache(new RepositoryCache());
        }
    }

    /**
     * 根据下载管理器ID获取对应的已下载任务仓库
     */
    static DownloadedTaskRepository getInstance(String downloadManagerId) {
        downloadManagerId = defaultIdIfEmpty(downloadManagerId);
        RepositoryCache cache = RepositoryCache.getInstance();
        Map<String, DownloadedTaskRepository> repositoryMap = cache.repositoryMap;
        DownloadedTaskRepository repository = repositoryMap.get(downloadManagerId);
        if (repository == null) {
            repository = new DownloadedTaskRepository(downloadManagerId);
            repositoryMap.put(downloadManagerId, repository);
            DownloadManagerConfig.cache.cache(cache);
        }
        return repository;
    }

    /**
     * 当没有下载记录时清除掉已经下载好的文件（在程序首次运行的时候执行，用于清除损坏的文件）
     *      该情况通常发生于下载的记录信息结构变更导致软件升级时无法找到已经下载过的文件，而这些无法找到的文件会占用空间
     *      因此需要清除掉这些实际上已经丢失的文件
     *      该方法适合在模块初始化或程序的入口处调用
     */
    static void deleteDownloadedFileWithoutRecord() {
        RepositoryCache cache = RepositoryCache.getInstance();
        for (Map.Entry<String, DownloadedTaskRepository> entry : cache.repositoryMap.entrySet()) {
            if (entry.getValue().downloadTaskMap == null || entry.getValue().downloadTaskMap.isEmpty()) {
                FileUtils.delete(DownloadManagerConfig.Config
                        .getDownloadedDirectoryById(entry.getKey()), true);
            }
        }
    }

    /**
     * 清除掉无效的下载任务
     */
    static boolean deleteInvalidRecord() {
        boolean clear = false;
        RepositoryCache cache = RepositoryCache.getInstance();
        for (DownloadedTaskRepository repository : cache.repositoryMap.values()) {
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
     * 清除掉无效的已下载记录（间隔性检查，由DownloadManager调用，当用户手动删除了文件通过该方法清除这些记录）
     *      该情况通常发生在用户通过外力删除掉了下载任务对应的文件导致该下载记录没有对应的文件
     *      由于文件删除可能在任何时刻发生，因此该方法适合定时的执行
     * @return 如果有无效的任务且被处理了则返回true
     */
    private boolean deleteInvalidRecordInner() {
        boolean clear = false;
        Iterator<Map.Entry<String, DownloadTask>> iterator = downloadTaskMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DownloadTask> next = iterator.next();
            if (!next.getValue().isValid()) {
                iterator.remove();
                clear = true;
            }
        }
        return clear;
    }

    /**
     * 添加一个下载任务
     */
    final void addTask(DownloadTask downloadTask) {
        downloadTaskMap.put(downloadTask.provideUniquelyIdentifies(), downloadTask);
        updateToCache();
    }

    /**
     * 根据任务ID删除已下载的任务
     */
    final void removeTaskById(String id) {
        if (downloadTaskMap.remove(id) != null) {
            updateToCache();
        }
    }

    /**
     * 根据任务ID找到对应的任务
     */
    final DownloadTask findTaskById(String id) {
        return downloadTaskMap.get(id);
    }

    /**
     * 获取全部的下载任务列表
     */
    final List<DownloadTask> getAllTask() {
        return new ArrayList<>(downloadTaskMap.values());
    }

    /**
     * 获取已下载任务的数量
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
