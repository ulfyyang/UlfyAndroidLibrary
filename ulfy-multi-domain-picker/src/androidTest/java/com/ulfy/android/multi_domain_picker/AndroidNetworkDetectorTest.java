package com.ulfy.android.multi_domain_picker;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AndroidNetworkDetectorTest {

    // 需要手动打开、关闭安卓设备的网络连接之后分别运行测试查看结果，需要人眼在控制台查看
    @Test public void isNetworkConnected() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        boolean networkConnected = new AndroidNetworkDetector(context).isNetworkConnected();
        assertTrue(networkConnected);
    }

}