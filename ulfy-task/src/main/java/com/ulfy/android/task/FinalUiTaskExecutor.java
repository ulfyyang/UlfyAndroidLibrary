package com.ulfy.android.task;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final class FinalUiTaskExecutor implements ITaskExecutor {
    private static final String TAG = FinalUiTaskExecutor.class.getName();
    private Executor executor = Executors.newCachedThreadPool();
    private List<UiTask> uiTaskList = new LinkedList<>();

    @Override public synchronized void post(Task task) {
        if (task instanceof UiTask) {
            for (UiTask uiTask : uiTaskList) {
                uiTask.setCancelUiHandler(true);
            }
            UiTask uiTask = (UiTask) task;
            uiTask.setLifecycleCallback(new TaskLifecycle());
            uiTaskList.add(uiTask);
            executor.execute(uiTask);
            Log.v(TAG, String.format("task has been executed and recorded by FinalUiTaskExecutor, other task will not update ui, current record count is %d", uiTaskList.size()));
        } else {
            Log.w(TAG, String.format("FinalUiTaskExecutor can only execute the instance of UiTask, this instance of %s is abandoned!", task.getClass().getName()));
        }
    }

    private synchronized void clearTask(Task task) {
        uiTaskList.remove(task);
        Log.v(TAG, String.format("task finished, removed from FinalUiTaskExecutor record, current record count is %d", uiTaskList.size()));
        if (uiTaskList.size() == 0) {
            Log.v(TAG, "FinalUiTaskExecutor record is empty now");
        }
    }

    private class TaskLifecycle implements Task.LifecycleCallback {
        @Override public void onStart(Task task) { }
        @Override public void onFinish(Task task) {
            clearTask(task);
        }
    }
}
