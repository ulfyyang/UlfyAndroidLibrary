package com.ulfy.android.time;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TimeRecorderDriverRepositoryTest {

    /**
     * 初始化时间记录驱动提供器，便于解耦安卓相关的依赖
     */
    @Before public void initTimeRecorderDriverProvider() {
        TimeRecorderDriverRepository.getInstance().setTimeRecorderDriverProvider(new TimeRecorderDriverRepository.TimeRecorderDriverProvider() {
            @Override public TimeRecorderDriver getDriver(String key) {
                return new EmptyTimeRecorderDriverProvider(key);
            }
        });
    }

    public static class EmptyTimeRecorderDriverProvider extends TimeRecorderDriver {
        public EmptyTimeRecorderDriverProvider(String key) {
            super(key);
        }
        @Override void startDrive() { }
        @Override void stopDrive() { }
    }

    /**
     * 常规的使用测试
     */
    @Test public void testTimeRecorderData() {
        assertNull(TimeRecorderDriverRepository.getInstance().findDriverByKey(null));
        assertNull(TimeRecorderDriverRepository.getInstance().findDriverByKey(""));
        assertNotNull(TimeRecorderDriverRepository.getInstance().findDriverByKey("key"));
    }

}