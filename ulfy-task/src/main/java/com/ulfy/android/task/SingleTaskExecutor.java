package com.ulfy.android.task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 单一任务执行器，在当前任务没有执行完毕之后其它的任务将被丢弃
 */
final class SingleTaskExecutor implements ITaskExecutor {
    private Executor executor = Executors.newSingleThreadExecutor();
    private Task task;

    @Override public void post(Task task) {
        postTask(task);
    }

    private class TaskLifecycle implements Task.LifecycleCallback {
        @Override public void onStart(Task task) { }
        @Override public void onFinish(Task task) {
            clearTask();
        }
    }

    private synchronized void postTask(Task task) {
        if (this.task == null) {
            this.task = task;
            task.setLifecycleCallback(new TaskLifecycle());
            executor.execute(task);
        }
    }

    private synchronized void clearTask() {
        this.task = null;
    }
}
