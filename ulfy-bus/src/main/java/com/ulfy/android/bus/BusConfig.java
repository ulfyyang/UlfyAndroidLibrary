package com.ulfy.android.bus;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public final class BusConfig {

    /**
     * 初始化事件总线
     */
    static void init(Application context) {
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) {
                BusRepository.getInstance().releaseBusOnLifecycleCallback(activity);
            }
        });
    }

}
