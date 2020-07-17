package com.ulfy.android.time;

import java.util.HashMap;
import java.util.Map;

/**
 * 时间记录驱动器仓库
 *      用于记录每条记录对应使用的驱动器类型
 */
class TimeRecorderDriverRepository {
    private static final TimeRecorderDriverRepository instance = new TimeRecorderDriverRepository();
    private Map<String, TimeRecorderDriver> timeRecorderDriverMap = new HashMap<>();
    private TimeRecorderDriverProvider timeRecorderDriverProvider;

    private TimeRecorderDriverRepository() {
        timeRecorderDriverProvider = new TimeRecorderDriverProvider() {
            @Override public TimeRecorderDriver getDriver(String key) {
                return new TimeRecorderTimerDriver(key);
            }
        };
    }

    /**
     * 获取实例
     */
    static TimeRecorderDriverRepository getInstance() {
        return instance;
    }

    /**
     * 根据Key获取具体的驱动器
     */
    synchronized TimeRecorderDriver findDriverByKey(String key) {
        if (key == null || key.length() == 0) {
            return null;
        } else {
            TimeRecorderDriver driver = timeRecorderDriverMap.get(key);
            if (driver == null) {
                driver = timeRecorderDriverProvider.getDriver(key);
                timeRecorderDriverMap.put(key, driver);
            }
            return driver;
        }
    }

    /**
     * 设置启动器，用于测试使用
     */
    void setTimeRecorderDriverProvider(TimeRecorderDriverProvider timeRecorderDriverProvider) {
        if (timeRecorderDriverProvider == null) {
            throw new IllegalArgumentException("TimeRecorderDriverProvider can not be null");
        }
        this.timeRecorderDriverProvider = timeRecorderDriverProvider;
    }

    /**
     * 时间记录驱动提供器，用于测试时动态切换驱动器
     */
    interface TimeRecorderDriverProvider {
        TimeRecorderDriver getDriver(String key);
    }
}
