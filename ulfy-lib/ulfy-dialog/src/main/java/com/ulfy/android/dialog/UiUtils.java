package com.ulfy.android.dialog;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

class UiUtils {
    /**
     * 清空view的父容器，使得view被添加到其它的容器中不会发生错误
     */
    static void clearParent(View view) {
        if(view == null) {
            return;
        }
        ViewParent parent = view.getParent();
        if(parent == null || !(parent instanceof ViewGroup)) {
            return;
        }
        // 有些机型 把子 view 移除后还保存这布局参数，这样会导致这个 view 在其它的容器中出现布局匹配错误
        ((ViewGroup)parent).removeView(view);
    }

    /**
     * 判断是否触摸了该 view
     */
    static boolean isTouchView(MotionEvent event, View view) {
        int[] location = {0, 0};
        view.getLocationInWindow(location);
        int left = location[0], top = location[1], bottom = top + view.getHeight(), right = left + view.getWidth();
        // 只要找到一个点击的 view，则不隐藏软键盘
        if (event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom) {
            return true;
        } else {
            return false;
        }
    }

}
