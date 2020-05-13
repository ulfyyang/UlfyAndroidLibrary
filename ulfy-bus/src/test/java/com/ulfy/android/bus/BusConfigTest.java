package com.ulfy.android.bus;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class BusConfigTest {
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before public void init() {
        BusConfig.configured = false;
    }

    /**
     * 如果配置了入口正常执行
     */
    @Test public void testConfiguredEntrace() {
        BusConfig.init(RuntimeEnvironment.application);
        BusUtils.post(new Object());
    }

    /**
     * 如果没有配置入口则调用任何方法都会抛出异常
     */
    @Test public void testNotConfiguredEntrace() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Bus not configured in Application entrace, please add BusConfig.init(this); to Application");
        BusUtils.post(new Object());
    }
}
