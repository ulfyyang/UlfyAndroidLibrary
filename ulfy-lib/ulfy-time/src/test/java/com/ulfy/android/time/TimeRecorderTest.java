package com.ulfy.android.time;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TimeRecorderTest {

    /**
     * 初始化时间记录器的缓存配置
     * 初始化时间记录器的驱动器
     */
    @Before public void initTimeRecorder() {
        TimeConfig.init(RuntimeEnvironment.application);
        TimeRecorderDriverRepository.getInstance().setTimeRecorderDriverProvider(new TimeRecorderDriverRepository.TimeRecorderDriverProvider() {
            @Override public TimeRecorderDriver getDriver(String key) {
                return new TestDriver(key);
            }
        });
    }

    public static class TestDriver extends TimeRecorderDriver {
        TestDriver(String key) {
            super(key);
        }
        @Override void startDrive() {
            for (int i = 0; i < 10; i++) {
                onDrive();
            }
        }
        @Override void stopDrive() { }
    }

    /**
     * 测试常规使用过程
     */
    @Test public void testNormalUse() {
        OnTimeRecordListener onTimeRecordListener = Mockito.mock(OnTimeRecordListener.class);
        String key = "key";

        // 这里测试用的是同步的驱动器，因此需要先注册回调否则后续无法注册
        TimeRecorder.setOnTimeRecordListener(key, onTimeRecordListener);
        TimeRecorder.startRecord(key);

        Mockito.verify(onTimeRecordListener, Mockito.times(10)).onTimeRecording(key);
        assertEquals(10, TimeRecorder.getRecordSecond(key));
        assertTrue(TimeRecorder.isSecondTimeArrived(key, 5));

        System.gc();

        TimeRecorder.resetTimeRecorder();
        Mockito.reset(onTimeRecordListener);
        TimeRecorder.startRecord(key);

        Mockito.verify(onTimeRecordListener, Mockito.times(10)).onTimeRecording(key);
        assertEquals(10, TimeRecorder.getRecordSecond(key));
        assertTrue(TimeRecorder.isSecondTimeArrived(key, 5));
    }

}
