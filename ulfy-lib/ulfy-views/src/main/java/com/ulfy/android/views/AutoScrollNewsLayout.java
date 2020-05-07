package com.ulfy.android.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

/**
 * 自动横向滚动的新闻布局
 */
public class AutoScrollNewsLayout extends LinearLayout {
    private View autoScrollView;

    public AutoScrollNewsLayout(Context context) {
        this(context, null);
    }

    public AutoScrollNewsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (autoScrollView == null) {
            autoScrollView = child;
        } else {
            throw new IllegalStateException("AutoScrollNewsLayout can only accept one child view");
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 设置自动滚动的View的宽为无限大在重新测量一次
        autoScrollView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), heightMeasureSpec);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (autoScrollView != null) {
            autoScrollView.clearAnimation();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(autoScrollView, "translationX", getWidth(), -autoScrollView.getMeasuredWidth());
            objectAnimator.setDuration(14000);
            objectAnimator.setInterpolator(new LinearInterpolator());
            objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
            objectAnimator.start();
        }
    }
}

