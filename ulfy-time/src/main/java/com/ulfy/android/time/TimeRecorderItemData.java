package com.ulfy.android.time;

import org.joda.time.MonthDay;

import java.io.Serializable;

/**
 * 记录一条时间相关的信息
 *      目前包括当天内和多天两种时间范围，当天内最多只能记录一天的时间且隔天后时间会被重置
 *      多天的不会重置时间，时间会一直累计增长
 */
class TimeRecorderItemData implements Serializable {
    private static final long serialVersionUID = -2301270602722612140L;
    private static final int DAY_SECONDS = 24 * 60 * 60 * 1000;     // 一天总共的毫秒数
    private int recordScope = TimeRecorder.RECORD_SCOPE_TODAY;      // 记录范围，默认只记录当天，隔天重置
    private MonthDay startDay;                                      // 记录第一次记录的日期
    private long currentTime;                                       // 记录当前的时长
    private TimeRecorderItemDataRepository itemDataRepository;      // 持有仓库的应用，用于当数据变化时更新到缓存
    private transient CurrentDayProvider currentDayProvider;        // 当前日期提供器，用于测试的时候便于切换当前时间

    /**
     * 构造方法
     */
    TimeRecorderItemData(TimeRecorderItemDataRepository itemDataRepository) {
        this.itemDataRepository = itemDataRepository;
        startDay = now();
        currentTime = 0;
    }

    /**
     * 重置当前时间记录
     */
    synchronized TimeRecorderItemData initTimeRecord() {
        if (!startDay.equals(now()) || currentTime != 0) {
            startDay = now();
            currentTime = 0;
            itemDataRepository.updateToCache();
        }
        return this;
    }

    /**
     * 设置记录范围
     */
    synchronized TimeRecorderItemData setRecordScope(int recordScope) {
        this.recordScope = recordScope;
        correctTimeIfNeed();
        return this;
    }

    /**
     * 增加以秒为单位的记录
     */
    synchronized TimeRecorderItemData increaseSecond(int seconds) {
        this.currentTime += seconds * 1000;
        itemDataRepository.updateToCache();
        correctTimeIfNeed();
        return this;
    }

    /**
     * 是否到达了指定的秒时间
     */
    synchronized boolean isSecondTimeArrived(long seconds) {
        return currentTime >= seconds * 1000;
    }

    /**
     * 是否到达了指定的分时间
     */
    synchronized boolean isMinuteTimeArrived(long minutes) {
        return isSecondTimeArrived(minutes * 60);
    }

    /**
     * 获取记录的秒数
     */
    synchronized long getRecordSecond() {
        return this.currentTime / 1000;
    }

    /**
     * 如果不是今天的记录则重置为今天的记录
     */
    private void correctTimeIfNeed() {
        if (recordScope == TimeRecorder.RECORD_SCOPE_TODAY) {
            if (!now().equals(startDay)) {
                initTimeRecord();
            } else if (currentTime > DAY_SECONDS) {
                currentTime = DAY_SECONDS;
                itemDataRepository.updateToCache();
            }
        }
    }

    /**
     * 获取当前时间的方法，由于在从缓存中恢复时无法恢复currentDayProvider，因此需要该方法确保一定可以获得
     *      该方法在没有当天提供器的情况下会提供一个默认的实现
     */
    private MonthDay now() {
        if (currentDayProvider == null) {
            currentDayProvider = new CurrentDayProvider() {
                @Override public MonthDay now() {
                    return MonthDay.now();
                }
            };
        }
        return currentDayProvider.now();
    }

    // ------------------- 下面的方法是用于测试使用的，直接获取原始的数据，不会做任何的加工 -------------------------

    MonthDay getStartDay() {
        return startDay;
    }

    long getCurrentTime() {
        return currentTime;
    }

    /**
     * 设置当前时间提供器，用于测试动态切换时间使用
     */
    TimeRecorderItemData setCurrentDayProvider(CurrentDayProvider currentDayProvider) {
        if (currentDayProvider == null) {
            throw new IllegalArgumentException("CurrentDayProvider can not be null");
        }
        this.currentDayProvider = currentDayProvider;
        return this;
    }

    /**
     * 当前时间提供器，用于测试动态切换时间使用
     */
    interface CurrentDayProvider {
        MonthDay now();
    }
}
