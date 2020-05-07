package com.ulfy.android.system;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

class BackgroundRunningDetector {
    private static int visiableActivityCount = 0;

    static void init(Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) {
                visiableActivityCount++;
            }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) {
                visiableActivityCount--;
            }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        });
    }

    static boolean isBackgroundRunning() {
        return visiableActivityCount == 0;
    }
}
