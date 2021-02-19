package com.ulfy.android.task_extension;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 在UI线程中执行任务的Timer循环
 */
public final class UiTimer {
    private Handler uiHandler = new Handler(Looper.getMainLooper());                                // 用于在UI线程中执行代码的切换器
    private ExecutorService executorService = Executors.newCachedThreadPool();                      // 用于在后台线程中执行代码的切换器
    private TimerDriver timerDriver;                                                                // 定时器的内核驱动器
    private boolean isSchedule;                                                                     // 是否已经开始了循环，这个表示循环是否终止
    private long delay;                                                                             // 每次执行间隔的时间
    private long delayStart;                                                                        // 延迟指定时间后启动定时器任务循环
    private List<OnTimerStartListener> onTimerStartListenerList = new ArrayList<>();                // 定时器启动时的回调
    private List<BackgroundTimerExecuteBody> backgroundTimerExecuteBodyList = new ArrayList<>();    // 定时器执行的任务
    private List<UiTimerExecuteBody> uiTimerExecuteBodyList = new ArrayList<>();                    // 定时器执行的任务
    private List<OnTimerFinishListener> onTimerFinishListenerList = new ArrayList<>();              // 当定时器结束时的回调

    public UiTimer() {
        this(0, null, null);
    }

    public UiTimer(long delay) {
        this(delay, null, null);
    }

    public UiTimer(long delay, UiTimerExecuteBody uiTimerExecuteBody, OnTimerFinishListener onTimerFinishListener) {
        this.setDelay(delay);
        this.timerDriver = new DefaultTimerDriver();
        this.setUiTimerExecuteBody(uiTimerExecuteBody);
        this.setOnTimerFinishListener(onTimerFinishListener);
    }

    /**
     * 设置延迟启动定时器循环的时间
     */
    public UiTimer setDelayStart(long delayStart) {
        this.delayStart = delayStart;
        return this;
    }

    /**
     * 连接一个UI定时器，在该任务执行完毕后自动开启定时器
     *      该方法必须在其它的设置监听器方法设置后执行
     */
    public synchronized UiTimer connect(final UiTimer uiTimer) {
        this.addOnTimerFinishListener(new OnTimerFinishListener() {
            @Override public void onTimerFinish(UiTimer timer, TimerDriver timerDriver) {
                uiTimer.schedule();
            }
        });
        return uiTimer;
    }

    /**
     * 连接一个UI后台器，在该任务执行完毕后自动开启后台器
     *      该方法必须在其它的设置监听器方法设置后执行
     */
    public synchronized UiBackgrounder connect(final UiBackgrounder uiBackgrounder) {
        this.addOnTimerFinishListener(new OnTimerFinishListener() {
            @Override public void onTimerFinish(UiTimer timer, TimerDriver timerDriver) {
                uiBackgrounder.start();
            }
        });
        return uiBackgrounder;
    }

    public synchronized UiTimer schedule() {
        if (!isSchedule) {
            isSchedule = true;
            if (Looper.myLooper() == Looper.getMainLooper()) {
                scheduleInner();
            } else {
                uiHandler.post(new Runnable() {
                    @Override public void run() {
                        scheduleInner();
                    }
                });
            }
        }
        return this;
    }

    private synchronized void scheduleInner() {
        for (OnTimerStartListener onTimerStartListener : onTimerStartListenerList) {
            if (onTimerStartListener != null) {
                onTimerStartListener.onTimerStart(this, timerDriver);
            }
        }
        timerDriver.startDrive(this);
    }

    /**
     * 取消定时器
     */
    public synchronized UiTimer cancel() {
        return cancel(true);
    }

    /**
     * 取消定时器
     * @param executeFinishCallback 取消时是否执行 onTimerFinish 回调
     */
    public synchronized UiTimer cancel(boolean executeFinishCallback) {
        if (isSchedule) {
            isSchedule = false;
            timerDriver.stopDrive(this);
            if (executeFinishCallback) {
                for (OnTimerFinishListener onTimerFinishListener : onTimerFinishListenerList) {
                    if (onTimerFinishListener != null) {
                        onTimerFinishListener.onTimerFinish(this, timerDriver);
                    }
                }
            }
        }
        return this;
    }

    public UiTimer setTimerDriver(TimerDriver timerDriver) {
        this.timerDriver = timerDriver;
        return this;
    }

