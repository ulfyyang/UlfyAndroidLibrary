package com.ulfy.android.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class FinalUiTaskExecutor implements ITaskExecutor {
    private Executor executor = Executors.newCachedThreadPool();
    private List<UiTask> uiTaskList = new ArrayList<>();

    @Override public void post(Task task) {
        if (task instanceof UiTask) {
            for (UiTask uiTask : uiTaskList) {
                uiTask.setCancelUiHandler(true);
            }
            UiTask pendingUiTask = (UiTask) task;
            pendingUiTask.setLifecycleCallback(new LifecycleCallbackImpl());
            uiTaskList.add(pendingUiTask);
            executor.execute(pendingUiTask);
        } else {
            throw new IllegalArgumentException("FinalUiTaskExecutor can only execute UiTask");
        }
    }

    private class LifecycleCallbackImpl implements Task.LifecycleCallback {
        @Override public void onStart(Task task) { }
        @Override public void onFinish(Task task) {
            uiTaskList.remove(task);
        }
    }
}
