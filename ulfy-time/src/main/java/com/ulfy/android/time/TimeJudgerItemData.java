package com.ulfy.android.time;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.MonthDay;
import org.joda.time.Period;

import java.io.Serializable;

/**
 * 时间判定器单项数据
 */
class TimeJudgerItemData implements Serializable {
    private static final long serialVersionUID = 4555368611943513585L;
    private long startTime;                                         // 记录第一次记录的时间。当没有记录的时候说明其时长无限长，则肯定不在固定的时段内
    private TimeJudgerItemDataRepository itemDataRepository;        // 持有仓库的应用，用于当数据变化时更新到缓存
    private transient CurrentDayProvider currentDayProvider;        // 当前日期提供器，用于测试的时候便于切换当前时间

    /**
     * 构造方法
     */
    TimeJudgerItemData(TimeJudgerItemDataRepository itemDataRepository) {
        this.itemDataRepository = itemDataRepository;
        startTime = 0;
    }

    /**
     * 重置当前时间记录为当前的时间
     *      如果不重置则表示任意时间，任意的时间表示不在任意的时间区间内
     */
    synchronized TimeJudgerItemData initTimeJudger() {
        startTime = System.currentTimeMillis();
        itemDataRepository.updateToCache();
        return this;
    }

    /**
     * 是否在指定的天数之内
     *      如果正好等于指定的天数则不表示在其内
     *      天数是按照日期计算的，只要日期不在同一天则表示不同的天数，计时不够24小时也会被算作一天
     */
    synchronized boolean isInDays(int days) {
        if (startTime == 0) {
            return false;
        } else {
            return new Period(new MonthDay(startTime), nowDay()).getDays() < days;
        }
    }

    /**
     * 是否在指定的小时数之内
     *      如果正好等于指定的小时数则不表示在其内
     *      小时是按照具体的小时个数计算的，如1天有24个小时
     */
    synchronized boolean isInHours(int hours) {
        if (startTime == 0) {
            return false;
        } else {
            return new Duration(new DateTime(startTime), nowTime()).getStandardHours() < hours;
        }
    }

    /**
     * 获取当前时间的方法，由于在从缓存中恢复时无法恢复currentDayProvider，因此需要该方法确保一定可以获得
     *      该方法在没有当天提供器的情况下会提供一个默认的实现
     */
    private MonthDay nowDay() {
        if (currentDayProvider == null) {
            currentDayProvider = new DefaultCurrentDayProvider();
        }
        return currentDayProvider.nowDay();
    }

    private DateTime nowTime() {
        if (currentDayProvider == null) {
            currentDayProvider = new DefaultCurrentDayProvider();
        }
        return currentDayProvider.nowTime();
    }

    // ------------------- 下面的方法是用于测试使用的，直接获取原始的数据，不会做任何的加工 -------------------------

    /**
     * 设置当前时间提供器，用于测试动态切换时间使用
     */
    TimeJudgerItemData setCurrentDayProvider(CurrentDayProvider currentDayProvider) {
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
        MonthDay nowDay();
        DateTime nowTime();
    }

    static class DefaultCurrentDayProvider implements CurrentDayProvider {
        @Override public MonthDay nowDay() {
            return MonthDay.now();
        }
        @Override public DateTime nowTime() {
            return DateTime.now();
        }
    }
}