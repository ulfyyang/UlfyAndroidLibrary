package com.ulfy.android.data_pre_loader;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataPreLoadTest {

    public static class UserPreLoadData1 implements DataPreLoaderManager.PreLoadDataLoader<UserPreLoadData1> {
        public User user;
        @Override public UserPreLoadData1 loadData() throws Exception {
            Thread.sleep(1000);
            user = new User();
            user.content = "content";
            return this;
        }
    }

    public static class UserPreLoadData2 implements DataPreLoaderManager.PreLoadDataLoader<UserPreLoadData2> {
        public User user;
        @Override public UserPreLoadData2 loadData() throws Exception {
            Thread.sleep(2000);
            user = new User();
            user.content = "content";
            return this;
        }
    }

    @Before public void resetDataPreLoadManager() {
        DataPreLoaderManager.getInstance().invalidate(UserPreLoadData1.class);
        DataPreLoaderManager.getInstance().invalidate(UserPreLoadData2.class);
    }

    /**
     * 可用性测试
     *      在后台线程预加载，在当前线程等待预加载的数据
     */
    @Test public void testNormalDataPreLoad() throws Exception {
        startDataPreLoad1InBackground();
        // 数据还在后台加载，现在没有加载成功
        assertFalse(DataPreLoaderManager.getInstance().isLoadDataSuccess(UserPreLoadData1.class));
        // 等待加载完成，这里会阻塞线程
        UserPreLoadData1 userPreLoadData = DataPreLoaderManager.getInstance().loadData(UserPreLoadData1.class);
        // 加载完成后进行基本状态检查
        assertTrue(DataPreLoaderManager.getInstance().isLoadDataSuccess(UserPreLoadData1.class));
        // 数据正确性判定
        assertNotNull(userPreLoadData);
        assertNotNull(userPreLoadData.user);
        assertEquals("content", userPreLoadData.user.content);
    }

    /**
     * 测试预加载完数据后使其失效
     */
    @Test public void testDataPreLoadThenInvalidate() throws Exception {
        startDataPreLoad1InBackground();
        // 等待加载完成，这里会阻塞线程
        DataPreLoaderManager.getInstance().loadDataThenInvalidate(UserPreLoadData1.class);
        // 加载完成失效后进行基本状态检查
        assertFalse(DataPreLoaderManager.getInstance().isLoadDataSuccess(UserPreLoadData1.class));
    }

    /**
     * 测试多个数据异步预加载
     *      最大的预加载时间为2000，加上线程执行上的时间误差不超过100毫秒
     */
    @Test public void testAsynchronizeDataPreLoad() throws Exception {
        long startTime = System.currentTimeMillis();
        startDataPreLoad1InBackground();
        startDataPreLoad2InBackground();
        DataPreLoaderManager.getInstance().loadData(UserPreLoadData1.class);
        DataPreLoaderManager.getInstance().loadData(UserPreLoadData2.class);
        long endTime = System.currentTimeMillis();
        assertTrue(endTime - startTime < 2100);
    }

    /**
     * 在后台线程中预加载数据
     */
    private void startDataPreLoad1InBackground() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    DataPreLoaderManager.getInstance().loadData(UserPreLoadData1.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 在后台线程中预加载数据
     */
    private void startDataPreLoad2InBackground() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    DataPreLoaderManager.getInstance().loadData(UserPreLoadData2.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
