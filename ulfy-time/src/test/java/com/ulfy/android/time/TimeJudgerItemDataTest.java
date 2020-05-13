package com.ulfy.android.time;

import org.joda.time.DateTime;
import org.joda.time.MonthDay;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TimeJudgerItemDataTest {
    public TimeJudgerItemDataRepository itemDataRepository;

    @Before public void initItemDataRepository() {
        itemDataRepository = Mockito.mock(TimeJudgerItemDataRepository.class);
    }

    /**
     * 测试是否在天内方法
     *      未初始化的判定器表示不在任何的时间区间内
     */
    @Test public void testIsInDaysWithoutInit() {
        TimeJudgerItemData judger = new TimeJudgerItemData(itemDataRepository);
        assertFalse(judger.isInDays(1));
        assertFalse(judger.isInDays(-1));
        assertFalse(judger.isInDays(5));
    }

    /**
     * 测试是否在天内方法
     *      同一个日期表示一天，相邻的两个日期表示两天
     */
    @Test public void testIsInDaysWithInit() {
        TimeJudgerItemData judger = new TimeJudgerItemData(itemDataRepository);

        judger.initTimeJudger();
        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();

        assertTrue(judger.isInDays(1));

        // 前移一天，则一共有两天。其不在一天内但是可以表示在两天、三天或更多的天内
        judger.setCurrentDayProvider(new TimeJudgerItemData.CurrentDayProvider() {
            @Override public MonthDay nowDay() {
                return MonthDay.now().plusDays(1);
            }
            @Override public DateTime nowTime() {
                return null;
            }
        });

        assertFalse(judger.isInDays(1));
        assertTrue(judger.isInDays(2));
        assertTrue(judger.isInDays(5));
    }

    /**
     * 测试是否在小时内方法
     *      未初始化的判定器表示不在任何的时间区间内
     */
    @Test public void testIsInHoursWithoutInit() {
        TimeJudgerItemData judger = new TimeJudgerItemData(itemDataRepository);
        assertFalse(judger.isInHours(1));
        assertFalse(judger.isInHours(-1));
        assertFalse(judger.isInHours(5));
    }

    /**
     * 测试是否在小时内方法
     */
    @Test public void testIsInHoursWithInit() {
        TimeJudgerItemData judger = new TimeJudgerItemData(itemDataRepository);

        judger.initTimeJudger();
        Mockito.verify(itemDataRepository, Mockito.times(1)).updateToCache();

        assertTrue(judger.isInHours(1));

        // 前移一小时，则一共有两小时。其不在一小时内但是可以表示在两小时、三小时或更多的小时内
        judger.setCurrentDayProvider(new TimeJudgerItemData.CurrentDayProvider() {
            @Override public MonthDay nowDay() {
                return null;
            }
            @Override public DateTime nowTime() {
                return DateTime.now().plusHours(1);
            }
        });

        assertFalse(judger.isInHours(1));
        assertTrue(judger.isInHours(2));
        assertTrue(judger.isInHours(5));
    }

}
