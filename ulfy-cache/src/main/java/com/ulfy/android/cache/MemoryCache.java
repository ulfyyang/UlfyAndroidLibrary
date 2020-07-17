package com.ulfy.android.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class MemoryCache implements ICache {
    private final Map<Class<?>, Object> objectMap = new HashMap<>();

    @Override public synchronized <T extends Serializable> boolean isCached(Class<T> clazz) {
        return objectMap.containsKey(clazz);
    }

    @Override public synchronized <T extends Serializable> T cache(T object) {
        objectMap.put(object.getClass(), object);
        return object;
    }

    @Override public synchronized <T extends Serializable> T getCache(Class<T> clazz) {
        return (T) objectMap.get(clazz);
    }

    @Override public synchronized void deleteCache(Class<?> clazz) {
        objectMap.remove(clazz);
    }

    @Override public synchronized void deleteAllCache() {
        objectMap.clear();
    }
}
