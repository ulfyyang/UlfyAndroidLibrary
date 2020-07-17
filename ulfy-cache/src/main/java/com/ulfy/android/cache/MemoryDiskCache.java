package com.ulfy.android.cache;

import java.io.Serializable;

public final class MemoryDiskCache implements ICache {
    private ICache memoryCache;
    private ICache diskCache;

    public MemoryDiskCache(ICache memoryCache, ICache diskCache) {
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
    }

    @Override public synchronized <T extends Serializable> boolean isCached(Class<T> clazz) {
        return memoryCache.isCached(clazz) || diskCache.isCached(clazz);
    }

    @Override public synchronized <T extends Serializable> T cache(T object) {
        memoryCache.cache(object);
        diskCache.cache(object);
        return object;
    }

    @Override public synchronized <T extends Serializable> T getCache(Class<T> clazz) {
        if (memoryCache.isCached(clazz)) {
            return memoryCache.getCache(clazz);
        } else {
            T object = diskCache.getCache(clazz);
            if (object != null) {
                memoryCache.cache(object);
            }
            return object;
        }
    }

    @Override public synchronized void deleteCache(Class<?> clazz) {
        memoryCache.deleteCache(clazz);
        diskCache.deleteCache(clazz);
    }

    @Override public synchronized void deleteAllCache() {
        memoryCache.deleteAllCache();
        diskCache.deleteAllCache();
    }
}
