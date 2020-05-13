package com.ulfy.android.image;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class ImageConfigTest {
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before public void init() {
        ImageConfig.configured = false;
    }

    /**
     * 如果配置了入口正常执行
     */
    @Test public void testConfiguredEntrace() {
        ImageConfig.init(RuntimeEnvironment.application);
        ImageUtils.getCacheSize();
    }

    /**
     * 如果没有配置入口则调用任何方法都会抛出异常
     */
    @Test public void testNotConfiguredEntrace1() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Image not configured in Application entrace, please add ImageConfig.init(this); to Application");
        ImageUtils.getCacheSize();
    }

    /**
     * 如果没有配置入口则调用任何方法都会抛出异常
     */
    @Test public void testNotConfiguredEntrace2() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Image not configured in Application entrace, please add ImageConfig.init(this); to Application");
        new BlurBitmapNode().onProcessBitmap(null);
    }
}
