package com.ulfy.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

public class AppBarLayoutBehavior extends AppBarLayout.Behavior {     // 不需要特殊定制了，貌似AndroidX版本没有下拉回弹这些BUG了
    private static final String TAG = AppBarLayoutBehavior.class.getSimpleName();
    private static final boolean DEBUG = false;
    private boolean needLimit;          // 是否需要做出一些限制（只有当滚动区域的高度无法填充容器的时候才需要）
    private int distance = 0;           // 显示中的内容与容器高度的差值

    public AppBarLayoutBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 情况下默认，底部可滚动区域和外部容器具有相同的高度。当底部显示区域显示的内容非常少时：在向上滑动的过程中，
     * 就会显得特别空旷，看起来像一个大白板。因此我们需要对这种底部显示区域无法填满外部容器的情况进行限制。
     *
     * 1）当显示中的内容高度小于容器的高度时（此时显示中的内容的高度与容器的高度之差为负数），我们要禁止滚动。
     * 2）当显示中的内容高度大于容器的高度时（此时显示中的内容的高度与容器的高度之差为正数），要限制在这个差值内。
     *
     * 显示中的内容包括上方跟随滚动的区域和下方可滚动的区域。
     *
     * 需要注意的是，由于要对比可滚动区域的高度与外部容器的高度，所以可滚动区域的高度不可以是填充父容器，否则他们
     * 两个的高度就会始终一样了。
     */
    @Override public boolean setTopAndBottomOffset(int offset) {
        if (needLimit) {
            if (distance <= 0) {        // 差值为负时限制滚动
                offset = 0;
            } else if (Math.abs(offset) > distance) {   // 差值为正数时限制最大偏移位置
                offset = -distance;
            }
            if (DEBUG) {
                Log.d(TAG, String.format("setTopAndBottomOffset() after correct --> offset %d", offset));
            }
        }
        return super.setTopAndBottomOffset(offset);
    }

    @Override public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl, int layoutDirection) {
        calculateLimitState(parent, abl);
        return super.onLayoutChild(parent, abl, layoutDirection);
    }

    /**
     *  距离 = AppBarLayout内部可滚动View高度 + AppBarLayout内部悬浮布局高度 + 底部可滚动组件的高度 - CoordinatorLayout布局的高度
     */
    private void calculateLimitState(CoordinatorLayout parent, AppBarLayout appBarLayout) {
        int barScrollTotalH = appBarLayout.getTotalScrollRange();               // 该方法返回AppBarLayout中带有scroll属性的View的高总和
        int barUnScrollTotalH = getTotalUnScrollRange(appBarLayout);            // 该方法返回AppBarLayout中不带scroll属性的View的高总和
        int contentScrollH = getScrollContentHeight(parent);                    // 该方法返回滚动区域的高度
        int parentH = parent.getMeasuredHeight();
        needLimit = contentScrollH < parentH;
        distance = barScrollTotalH + barUnScrollTotalH + contentScrollH - parentH;
        if (DEBUG) {
            Log.d(TAG, String.format("calculateLimitState() --> barScrollTotalH %d, barUnScrollTotalH %d, contentScrollH %d, parentH %d, needLimit %b, distance %d",
                    barScrollTotalH, barUnScrollTotalH, contentScrollH, parentH, needLimit, distance));
        }
    }

    /**
     * 获取AppBarLayout中所有不带scroll属性的View高度总和
     */
    private static int getTotalUnScrollRange(AppBarLayout appBarLayout) {
        int range = 0;
        for (int i = 0; i < appBarLayout.getChildCount(); i++) {
            View view = appBarLayout.getChildAt(i);
            AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) view.getLayoutParams();
            if ((lp.getScrollFlags() & AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL) == 0) {
                range += view.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            }
        }
        return range;
    }

    /**
     * 只取第二个View，暂时不考虑其它的情况
     */
    private static int getScrollContentHeight(CoordinatorLayout parent) {
        View view = parent.getChildAt(1);
        if (view == null) {
            return 0;
        } else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            return view.getMeasuredHeight() + params.topMargin + params.bottomMargin;
        }
    }
}
