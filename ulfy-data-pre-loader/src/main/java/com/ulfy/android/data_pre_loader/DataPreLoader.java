package com.ulfy.android.data_pre_loader;

/**
 * 数据预加载器
 */
class DataPreLoader<T> {
    private DataPreLoaderManager.PreLoadDataLoader<T> preLoadDataLoader;
    private boolean isLoadSuccess;
    private T data;

    DataPreLoader(DataPreLoaderManager.PreLoadDataLoader<T> preLoadDataLoader) {
        this.preLoadDataLoader = preLoadDataLoader;
    }

    synchronized void loadData() throws Exception {
        try {
            if (!isLoadSuccess) {
                data = preLoadDataLoader.loadData();
                isLoadSuccess = true;
            }
        } catch (Exception e) {
            isLoadSuccess = false;
            throw e;
        }
    }

    boolean isLoadSuccess() {
        return isLoadSuccess;
    }

    T getData() {
        return data;
    }
}
