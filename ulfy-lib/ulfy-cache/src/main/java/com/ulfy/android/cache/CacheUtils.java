package com.ulfy.android.cache;

import java.io.Serializable;


/**
 * 缓存工具类
 *      默认使用的内存硬盘双重缓存
 */
public final class CacheUtils {

    /**
     * 判断一个对象是否被缓存了
     */
    public static synchronized <T extends Serializable> boolean isCached(Class<T> clazz) {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        return CacheConfig.defaultMemoryDiskCache.isCached(clazz);
    }

    /**
     * 缓存一个对象
     */
    public static synchronized <T extends Serializable> T cache(T object) {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        return CacheConfig.defaultMemoryDiskCache.cache(object);
    }

    /**
     * 取出一个缓存对象
     */
    public static synchronized <T extends Serializable> T getCache(Class<T> clazz) {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        return CacheConfig.defaultMemoryDiskCache.getCache(clazz);
    }

    /**
     * 删除缓存对象
     */
    public static synchronized void deleteCache(Class<?> clazz) {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        CacheConfig.defaultMemoryDiskCache.deleteCache(clazz);
    }

    /**
     * 删除所有缓存对象
     */
    public static synchronized void deleteAllCache() {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        CacheConfig.defaultMemoryDiskCache.deleteAllCache();
    }

    /**
     * 获取默认的内存缓存
     */
    public static ICache defaultMemoryCache() {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        return CacheConfig.defaultMemoryCache;
    }

    /**
     * 获取默认的弱引用内存缓存
     */
    public static ICache defaultWeakMemoryCache() {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        return CacheConfig.defaultWeakMemoryCache;
    }

    /**
     * 获取默认的硬盘缓存
     */
    public static ICache defaultDiskCache() {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        return CacheConfig.defaultDiskCache;
    }

    /**
     * 获取内存硬盘双重缓存
     */
    public static ICache defaultMemoryDiskCache() {
        CacheConfig.throwExceptionIfDefaultConfigNotConfigured();
        return CacheConfig.defaultMemoryDiskCache;
    }

}
