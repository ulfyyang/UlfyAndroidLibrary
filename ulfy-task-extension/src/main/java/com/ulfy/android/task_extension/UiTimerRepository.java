package com.ulfy.android.task_extension;

import java.util.ArrayList;
import java.util.List;

/**
 * UiTimer定时器仓库，可以对多个定时器进行统一的管理
 */
public class UiTimerRepository {
    private List<UiTimer> uiTimerList = new ArrayList<>();
    private OnAllTimerFinishedListener onAllTimerFinishedListener;

    public UiTimerRepository register(UiTimer uiTimer) {
        if (!uiTimerList.contains(uiTimer)) {
            uiTimerList.add(uiTimer);
            uiTimer.setOnTimerFinishListener(new UiTimer.OnTimerFinishListener() {
                public void onTimerFinish(UiTimer timer, UiTimer.TimerDriver timerDriver) {
                    callOnALlTimerFinishedListener();
                }
            });
        }
        return this;
    }

    public UiTimerRepository unRegister(UiTimer uiTimer) {
        if (uiTimerList.contains(uiTimer)) {
            uiTimerList.remove(uiTimer);
        }
        return this;
    }

    public UiTimerRepository schduleAll() {
        for (UiTimer uiTimer : uiTimerList) {
            uiTimer.schedule();
        }
        return this;
    }

    public UiTimerRepository cancelAll() {
        for (UiTimer uiTimer : uiTimerList) {
            uiTimer.cancel();
        }
        return this;
    }

    public UiTimerRepository setOnAllTimerFinishedListener(OnAllTimerFinishedListener onAllTimerFinishedListener) {
        this.onAllTimerFinishedListener = onAllTimerFinishedListener;
        return this;
    }

    private synchronized void callOnALlTimerFinishedListener() {
        if (onAllTimerFinishedListener != null) {
            boolean allFinished = true;
            for (UiTimer uiTimer : uiTimerList) {
                if (uiTimer.isSchedule()) {
                    allFinished = false;
                    break;
                }
            }
            if (allFinished) {
                onAllTimerFinishedListener.onAllTimerFinished();
            }
        }
    }

    public interface OnAllTimerFinishedListener {
        void onAllTimerFinished();
    }
}
