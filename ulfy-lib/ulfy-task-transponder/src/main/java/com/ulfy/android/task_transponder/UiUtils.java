package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

class UiUtils {

    /**
     * 根据view的反射类型创建view
     */
    static View createViewFromClazz(Context context, Class<? extends View> clazz) {
        try {
            return clazz.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            throw new IllegalArgumentException("create view failed", e);
        }
    }

    /**
     * 将一个View填充到容器中，该容器中只会有一个View。如果该View已经在容器中了，则不会有任何操作
     */
    static void displayViewOnViewGroup(View view, ViewGroup container) {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        displayViewOnViewGroup(view, container, layoutParams);
    }

    /**
     * 将一个View填充到容器中，该容器中只会有一个View。如果该View已经在容器中了，则不会有任何操作
     */
    static void displayViewOnViewGroup(View view, ViewGroup container, ViewGroup.LayoutParams layoutParams) {
        if (container.indexOfChild(view) == -1 || container.getChildCount() != 1) {
            container.removeAllViews();
            container.addView(view, layoutParams);
        }
    }

    /**
     * 判断是否触摸了该 view
     */
    static boolean isTouchView(MotionEvent event, View view) {
        int[] location = {0, 0};
        view.getLocationInWindow(location);
        int left = location[0], top = location[1], bottom = top + view.getHeight(), right = left + view.getWidth();
        return event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom;
    }

    /**
     * 根据配置创建一个弹出框
     */
    static CustomDialog generateDialogByConfig(Context context, View view, TaskTransponderConfig.DialogProcesserConfig config) {
        return new CustomDialog(context, view, config.noBackground(), true, Gravity.CENTER, config.touchOutsideDismiss(), config.cancelable()).build();
    }
}
