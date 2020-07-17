package com.ulfy.android.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

final class FinalUiTaskExecutor implements ITaskExecutor {
    private Executor executor = Executors.newCachedThreadPool();
    private List<UiTask> uiTaskList = new ArrayList<>();

    @Override public void post(Task task) {
        postTask(task);
    }

    private class TaskLifecycle implements Task.LifecycleCallback {
        @Override public void onStart(Task task) { }
        @Override public void onFinish(Task task) {
            clearTask(task);
        }
    }

    private synchronized void postTask(Task task) {
        if (task instanceof UiTask) {
            for (UiTask uiTask : uiTaskList) {
                uiTask.setCancelUiHandler(true);
            }
            UiTask uiTask = (UiTask) task;
            uiTask.setLifecycleCallback(new TaskLifecycle());
            uiTaskList.add(uiTask);
            executor.execute(uiTask);
        } else {
            throw new IllegalArgumentException("FinalUiTaskExecutor can only execute the instance of UiTask");
        }
    }

    private synchronized void clearTask(Task task) {
        uiTaskList.remove(task);
    }
}
