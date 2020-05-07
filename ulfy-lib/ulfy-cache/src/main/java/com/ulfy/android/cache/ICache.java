package com.ulfy.android.cache;

import java.io.Serializable;

public interface ICache {
    public <T extends Serializable> boolean isCached(Class<T> clazz);
    public <T extends Serializable> T cache(T object);
    public <T extends Serializable> T getCache(Class<T> clazz);
    public void deleteCache(Class<?> clazz);
    public void deleteAllCache();
}
