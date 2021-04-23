package com.ulfy.android.multi_domain_picker;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * 模拟一下真实的使用
 */
public class MultiDomainPickerTest {
    public static final String KEY = "KEY_DEMO";
    public String urlBaidu = "http://www.baidu.com";
    public String urlGoogle = "http://www.google.com";
    public String[] urls = new String[]{urlBaidu, urlGoogle};

    @Before public void init() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        MultiDomainPickerConfig.init(context, Arrays.asList(urls));
        MultiDomainPickerConfig.init(context, KEY, Arrays.asList(urls));
        MultiDomainPicker.getInstance().reset();
    }

    @Test public void testNormalUse() throws Exception {
        String targetUrl = MultiDomainPicker.getInstance().getTargetDomainUrl(true);
        assertEquals(urlBaidu, targetUrl);
        targetUrl = MultiDomainPicker.getInstance().getTargetDomainUrl(KEY, true);
        assertEquals(urlBaidu, targetUrl);
    }
}
