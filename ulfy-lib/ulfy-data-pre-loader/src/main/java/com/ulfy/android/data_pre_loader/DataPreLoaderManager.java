package com.ulfy.android.data_pre_loader;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * 数据预加载管理器
 */
public final class DataPreLoaderManager {
    private static final DataPreLoaderManager instance = new DataPreLoaderManager();                                // 单例对象
    private Map<Class<? extends PreLoadDataLoader>, DataPreLoader> preDataLoaderMap = new WeakHashMap<>();          // 保存每份预加载器

    /**
     * 禁止直接构造
     */
    private DataPreLoaderManager() { }

    /**
     * 获取预加载管理器实例
     */
    public static DataPreLoaderManager getInstance() {
        return instance;
    }

    /**
     * 根据预加载数据类型加载需要加载的预加载数据
     *      当获取数据之后会使预加载的数据失效，下载加载会重新加载
     *      如果存在预加载数据则会进行适当的延迟
     * @param clazz 预加载数据类型
     */
    public <T> T loadDataDelayThenInvalidate(Class<? extends PreLoadDataLoader<T>> clazz) throws Exception {
        if (isLoadDataSuccess(clazz)) {
            Thread.sleep(Config.loadDataDelayTime);
        }
        return loadDataThenInvalidate(clazz);
    }

    /**
     * 根据预加载数据类型加载需要加载的预加载数据
     *      当获取数据之后会使预加载的数据失效，下载加载会重新加载
     * @param clazz 预加载数据类型
     */
    public <T> T loadDataThenInvalidate(Class<? extends PreLoadDataLoader<T>> clazz) throws Exception {
        T data = loadData(clazz);
        invalidate(clazz);
        return data;
    }

    /**
     * 根据预加载数据类型加载需要加载的预加载数据
     * @param clazz 预加载数据类型
     */
    public <T> T loadData(Class<? extends PreLoadDataLoader<T>> clazz) throws Exception {
        DataPreLoader<T> dataPreLoader = findPreDataLoaderByPreLoadData(clazz);
        dataPreLoader.loadData();
        return dataPreLoader.getData();
    }

    /**
     * 数据是否已经加载完毕了
     */
    public boolean isLoadDataSuccess(Class<? extends PreLoadDataLoader> clazz) {
        try {
            return findPreDataLoaderByPreLoadData(clazz).isLoadSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 使预加载的数据失效，下次会重新加载
     */
    public void invalidate(Class<? extends PreLoadDataLoader> clazz) {
        preDataLoaderMap.remove(clazz);
    }

    /**
     * 根据预加载数据类型获取该类型数据的预加载器
     * @param clazz 预加载数据类型
     */
    private synchronized DataPreLoader findPreDataLoaderByPreLoadData(Class<? extends PreLoadDataLoader> clazz) throws Exception {
        DataPreLoader dataPreLoader = preDataLoaderMap.get(clazz);
        if (dataPreLoader == null) {
            dataPreLoader = new DataPreLoader(clazz.getConstructor().newInstance());
            preDataLoaderMap.put(clazz, dataPreLoader);
        }
        return dataPreLoader;
    }

    /**
     * 预加载数据预加载流程接口
     *      客户端通过实现该接口来指定具体的加载流程
     * @param <T> 需要被加载的数据
     */
    public interface PreLoadDataLoader<T> {
        /**
         * 真正的加载数据方法
         * @return  加载完数据以后返回加载完整的数据
         */
        public T loadData() throws Exception;
    }

    public static class Config {
        public static int loadDataDelayTime = 100;          // 当使用延迟加载时演示的时间（毫秒）
    }
}