    public synchronized boolean isSchedule() {
        return isSchedule;
    }

    public synchronized UiTimer setDelay(long delay) {
        this.delay = delay < 0 ? 0 : delay;
        return this;
    }

    public synchronized long getDelay() {
        return delay;
    }

    public synchronized UiTimer setOnTimerStartListener(OnTimerStartListener onTimerStartListener) {
        this.onTimerStartListenerList.clear();
        return this.addOnTimerStartListener(onTimerStartListener);
    }

    public synchronized UiTimer addOnTimerStartListener(OnTimerStartListener onTimerStartListener) {
        if (onTimerStartListener != null) {
            this.onTimerStartListenerList.add(onTimerStartListener);
        }
        return this;
    }

    public synchronized UiTimer setBackgroundTimerExecuteBody(BackgroundTimerExecuteBody backgroundTimerExecuteBody) {
        this.backgroundTimerExecuteBodyList.clear();
        return this.addBackgroundTimerExecuteBody(backgroundTimerExecuteBody);
    }

    public synchronized UiTimer addBackgroundTimerExecuteBody(BackgroundTimerExecuteBody backgroundTimerExecuteBody) {
        if (backgroundTimerExecuteBody != null) {
            this.backgroundTimerExecuteBodyList.add(backgroundTimerExecuteBody);
        }
        return this;
    }

    public synchronized UiTimer setUiTimerExecuteBody(UiTimerExecuteBody uiTimerExecuteBody) {
        this.uiTimerExecuteBodyList.clear();
        return this.addUiTimerExecuteBody(uiTimerExecuteBody);
    }

    public synchronized UiTimer addUiTimerExecuteBody(UiTimerExecuteBody uiTimerExecuteBody) {
        if (uiTimerExecuteBody != null) {
            this.uiTimerExecuteBodyList.add(uiTimerExecuteBody);
        }
        return this;
    }

    public synchronized UiTimer setOnTimerFinishListener(OnTimerFinishListener onTimerFinishListener) {
        this.onTimerFinishListenerList.clear();
        return this.addOnTimerFinishListener(onTimerFinishListener);
    }

    public synchronized UiTimer addOnTimerFinishListener(OnTimerFinishListener onTimerFinishListener) {
        if (onTimerFinishListener != null) {
            this.onTimerFinishListenerList.add(onTimerFinishListener);
        }
        return this;
    }

    public TimerDriver getTimerDriver() {
        return timerDriver;
    }

    /**
     * 当定时器启动时的回调
     */
    public interface OnTimerStartListener {
        void onTimerStart(UiTimer uiTimer, TimerDriver timerDriver);
    }

    /**
     * 任务执行体
     *      在后台线程中运行
     */
    public interface BackgroundTimerExecuteBody {
        void onExecute(UiTimer timer, TimerDriver timerDriver);
    }

    /**
     * 任务执行体
     *      在UI线程中运行
     */
    public interface UiTimerExecuteBody {
        void onExecute(UiTimer timer, TimerDriver timerDriver);
    }

    /**
     * 当任务结束时的回调
     */
    public interface OnTimerFinishListener {
        void onTimerFinish(UiTimer timer, TimerDriver timerDriver);
    }

    /**
     * 定时器驱动接口
     */
    public interface TimerDriver {
        void startDrive(UiTimer uiTimer);
        void stopDrive(UiTimer uiTimer);
    }

    /**
     * 默认的定时器驱动器
     * 无限制的按照时间间隔执行
     */
    public static class DefaultTimerDriver implements TimerDriver {
        private TimerHandler timerHandler;

        @Override public void startDrive(final UiTimer uiTimer) {
            if (timerHandler != null) {
                timerHandler.stopTimer();
            }
            timerHandler = new TimerHandler(uiTimer, new Runnable() {
                @Override public void run() {
                    timerExec(uiTimer);
                }
            });
            timerHandler.startTimer();
        }

        @Override public void stopDrive(UiTimer uiTimer) {
            if (timerHandler != null) {
                timerHandler.stopTimer();
            }
        }

