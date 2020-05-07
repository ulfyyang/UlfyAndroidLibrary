package com.ulfy.android.multi_domain_picker;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * 模拟一下真实的使用
 */
@RunWith(AndroidJUnit4.class)
public class MultiDomainPickerTest extends BaseAndroidTest {
    public String urlBaidu = "http://www.baidu.com";
    public String urlGoogle = "http://www.google.com";
    public String[] urls = new String[]{urlBaidu, urlGoogle};

    @Before public void init() {
        MultiDomainPickerConfig.init(InstrumentationRegistry.getContext(), Arrays.asList(urls));
        MultiDomainPicker.getInstance().reset();
    }

    @Test public void testNormalUse() throws Exception {
        String targetUrl = MultiDomainPicker.getInstance().getTargetDomainUrl(true);
        assertEquals(urlBaidu, targetUrl);
    }
}
