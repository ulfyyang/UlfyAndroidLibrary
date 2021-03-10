package com.ulfy.android.task;

import android.util.Log;

/**
 * 抽象任务，任务执行器 {@link ITaskExecutor} 执行的对象。
 */
public abstract class Task implements Runnable {
    private static final String TAG = Task.class.getName();
    private boolean isRunning;                      // 记录任务是否正在执行，记录的是后台运行的部分
    private LifecycleCallback lifecycleCallback;    // 生命周期回调

    /**
     * 实现 Runnable 的方法做出一些定制工作，客户程序在不了解设计原则的情况下
     * 不要直接重载定制，建议子类通过复写 {@link #run(Task)} 方法来对任务进
     * 行定制
     */
    @Override public void run() {
        isRunning = true;       // 在任务开始回调的时候，任务必然已经开启了
        if (lifecycleCallback != null) {
            lifecycleCallback.onStart(this);
        }
        try {// 如果子类不处理异常，则这里拦获并静默处理（确保任务能够正常结束）
            run(this);
        } catch (Exception e) {
            Log.w(TAG, "task execute faile without handling!", e);
        }
        isRunning = false;      // 在任务结束回调的时候，任务必然已经结束了
        if (lifecycleCallback != null) {
            lifecycleCallback.onFinish(this);
        }
    }

    /**
     * 抽象方法，由子类实现具体的运行细节
     */
    protected abstract void run(Task task);

    /**
     * 判断任务是否正在执行
     */
    public final boolean isRunning() {
        return isRunning;
    }

    /**
     * 设置生命周期回调监听
     */
    final void setLifecycleCallback(LifecycleCallback lifecycleCallback) {
        this.lifecycleCallback = lifecycleCallback;
    }


    /**
     * 任务执行生命周期的回调
     */
    public interface LifecycleCallback {
        /**
         * 当任务开始时的回调（该回调不会在 UI 线程中执行）
         */
        void onStart(Task task);
        /**
         * 当任务结束时的回调（该回调不会在 UI 线程中执行）
         */
        void onFinish(Task task);
    }
}
