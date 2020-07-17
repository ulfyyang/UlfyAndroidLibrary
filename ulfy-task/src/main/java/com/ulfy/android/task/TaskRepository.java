package com.ulfy.android.task;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TaskRepository {
    private static final TaskRepository instance = new TaskRepository();
    private Map<Context, List<UiTask>> uiTaskMap = new HashMap<>();

    private TaskRepository() { }

    static TaskRepository getInstance() {
        return instance;
    }

    synchronized void addUiTask(Context context, UiTask uiTask) {
        List<UiTask> uiTaskList = uiTaskMap.get(context);
        if (uiTaskList == null) {
            uiTaskList = new ArrayList<>();
            uiTaskMap.put(context, uiTaskList);
        }
        uiTaskList.add(uiTask);
    }

    synchronized void removeUiTask(Context context, UiTask uiTask) {
        List<UiTask> uiTaskList = uiTaskMap.get(context);
        if (uiTaskList != null) {
            uiTaskList.remove(uiTask);
            if (uiTaskList.size() == 0) {
                uiTaskMap.remove(context);
            }
        }
    }

    synchronized void releaseUiTaskOnActivityDestoryed(Context context) {
        List<UiTask> uiTaskList = uiTaskMap.remove(context);
        if (uiTaskList != null) {
            for (UiTask uiTask : uiTaskList) {
                uiTask.setCancelUiHandler(true);
            }
        }
    }
}
