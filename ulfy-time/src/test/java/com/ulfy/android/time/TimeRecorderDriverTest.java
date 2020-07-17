package com.ulfy.android.time;

import org.junit.Test;
import org.mockito.Mockito;

public class TimeRecorderDriverTest {

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
     * 测试时间记录驱动器的常规使用
     */
    @Test public void testNormalTimeRecorderDriver() {
        TimeRecorderDriver.OnDriveListener onDriveListener = Mockito.mock(TimeRecorderDriver.OnDriveListener.class);
        OnTimeRecordListener onTimeRecordListener = Mockito.mock(OnTimeRecordListener.class);

        TestDriver driver = new TestDriver("key");
        driver.setOnDriveListener(onDriveListener);
        driver.setOnTimeRecordListener(onTimeRecordListener);

        driver.startDrive();

        Mockito.verify(onDriveListener, Mockito.times(10)).onDrive("key");
        Mockito.verify(onTimeRecordListener, Mockito.times(10)).onTimeRecording("key");
    }

}
