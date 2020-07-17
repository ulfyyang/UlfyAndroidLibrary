package com.ulfy.android.bus;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public final class BusConfig {
    static boolean configured;

    /**
     * 初始化事件总线
     */
    public static void init(Application context) {
        if (!configured) {
            configured = true;
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

    static void throwExceptionIfConfigNotConfigured() {
        if (!configured) {
            throw new IllegalStateException("Bus not configured in Application entrace, please add BusConfig.init(this); to Application");
        }
    }
}
