package com.ulfy.android.dialog;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public final class DialogConfig {
    public static final String ULFY_MAIN_DIALOG_ID = "__ULFY_MAIN_DIALOG_ID__";										// 默认使用的Dialog的弹出ID
    public static final String ULFY_MAIN_POPUP_ID = "__ULFY_MAIN_POPUP_ID__";										// 默认使用的Popup的弹出ID
    static final String ULFY_MAIN_ALERT_ID = "__ULFY_MAIN_ALERT_ID__";										        // 默认使用的Alert的弹出ID
    static final String ULFY_MAIN_PROGRASS_ID = "__ULFY_MAIN_PROGRASS_ID__";								        // 默认进度处理Dialog的ID
    static final String ULFY_DISABLE_TOUCH_DIALOG_ID = "__ULFY_DISABLE_TOUCH_DIALOG_ID__";					        // 屏蔽触摸弹出框

    public static final int DIALOG_ANIMATION_ID_TOP = R.style.window_anim_top;										// 用于在顶部显示的弹出框动画ID
    public static final int DIALOG_ANIMATION_ID_BOTTOM = R.style.window_anim_bottom;                                // 用于在底部显示的弹出框动画ID

    /**
     * 初始化弹出框模块
     */
    public static void init(Application context) {
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) {
                DialogRepository.getInstance().releaseDialogOnActivityDestoryed(activity);
            }
        });
    }
}
