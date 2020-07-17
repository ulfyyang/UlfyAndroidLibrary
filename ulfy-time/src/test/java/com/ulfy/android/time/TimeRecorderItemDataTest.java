package com.ulfy.android.time;

import org.joda.time.MonthDay;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeRecorderItemDataTest {
    public TimeRecorderItemDataRepository itemDataRepository;

    @Before public void initItemDataRepository() {
        itemDataRepository = Mockito.mock(TimeRecorderItemDataRepository.class);
    }

    /*
    =============================== 方法测试 =====================================
     */

    /**
     * 构造方法测试
     *      构造方法不触发更新到缓存
     */
    @Test public void testConstructerUpdateToCache() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);
        Mockito.verify(itemDataRepository, Mockito.never()).updateToCache();
    }

    /**
     * 重置当前时间记录方法测试
     *      初始状态下重置不触发更新到缓存
     */
    @Test public void testInitTimeRecordUpdateToCacheWhenInit() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.initTimeRecord();

        Mockito.verify(itemDataRepository, Mockito.never()).updateToCache();
    }

    /**
     * 重置当前时间记录方法测试
     *      在不同日期下要触发更新到缓存
     */
    @Test public void testInitTimeRecordUpdateToCacheWhenInitOnDifferentDate() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.setCurrentDayProvider(new TimeRecorderItemData.CurrentDayProvider() {
            @Override public MonthDay now() {
                return MonthDay.now().plusDays(1);
            }
        });
        recorder.initTimeRecord();

        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();
    }

    /**
     * 重置当前时间记录方法测试
     *      在不同时间区间下要触发更新到缓存
     */
    @Test public void testInitTimeRecordUpdateToCacheWhenInitOnDifferrentTime() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.increaseSecond(1);
        Mockito.reset(itemDataRepository);
        recorder.initTimeRecord();

        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();
    }

    /**
     * 重置当前时间记录方法测试
     *      在不同日期不同时间区间下要触发更新到缓存
     */
    @Test public void testInitTimeRecordUpdateToCacheWhenInitOnDifferentDateAndTime() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        // 这里要先增加时间区间在增加日期，否则会因为日期不同而导致增加时间的时候会矫正状态
        recorder.increaseSecond(1);
        recorder.setCurrentDayProvider(new TimeRecorderItemData.CurrentDayProvider() {
            @Override public MonthDay now() {
                return MonthDay.now().plusDays(1);
            }
        });
        Mockito.reset(itemDataRepository);
        recorder.initTimeRecord();

        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();
    }

    /**
     * 测试记录范围变更方法
     *      日期不变时从一天切换到多天不会矫正状态
     */
    @Test public void testSetRecorderScopeFormTodayToAlways() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.increaseSecond(1);
        MonthDay startDay = recorder.getStartDay();
        long currentTime = recorder.getCurrentTime();

        // 更改记录范围不修改状态因此不触发更新到缓存
        Mockito.reset(itemDataRepository);
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);

        Mockito.verify(itemDataRepository, Mockito.never()).updateToCache();
        assertEquals(startDay, recorder.getStartDay());
        assertEquals(currentTime, recorder.getCurrentTime());
    }

    /**
     * 测试记录范围变更方法
     *      日期不变从多天切换到一天在一天内的时间保持状态不变
     *      日期不变从多天切换到一天要确保记录的时间不可超过一天的范围
     */
    @Test public void testSetRecorderScopeFromAlwaysToToday() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);
        MonthDay startDay = recorder.getStartDay();

        // 增加半天，切换到天记录范围进行判定
        int halfDaySeconds = 12 * 60 * 60;
        recorder.increaseSecond(halfDaySeconds);
        Mockito.reset(itemDataRepository);
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_TODAY);
        Mockito.verify(itemDataRepository, Mockito.never()).updateToCache();;
        assertEquals(startDay, recorder.getStartDay());
        assertEquals(halfDaySeconds, recorder.getCurrentTime() / 1000);

        // 切回多天范围，增加半天，切换到天记录范围进行判定
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);
        recorder.increaseSecond(halfDaySeconds);
        Mockito.reset(itemDataRepository);
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_TODAY);
        Mockito.verify(itemDataRepository, Mockito.never()).updateToCache();;
        assertEquals(startDay, recorder.getStartDay());
        assertEquals(halfDaySeconds * 2, recorder.getCurrentTime() / 1000);

        // 切回多天范围，增加半天，切换到天记录范围进行判定（这时已经添加了一天半的时间，但是切换回来以后只保留一天的时间）
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);
        recorder.increaseSecond(halfDaySeconds);
        Mockito.reset(itemDataRepository);
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_TODAY);
        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();;
        assertEquals(startDay, recorder.getStartDay());
        assertEquals(halfDaySeconds * 2, recorder.getCurrentTime() / 1000);
    }

    /**
     * 测试记录范围变更方法
     *      日期不同从一天切到多天切换状态保持不变
     */
    @Test public void testSetRecorderScopeFromTodayToAlwaysInDifferentDate() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        long currentTime = recorder.getCurrentTime();
        recorder.setCurrentDayProvider(new TimeRecorderItemData.CurrentDayProvider() {
            @Override public MonthDay now() {
                return MonthDay.now().plusDays(1);
            }
        });
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);

        Mockito.verify(itemDataRepository, Mockito.never()).updateToCache();
        assertEquals(currentTime, recorder.getCurrentTime());
    }

    /**
     * 测试记录范围变更方法
     *      日期不同从多天切到一天重置状态
     */
    @Test public void testSetRecorderScopeFromAlwasyToTodayInDifferentDate() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);
        final MonthDay nextDay = MonthDay.now().plusDays(1);
        recorder.increaseSecond(1);
        recorder.setCurrentDayProvider(new TimeRecorderItemData.CurrentDayProvider() {
            @Override public MonthDay now() {
                return nextDay;
            }
        });
        Mockito.reset(itemDataRepository);
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_TODAY);

        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();
        assertEquals(nextDay, recorder.getStartDay());
        assertEquals(0, recorder.getCurrentTime());
    }

    /**
     * 测试增加描述方法
     *      当天范围内正常使用
     */
    @Test public void testIncreaseSecondTodayNormal() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.increaseSecond(1);

        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();
        assertEquals(1, recorder.getRecordSecond());
    }

    /**
     * 测试增加秒数方法
     *      当天范围内增加超过一天最多保存一天的时间
     */
    @Test public void testIncreaseSecondTodayMoreThanOneDay() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.increaseSecond(36 * 60 * 60);

        // 触发两次：增加秒数触发，矫正时间触发
        Mockito.verify(itemDataRepository, Mockito.times(2)).updateToCache();
        assertEquals(24 * 60 * 60, recorder.getRecordSecond());
    }

    /**
     * 则是增加秒数方法
     *      不同天增加一天内的时间
     */
    @Test public void testIncreaseSecondAlwaysNormal() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);

        recorder.increaseSecond(1);

        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();
        assertEquals(1, recorder.getRecordSecond());
    }

    /**
     * 测试增加秒数方法
     *      不同天增加超过一天的时间
     */
    @Test public void testIncreaseSecondAlwaysMoreThanOneDay() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);
        recorder.setRecordScope(TimeRecorder.RECORD_SCOPE_ALWAYS);

        recorder.increaseSecond(36 * 60 * 60);

        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();
        assertEquals(36 * 60 * 60, recorder.getRecordSecond());
    }

    /**
     * 测试时间秒数是否到达方法
     */
    @Test public void testIsSecondTimeArrived() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.increaseSecond(100);

        assertTrue(recorder.isSecondTimeArrived(50));
        assertTrue(recorder.isSecondTimeArrived(100));
        assertFalse(recorder.isSecondTimeArrived(150));
    }

    /**
     * 测试分钟是否到达方法
     */
    @Test public void testIsMinutesTimeArrived() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.increaseSecond(100);

        assertTrue(recorder.isMinuteTimeArrived(1));
        assertFalse(recorder.isMinuteTimeArrived(2));
    }

    /*
    =============================== 综合测试 =====================================
     */

    /**
     * 测试数据更新后触发数据缓存更新
     */
    @Test public void testUpdateToCacheWhenIncreaseSecond() {
        TimeRecorderItemData recorder = new TimeRecorderItemData(itemDataRepository);

        recorder.initTimeRecord();
        recorder.increaseSecond(1);
        recorder.increaseSecond(1);
        recorder.increaseSecond(1);

        Mockito.verify(itemDataRepository, Mockito.times(3)).updateToCache();
    }

}