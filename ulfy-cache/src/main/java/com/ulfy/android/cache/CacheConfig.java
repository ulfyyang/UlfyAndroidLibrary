package com.ulfy.android.cache;

import android.content.Context;

public final class CacheConfig {
    static ICache defaultMemoryCache;
    static ICache defaultWeakMemoryCache;
    static ICache defaultDiskCache;
    static ICache defaultMemoryDiskCache;

    /*
    =============================== 提供一个默认的主缓存，用于全局使用 ================================
     */

    /**
     * 初始化主缓存
     *      如果使用默认的缓存，则必须在Application中初始化
     */
    static void initDefaultCache(Context context) {
        defaultMemoryCache = new MemoryCache();
        defaultWeakMemoryCache = new WeakMemoryCache();
        defaultDiskCache = new DiskCache(context, Config.recordInfoCacheDirName);
        defaultMemoryDiskCache = new MemoryDiskCache(defaultWeakMemoryCache, defaultDiskCache);
    }

    /**
     * 获取默认的内存缓存
     */
    public static ICache newMemoryCache() {
        return new MemoryCache();
    }

    /**
     * 获取默认的弱引用内存缓存
     */
    public static ICache newWeakMemoryCache() {
        return new WeakMemoryCache();
    }

    /**
     * 获取默认的硬盘缓存
     */
    public static ICache newDiskCache(Context context, String dirName) {
        return new DiskCache(context, dirName);
    }

    /**
     * 获取内存硬盘双重缓存
     */
    public static ICache newMemoryDiskCache(Context context, String dirName) {
        return new MemoryDiskCache(new MemoryCache(), new DiskCache(context, dirName));
    }

    /**
     * 获取内存硬盘双重缓存
     */
    public static ICache newWeakMemoryDiskCache(Context context, String dirName) {
        return new MemoryDiskCache(new WeakMemoryCache(), new DiskCache(context, dirName));
    }

    public static final class Config {
        public static String recordInfoCacheDirName = "local_entity_cache";                         // 用于跟踪下载信息的缓存目录
    }
}
