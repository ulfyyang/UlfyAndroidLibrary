package com.ulfy.android.time;

import com.ulfy.android.task_extension.UiTimer;

/**
 * 时间记录器的驱动器，通过时间来驱动
 */
class TimeRecorderTimerDriver extends TimeRecorderDriver {
    private UiTimer uiTimer = new UiTimer(1000);

    TimeRecorderTimerDriver(String key) {
        super(key);
        uiTimer.setUiTimerExecuteBody(new UiTimer.UiTimerExecuteBody() {
            @Override public void onExecute(UiTimer timer, UiTimer.TimerDriver timerDriver) {
                onDrive();
            }
        });
    }

    @Override void startDrive() {
        uiTimer.schedule();
    }

    @Override void stopDrive() {
        uiTimer.cancel();
    }
}