        private void timerExec(final UiTimer uiTimer) {
            if (uiTimer.backgroundTimerExecuteBodyList.size() > 0) {
                uiTimer.executorService.execute(new Runnable() {
                    @Override public void run() {
                        for (BackgroundTimerExecuteBody backgroundTimerExecuteBody : uiTimer.backgroundTimerExecuteBodyList) {
                            if (backgroundTimerExecuteBody != null) {
                                backgroundTimerExecuteBody.onExecute(uiTimer, DefaultTimerDriver.this);
                            }
                        }
                    }
                });
            }
            for (UiTimerExecuteBody uiTimerExecuteBody : uiTimer.uiTimerExecuteBodyList) {
                if (uiTimerExecuteBody != null) {
                    uiTimerExecuteBody.onExecute(uiTimer, DefaultTimerDriver.this);
                }
            }
        }
    }

    public static class NumberTimerDriver implements TimerDriver {
        private TimerHandler timerHandler;
        private int startNumber;
        private int endNumber;
        private int currentNumber;
        private int step = 1;
        private boolean loop;
        private boolean reverse;

        public NumberTimerDriver(int startNumber, int endNumber, int step, boolean loop, boolean reverse) {
            this.startNumber = startNumber;
            this.endNumber = endNumber;
            this.step = step;
            this.loop = loop;
            this.reverse = reverse;
            this.initCurrentNumber();
        }

        private void reverseStatusIfNeed() {
            if (this.reverse) {
                int temp = this.startNumber;
                this.startNumber = this.endNumber;
                this.endNumber = temp;
            }
        }

        public void initCurrentNumber() {
            this.currentNumber = this.startNumber;
        }

        private void optCurrentNumber() {
            this.currentNumber += this.startNumber < this.endNumber ? this.step : -this.step;
        }

        private boolean shouldStop() {
            return this.startNumber < this.endNumber ? this.currentNumber > this.endNumber : this.currentNumber < this.endNumber;
        }

        public int getCurrentNumber() {
            return currentNumber;
        }

        @Override public void startDrive(final UiTimer uiTimer) {
            if (timerHandler != null) {
                timerHandler.stopTimer();
            }
            timerHandler = new TimerHandler(uiTimer, new Runnable() {
                @Override public void run() {
                    timerExec(uiTimer);
                }
            });
            timerHandler.startTimer();
        }

        @Override public void stopDrive(UiTimer uiTimer) {
            if (timerHandler != null) {
                timerHandler.stopTimer();
            }
        }

        private void timerExec(final UiTimer uiTimer) {
            if (uiTimer.backgroundTimerExecuteBodyList.size() > 0) {
                uiTimer.executorService.execute(new Runnable() {
                    @Override public void run() {
                        for (BackgroundTimerExecuteBody backgroundTimerExecuteBody : uiTimer.backgroundTimerExecuteBodyList) {
                            if (backgroundTimerExecuteBody != null) {
                                backgroundTimerExecuteBody.onExecute(uiTimer, NumberTimerDriver.this);
                            }
                        }
                    }
                });
            }
            for (UiTimerExecuteBody uiTimerExecuteBody : uiTimer.uiTimerExecuteBodyList) {
                if (uiTimerExecuteBody != null && !shouldStop()) {
                    uiTimerExecuteBody.onExecute(uiTimer, NumberTimerDriver.this);
                }
            }
            if (shouldStop()) {
                if (loop) {
                    reverseStatusIfNeed();
                    initCurrentNumber();
                    timerExec(uiTimer);
                } else {
                    uiTimer.cancel();
                }
            } else {
                optCurrentNumber();
            }
        }
    }

    /**
     * 定时执行任务的简单封装，用于提供最底层的间隔执行能力
     */
    private static class TimerHandler extends Handler {
        private UiTimer uiTimer;
        private int identityId;
        private Runnable timerExecuteBody;

        public TimerHandler(UiTimer uiTimer, Runnable timerExecuteBody) {
            super(Looper.getMainLooper());
            this.uiTimer = uiTimer;
            this.identityId = hashCode();
            this.timerExecuteBody = timerExecuteBody;
        }

        public void startTimer() {
            if (uiTimer.delayStart > 0) {
                sendMessageDelayed(obtainMessage(identityId), uiTimer.delayStart);
            } else {
                sendMessage(obtainMessage(identityId));
            }
        }

        public void stopTimer() {
            removeMessages(identityId);
        }

        public void handleMessage(Message msg) {
            if (msg.what == identityId && uiTimer.isSchedule && timerExecuteBody != null) {
                timerExecuteBody.run();
                sendMessageDelayed(obtainMessage(identityId), uiTimer.delay);
            }
        }
    }

}
