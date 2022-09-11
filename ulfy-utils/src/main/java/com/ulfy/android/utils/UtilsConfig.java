package com.ulfy.android.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.Gravity;

public final class UtilsConfig {
    static Application context;

    static void init(Application context) {
        UtilsConfig.context = context;
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) {
                RecycledViewPoolRepository.getInstance().releasePoolOnActivityDestoryed(activity);
            }
        });
    }

    public static final class Config {
        public static String logTag = "ulfy-log";                                           // 日志打印的TAG值
        public static int toastGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;        // 吐司显示的默认位置
        public static float extraLayoutSpaceMultiple = 1;                                   // Recycler中额外布局空间倍数，表示为屏幕的几倍
    }
}
