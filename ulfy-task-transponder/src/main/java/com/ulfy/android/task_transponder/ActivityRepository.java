package com.ulfy.android.task_transponder;

import android.app.Activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ActivityRepository {
    private final static ActivityRepository instance = new ActivityRepository();
    private List<Activity> activityList = new ArrayList<>();

    private ActivityRepository() { }

    static ActivityRepository getInstance() {
        return instance;
    }

    void notifyActivityCreated(Activity activity) {
        activityList.add(activity);
    }

    void notifyActivityDestroyed(Activity activity) {
        Iterator<Activity> iterator = activityList.iterator();
        while (iterator.hasNext()) {
            if (activity == iterator.next()) {
                iterator.remove();
            }
        }
    }

    Activity getTopActivity() {
        return activityList.size() > 0 ? activityList.get(activityList.size() - 1) : null;
    }
}
