package com.ulfy.android.download_manager;

import android.app.Application;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DownloadManagerConfigTest extends BaseAndroidTest {
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before public void init() {
        DownloadManagerConfig.configured = false;
    }

    /**
     * 如果配置了入口正常执行
     */
    @Test public void testConfiguredEntrace() {
        DownloadManagerConfig.init((Application) InstrumentationRegistry.getContext().getApplicationContext());
        DownloadManager.getInstance();
    }

    /**
     * 如果没有配置入口则调用任何方法都会抛出异常
     */
    @Test public void testNotConfiguredEntrace() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("DownloadManager not configured in Application entrace, please add DownloadManagerConfig.init(this); to Application");
        DownloadManager.getInstance();
    }
}
