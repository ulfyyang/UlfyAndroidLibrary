package com.ulfy.android.cache;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public final class DiskCache implements ICache {
    private File cacheDirPath;         // 缓存的硬盘路径

    public DiskCache(Context context, String dirName) {
        this.cacheDirPath = new File(context.getFilesDir(), dirName);
        if (!cacheDirPath.exists()) {
            cacheDirPath.mkdirs();
        }
    }

    @Override public synchronized <T extends Serializable> boolean isCached(Class<T> clazz) {
//        return getCacheFilePath(clazz).exists();        // 单纯采用文件是否存在的方式判断有误判的情况
        return getCache(clazz) != null;                   // 如果能真正读出来才表示其时存在的
    }

    @Override public synchronized <T extends Serializable> T cache(T object) {
        cacheInner(object, false);
        return object;
    }

    @Override public synchronized <T extends Serializable> T getCache(Class<T> clazz) {
        return getCacheInner(clazz);
    }

    @Override public synchronized void deleteCache(Class<?> clazz) {
        getCacheFilePath(clazz).delete();
    }

    @Override public synchronized void deleteAllCache() {
        if (cacheDirPath.exists()) {
            for (File file : cacheDirPath.listFiles()) {
                file.delete();
            }
        }
    }

    private synchronized void cacheInner(Object object, boolean haveTryed) {
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(getCacheFilePath(object.getClass())));
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
        } catch (Exception e) {
            if (!haveTryed) {
                closeOutputStream(objectOutputStream);
                deleteCache(object.getClass());
                cacheInner(object, true);
            } else {
                e.printStackTrace();
            }
        } finally {
            closeOutputStream(objectOutputStream);
        }
    }

    private synchronized <T> T getCacheInner(Class<T> clazz) {
        File cacheFile = getCacheFilePath(clazz);
        if (cacheFile.exists()) {
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(new FileInputStream(cacheFile));
                return (T) objectInputStream.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                closeInputStream(objectInputStream);
                deleteCache(clazz);
                return null;
            } finally {
                closeInputStream(objectInputStream);
            }
        } else {
            return null;
        }
    }

    private synchronized void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void closeOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getCacheFilePath(Class clazz) {
        return new File(cacheDirPath, clazz.getName());
    }
}
