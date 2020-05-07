package com.ulfy.android.multi_domain_picker;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class BaseAndroidTest {
    private static final int DEFAULT_DURATION = 100;        // 默认的延迟时间
    @Rule public TestName testName = new TestName();        // 用于获取每个测试的方法名

    /**
     * 测试执行之前删除掉该用例的的截图文件
     */
    @Before public void clearScreenshot() {
        for (File file : getTestcaseScreenshotDictionary().listFiles()) {
            file.delete();
        }
    }

    /**
     * 设置View的布局参数
     */
    protected void matchParent(View view) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
    }

    /**
     * 设置View的布局参数
     */
    protected void wrapContent(View view) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        view.setLayoutParams(layoutParams);
    }

    /**
     * 保存屏幕截图
     *      默认100的延迟
     */
    protected void screenshot() {
        screenshot(DEFAULT_DURATION);
    }

    /**
     * 保存屏幕截图
     */
    protected void screenshot(int duration) {
        // 由于页面绘制完成后会有适当的延迟才会显示出来，因此需要延迟一下在截图。
        // 通常情况下100的延迟已经够了，但是对于一些复杂的页面变换是不够的，因此需要自己根据情况来实际处理
        delay(duration);

        File testcaseFile = new File(getTestcaseScreenshotDictionary(), testName.getMethodName() + '_' + System.currentTimeMillis() + ".png");

        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        boolean success = uiDevice.takeScreenshot(testcaseFile, 1.0f, 100);

        assertTrue(success);
    }

    /**
     * 延迟一段时间
     */
    protected void delay() {
        delay(DEFAULT_DURATION);
    }

    /**
     * 延迟指定的时间
     */
    protected void delay(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    /**
     * 获取每个测试用例的保存目录，一个测试用例有可能会产生多张图片，因此采用目录的方式进行管理
     */
    private File getTestcaseScreenshotDictionary() {
        File screenshotDictionary = new File(InstrumentationRegistry.getTargetContext().getCacheDir(), "test_screenshot");
        File testcaseDictionary = new File(screenshotDictionary, getClass().getName() + "_" + testName.getMethodName());

        if (!testcaseDictionary.exists()) {
            testcaseDictionary.mkdirs();
        }

        return testcaseDictionary;
    }
}
