package com.ulfy.android.task;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 单一任务执行器，在当前任务没有执行完毕之前其它的任务将被丢弃
 */
final class SingleTaskExecutor implements ITaskExecutor {
    private static final String TAG = SingleTaskExecutor.class.getName();
    private Executor executor = Executors.newSingleThreadExecutor();
    private Task task;

    @Override public synchronized void post(Task task) {
        if (this.task == null) {
            this.task = task;
            task.setLifecycleCallback(new TaskLifecycle());
            executor.execute(task);
        } else {
            Log.w(TAG, "SingleTaskExecutor is executing task now, new task is abandoned!");
        }
    }

    private synchronized void clearTask() {
        this.task = null;
        Log.v(TAG, "task finished, SingleTaskExecutor is free now.");
    }

    private class TaskLifecycle implements Task.LifecycleCallback {
        @Override public void onStart(Task task) { }
        @Override public void onFinish(Task task) { clearTask(); }
    }
}
