package com.ulfy.android.time;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class TimeConfigTest {
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before public void init() {
        TimeConfig.configured = false;
    }

    /**
     * 如果配置了入口正常执行
     */
    @Test public void testConfiguredEntrace() {
        TimeConfig.init(RuntimeEnvironment.application);
        TimeRecorder.resetTimeRecorder();
    }

    /**
     * 如果没有配置入口则调用任何方法都会抛出异常
     */
    @Test public void testNotConfiguredEntrace() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Time not configured in Application entrace, please add TimeConfig.init(this); to Application");
        TimeRecorder.resetTimeRecorder();
    }
}
