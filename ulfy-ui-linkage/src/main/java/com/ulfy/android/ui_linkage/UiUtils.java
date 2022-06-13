package com.ulfy.android.ui_linkage;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

class UiUtils {
    /**
     * 判断输入框中的内容是否为空
     */
    static boolean isEmpty(TextView textView) {
        String text = textView.getText().toString().trim();
        return text == null || text.length() == 0;
    }

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
     * dp转px
     */
    static int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 将一个View填充到容器中，该容器中只会有一个View。如果该View已经在容器中了，则不会有任何操作
     */
    static void displayViewOnViewGroup(View view, ViewGroup container, boolean isMatch) {
        ViewGroup.LayoutParams lp;
        if(isMatch) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        displayViewOnViewGroup(view, container, lp);
    }

    /**
     * 将一个View填充到容器中，该容器中只会有一个View。如果该View已经在容器中了，则不会有任何操作
     */
    private static void displayViewOnViewGroup(View view, ViewGroup container, ViewGroup.LayoutParams lp) {
        if(view == null) {
            throw new NullPointerException("view cannot be null");
        }
        if (container == null) {
            throw new NullPointerException("container cannot be null");
        }
        if (lp == null) {
            throw new NullPointerException("layout param cannot be null");
        }
        // 如果在容器中存在并且只有一个，则不进行操作
        if (container.indexOfChild(view) == -1 || container.getChildCount() != 1) {
            container.removeAllViews();
            container.addView(view, lp);
        }
    }

    static Activity findActivityFromContext(Context context) {
        if (context == null) {
            return null;
        } else if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return findActivityFromContext(((ContextWrapper) context).getBaseContext());
        } else {
            return null;
        }
    }
}
