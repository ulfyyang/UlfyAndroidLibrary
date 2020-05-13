package com.ulfy.android.time;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TimeJudgerTest {

    @Before public void initTimeJudger() {
        TimeConfig.init(RuntimeEnvironment.application);
    }

    /**
     * 测试常规使用过程
     */
    @Test public void testNormalUse() {
        String key = "key";

        assertFalse(TimeJudger.isInDays(key, 1));
        assertFalse(TimeJudger.isInHours(key, 1));

        TimeJudger.initTimeJudger(key);

        System.gc();

        assertTrue(TimeJudger.isInDays(key, 1));
        assertTrue(TimeJudger.isInHours(key, 1));
    }

}
