package com.ulfy.android.cache;

import java.io.Serializable;
import java.util.Map;
import java.util.WeakHashMap;

public final class WeakMemoryCache implements ICache {
    private final Map<String, Object> objectMap = new WeakHashMap<>();

    @Override public synchronized <T extends Serializable> boolean isCached(Class<T> clazz) {
        return objectMap.containsKey(new String(clazz.getName()));
    }

    @Override public synchronized <T extends Serializable> T cache(T object) {
        objectMap.put(new String(object.getClass().getName()), object);
        return object;
    }

    @Override public synchronized <T extends Serializable> T getCache(Class<T> clazz) {
        return (T) objectMap.get(new String(clazz.getName()));
    }

    @Override public synchronized void deleteCache(Class<?> clazz) {
        objectMap.remove(new String(clazz.getName()));
    }

    @Override public synchronized void deleteAllCache() {
        objectMap.clear();
    }
}
