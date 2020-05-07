package com.ulfy.android.task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 单一任务执行器，在当前任务没有执行完毕之后其它的任务将被丢弃
 */
class SingleTaskExecutor implements ITaskExecutor {
    private Executor executor = Executors.newSingleThreadExecutor();
    private Task task;

    @Override public void post(Task task) {
        initTask(task);
    }

    private class TaskLifecycle implements Task.LifecycleCallback {
        @Override public void onStart(Task task) { }
        @Override public void onFinish(Task task) {
            clearTask();
        }
    }

    private synchronized void initTask(Task task) {
        if (this.task == null) {
            this.task = task;
            executor.execute(task.setLifecycleCallback(new TaskLifecycle()));
        }
    }

    private synchronized void clearTask() {
        this.task = null;
    }
}
