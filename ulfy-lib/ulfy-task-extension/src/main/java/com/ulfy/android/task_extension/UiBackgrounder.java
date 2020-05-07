package com.ulfy.android.task_extension;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 在后台执行任务，在UI线程中相应的便捷小工具
 */
public final class UiBackgrounder {
    private Handler uiHandler = new Handler(Looper.getMainLooper());                                    // 用于在UI线程中执行代码的切换器
    private ExecutorService executorService = Executors.newCachedThreadPool();                          // 用于在后台线程中执行代码的切换器
    private List<OnBackgrounderStartListener> onBackgrounderStartListenerList = new ArrayList<>();      // 任务开始的监听，在UI线程执行
    private List<UiBackgrounderExecuteBody> uiBackgrounderExecuteBodyList = new ArrayList<>();          // 任务执行过程，在后台线程执行
    private List<OnBackgrounderFinishListener> onBackgrounderFinishListenerList = new ArrayList<>();    // 任务结束监听，在UI线程执行

    /**
     * 连接一个UI定时器，在该任务执行完毕后自动开启定时器
     *      该方法必须在其它的设置监听器方法设置后执行
     */
    public synchronized UiTimer connect(final UiTimer uiTimer) {
        this.addOnBackgrounderFinishListener(new OnBackgrounderFinishListener() {
            @Override public void onBackgrounderFinish(UiBackgrounder backgrounder) {
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
        this.addOnBackgrounderFinishListener(new OnBackgrounderFinishListener() {
            @Override public void onBackgrounderFinish(UiBackgrounder backgrounder) {
                uiBackgrounder.start();
            }
        });
        return uiBackgrounder;
    }

    public synchronized UiBackgrounder start() {
        // UI 线程中相应开始回调
        if (onBackgrounderStartListenerList.size() > 0) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                startInner();
            } else {
                uiHandler.post(new Runnable() {
                    @Override public void run() {
                        startInner();
                    }
                });
            }
        }
        return this;
    }

    private synchronized void startInner() {
        for (OnBackgrounderStartListener onBackgrounderStartListener : onBackgrounderStartListenerList) {
            if (onBackgrounderStartListener != null) {
                onBackgrounderStartListener.onBackgrounderStart(UiBackgrounder.this);
            }
        }
        // 后台线程中执行执行体
        if (uiBackgrounderExecuteBodyList.size() > 0) {
            executorService.execute(new Runnable() {
                @Override public void run() {
                    for (UiBackgrounderExecuteBody uiBackgrounderExecuteBody : uiBackgrounderExecuteBodyList) {
                        if (uiBackgrounderExecuteBody != null) {
                            uiBackgrounderExecuteBody.onExecute(UiBackgrounder.this);
                        }
                    }
                    // UI 线程中相应结束回调
                    if (onBackgrounderFinishListenerList.size() > 0) {
                        uiHandler.post(new Runnable() {
                            @Override public void run() {
                                for (OnBackgrounderFinishListener onBackgrounderFinishListener : onBackgrounderFinishListenerList) {
                                    if (onBackgrounderFinishListener != null) {
                                        onBackgrounderFinishListener.onBackgrounderFinish(UiBackgrounder.this);
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public synchronized UiBackgrounder setOnBackgrounderStartListener(OnBackgrounderStartListener onBackgrounderStartListener) {
        this.onBackgrounderStartListenerList.clear();
        return this.addOnBackgrounderStartListener(onBackgrounderStartListener);
    }

    public synchronized UiBackgrounder addOnBackgrounderStartListener(OnBackgrounderStartListener onBackgrounderStartListener) {
        if (onBackgrounderStartListener != null) {
            this.onBackgrounderStartListenerList.add(onBackgrounderStartListener);
        }
        return this;
    }

    public synchronized UiBackgrounder setUiBackgrounderExecuteBody(UiBackgrounderExecuteBody uiBackgrounderExecuteBody) {
        this.uiBackgrounderExecuteBodyList.clear();
        return this.addUiBackgrounderExecuteBody(uiBackgrounderExecuteBody);
    }

    public synchronized UiBackgrounder addUiBackgrounderExecuteBody(UiBackgrounderExecuteBody uiBackgrounderExecuteBody) {
        if (uiBackgrounderExecuteBody != null) {
            this.uiBackgrounderExecuteBodyList.add(uiBackgrounderExecuteBody);
        }
        return this;
    }

    public synchronized UiBackgrounder setOnBackgrounderFinishListener(OnBackgrounderFinishListener onBackgrounderFinishListener) {
        this.onBackgrounderFinishListenerList.clear();
        return this.addOnBackgrounderFinishListener(onBackgrounderFinishListener);
    }

    public synchronized UiBackgrounder addOnBackgrounderFinishListener(OnBackgrounderFinishListener onBackgrounderFinishListener) {
        if (onBackgrounderFinishListener != null) {
            this.onBackgrounderFinishListenerList.add(onBackgrounderFinishListener);
        }
        return this;
    }

    /**
     * 当定时器启动时的回调
     */
    public interface OnBackgrounderStartListener {
        void onBackgrounderStart(UiBackgrounder backgrounder);
    }

    /**
     * 任务执行体
     */
    public interface UiBackgrounderExecuteBody {
        void onExecute(UiBackgrounder backgrounder);
    }

    /**
     * 当任务结束时的回调
     */
    public interface OnBackgrounderFinishListener {
        void onBackgrounderFinish(UiBackgrounder backgrounder);
    }

}
