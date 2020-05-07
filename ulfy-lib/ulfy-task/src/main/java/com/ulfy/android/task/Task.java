package com.ulfy.android.task;

/**
 * 抽象任务，任务执行器执行的对象
 */
public abstract class Task implements Runnable {

    /**
     * 任务执行生命周期的回调
     */
    public interface LifecycleCallback {
        /**
         * 当任务开始时的回调
         * 该回调不会在UI线程中执行
         */
        void onStart(Task task);

        /**
         * 当任务结束时的回调
         * 该回调不会在UI线程中执行
         */
        void onFinish(Task task);
    }

    private boolean isRunning;                      // 记录任务是否正在执行，记录的是后台运行的部分
    private LifecycleCallback lifecycleCallback;    // 生命周期回调

    @Override public void run() {
        isRunning = true;
        if (lifecycleCallback != null) {
            lifecycleCallback.onStart(this);
        }
        run(this);
        isRunning = false;
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
    public final Task setLifecycleCallback(LifecycleCallback lifecycleCallback) {
        this.lifecycleCallback = lifecycleCallback;
        return this;
    }
}
