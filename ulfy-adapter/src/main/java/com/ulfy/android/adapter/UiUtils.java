package com.ulfy.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

class UiUtils {

    /**
     * 根据视图模型创建对应的View
     */
    static View createView(Context context, View view, IViewModel model) {
        view = isViewCanReuse(view, model) ? view : createViewFromClazz(context, (Class<? extends View>) model.getViewClass());
        if (view != null && view instanceof IView) {
            ((IView)view).bind(model);
        }
        return view;
    }

    /**
     * 判断一个view是否可以重复使用
     */
    static boolean isViewCanReuse(View view, IViewModel model) {
        return view != null && model != null && view.getClass() == model.getViewClass();
    }

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
     * 设置View的点击事件，根据是否为空设置View的点击性
     */
    static void setViewClickListener(View view, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);
        view.setClickable(onClickListener != null || onLongClickListener != null);
    }

    /**
     * 清空view的父容器，使得view被添加到其它的容器中不会发生错误
     */
    static void clearParent(View view) {
        if(view != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup)view.getParent()).removeView(view);
        }
    }
}
