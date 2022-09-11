package com.ulfy.android.download_manager;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.arialyy.aria.core.Aria;
import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.cache.CacheConfig;
import com.ulfy.android.cache.ICache;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class DownloadManagerConfig {
    static Application context; static ICache cache;
    public static final String DEFAULT_DOWNLOAD_MANAGER_ID = "ULFY_DEFAULT_DOWNLOAD_MANAGER_ID";

    static String defaultIdIfEmpty(String downloadManagerId) {      // 为了保证从序列化恢复的正确性，所有需要用到管理器ID的地方都要用该方法矫正
        if (TextUtils.isEmpty(downloadManagerId)) {
            downloadManagerId = DEFAULT_DOWNLOAD_MANAGER_ID;
        }
        return downloadManagerId;
    }

    /**
     * 初始化下载任务模块
     */
    static void init(Application context) {
            DownloadManagerConfig.context = context;

            Aria.init(context);
            Aria.get(context).getDownloadConfig().setMaxTaskNum(Integer.MAX_VALUE);

            cache = CacheConfig.newMemoryDiskCache(context, Config.recordInfoCacheDirName);

            Config.init(context);
            DownloadLimitConfig.initLimitCount(Config.limitCount);
            DownloadingTaskRepository.deleteDownloadingFileWithoutRecord();
            DownloadedTaskRepository.deleteDownloadedFileWithoutRecord();

            BusUtils.post(new DownloadManager.OnDownloadManagerStateChangeEvent());
    }

    /**
     * 通用配置
     */
    public static final class Config {
        public static DirectoryConfig directoryConfig = new DefaultDirectoryConfig();                   // 目录配置接口。如果配置外部存储必须给予读写权限
        public static String recordInfoCacheDirName = "download_manager_cache";                         // 用于跟踪下载信息的缓存目录
        public static boolean startWaitingFirst = true;                                                 // 优先开启等待中任务
        public static Map<String, Integer> limitCount = new HashMap<>();                                // 默认的同时下载数量限制。没有表示无限制
        public static String statusStringStart = "下载中";                                               // 任务开始状态显示的文字（调用DownloadTask.getStatusString()的配置）
        public static String statusStringPause = "已暂停";                                               // 任务暂停状态显示的文字（调用DownloadTask.getStatusString()的配置）
        public static String statusStringWaiting = "等待中";                                             // 任务等待状态显示的文字（调用DownloadTask.getStatusString()的配置）
        public static String statusStringComplete = "已完成";                                            // 任务完成状态显示的文字（调用DownloadTask.getStatusString()的配置）
        static File downloadingDirectory;                                                               // 下载中文件目录
        static File downloadedDirectory;                                                                // 下载完成文件目录

        /**
         * 初始化方法
         */
        private static void init(Context context) {
            downloadingDirectory = directoryConfig.getDownloadingDirectory(context);
            if (!downloadingDirectory.exists()) {
                downloadingDirectory.mkdirs();
            }
            downloadedDirectory = directoryConfig.getDownloadedDirectory(context);
            if (!downloadedDirectory.exists()) {
                downloadedDirectory.mkdirs();
            }
        }

        static File getDownloadingDirectoryById(String id) {
            id = defaultIdIfEmpty(id);
            File directory = new File(downloadingDirectory, id);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            return directory;
        }

        static File getDownloadedDirectoryById(String id) {
            id = defaultIdIfEmpty(id);
            File directory = new File(downloadedDirectory, id);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            return directory;
        }

        /**
         * 目录配置
         *      下载中和已下载目录尽量放在统一级别下，如都在内部存储或外部存储。
         *      由于内部存储和外部存储的目录权限不同，直接重命名会失败
         *      配置外部存储目录需要Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE权限
         */
        public interface DirectoryConfig {
            /**
             * 下载中目录配置
             *      目录配置应当采用一个子目录用来单独管理，当任务追踪失败的时候会清空该目录下的内容
             */
            public File getDownloadingDirectory(Context context);
            /**
             * 下载完成目录配置
             *      目录配置应当采用一个子目录用来单独管理，当任务追踪失败的时候会清空该目录下的内容
             */
            public File getDownloadedDirectory(Context context);
        }

        /**
         * 默认目录配置
         */
        public static class DefaultDirectoryConfig implements DirectoryConfig {
            @Override public File getDownloadingDirectory(Context context) {
                return new File(context.getCacheDir(), "downloading");
            }
            @Override public File getDownloadedDirectory(Context context) {
                return new File(context.getFilesDir(), "downloaded");
            }
        }
    }

    /**
     * 同时下载数量限制配置
     *      该类的实例不能只能在DownloadManager初始化之后使用，该类设计的目的是为了在运行中能动态该设置下载限制
     */
    public static class DownloadLimitConfig implements Serializable {
        private static final long serialVersionUID = 2180202361490555482L;
        private String downloadManagerId;                                           // 存储仓库所属的下载管理器
        private int limitCount;             // 同时下载数量
        private boolean modifyByUser;       // 是否被用户修改过。被用户修改过以后就会

        /**
         * 私有化构造方法
         */
        private DownloadLimitConfig(String downloadManagerId) {
            this.downloadManagerId = defaultIdIfEmpty(downloadManagerId);
        }

        final static class LimitCache implements Serializable {
            private static final long serialVersionUID = -1168591889508822040L;
            Map<String, DownloadLimitConfig> limitMap = new HashMap<>();

            private LimitCache() { }

            static LimitCache getInstance() {
                ICache cache = DownloadManagerConfig.cache;
                return cache.isCached(LimitCache.class) ? cache.getCache(LimitCache.class) : cache.cache(new LimitCache());
            }
        }

        /**
         * 获取实例
         */
        public static DownloadLimitConfig getInstance() {
            return getInstance(DEFAULT_DOWNLOAD_MANAGER_ID);
        }

        /**
         * 获取实例
         */
        public static DownloadLimitConfig getInstance(String downloadManagerId) {
            downloadManagerId = defaultIdIfEmpty(downloadManagerId);
            LimitCache cache = LimitCache.getInstance();
            Map<String, DownloadLimitConfig> limitMap = cache.limitMap;
            DownloadLimitConfig config = limitMap.get(downloadManagerId);
            if (config == null) {
                config = new DownloadLimitConfig(downloadManagerId);
                limitMap.put(downloadManagerId, config);
                DownloadManagerConfig.cache.cache(cache);
            }
            return config;
        }

        static void initLimitCount(Map<String, Integer> limitCountMap) {
            boolean modify = false;
            LimitCache cache = LimitCache.getInstance();
            for (Map.Entry<String, Integer> entry : limitCountMap.entrySet()) {
                String id = defaultIdIfEmpty(entry.getKey());
                if (DownloadLimitConfig.getInstance(id).initLimitCountInner(entry.getValue())) {
                    modify = true;
                }
            }
            if (modify) {
                DownloadManagerConfig.cache.cache(cache);
            }
        }

        /**
         * 初始化同时下载数量
         *      该方法将使用Config配置的值
         *      当用户调用过updateLimitCount方法后该方法将不会再生效
         * @return 是否修改了
         */
        private boolean initLimitCountInner(int limitCount) {
            if (!modifyByUser) {
                this.limitCount = limitCount;
                return true;
            } else {
                return false;
            }
        }

        /**
         * 更新同时下载数量
         *      0 表示无限制
         */
        public void updateLimitCount(int limitCount) {
            if (this.limitCount != limitCount) {
                this.limitCount = limitCount;
                this.modifyByUser = true;
                updateToCache();
                DownloadManager.getInstance().reScheduleDownloadTaskStatus();
            }
        }

        /**
         * 获取同时下载数量
         */
        int getLimitCount() {
            return limitCount;
        }

        /**
         * 是否可以开启下载任务
         */
        boolean canStartDownloadTask() {
            return limitCount <= 0 || DownloadingTaskRepository
                    .getInstance(defaultIdIfEmpty(downloadManagerId)).getDownloadingTaskCount() < limitCount;
        }

        /**
         * 更新到缓存
         */
        private void updateToCache() {
            LimitCache cache = LimitCache.getInstance();
            cache.limitMap.put(defaultIdIfEmpty(downloadManagerId), this);
            DownloadManagerConfig.cache.cache(cache);
        }
    }
}
