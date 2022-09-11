package com.ulfy.android.time;

import android.content.Context;

import com.ulfy.android.cache.CacheConfig;
import com.ulfy.android.cache.ICache;

public final class TimeConfig {
    static ICache cache;                                                           // 用于管理持久化状态的缓存对象

    /**
     * 初始化
     *      不再使用弱引用的二级缓存方案
     */
    static void init(Context context) {
        cache = CacheConfig.newWeakMemoryDiskCache(context, Config.recordInfoCacheDirName);
    }

    public static final class Config {
        public static String recordInfoCacheDirName = "time_cache";                   // 用于跟踪下载信息的缓存目录
    }
}
