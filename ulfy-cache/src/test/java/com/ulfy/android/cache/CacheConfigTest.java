package com.ulfy.android.cache;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class CacheConfigTest {
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before public void init() {
        CacheConfig.defaultConfigured = false;
    }

    /**
     * 如果配置了入口正常执行
     */
    @Test public void testConfiguredEntrace() {
        CacheConfig.initDefaultCache(RuntimeEnvironment.application);
        CacheUtils.isCached(TestEntity1.class);
    }

    /**
     * 如果没有配置入口则调用任何方法都会抛出异常
     */
    @Test public void testNotConfiguredEntrace() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Cache not configured for default config in Application entrace, please add CacheConfig.initDefaultCache(this); to Application");
        CacheUtils.isCached(TestEntity1.class);
    }
}
