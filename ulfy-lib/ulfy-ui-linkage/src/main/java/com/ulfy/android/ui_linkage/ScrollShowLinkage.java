package com.ulfy.android.ui_linkage;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 目前只支持NestedScrollView、RecyclerView。NestedScrollView会占用设置混动回调
 */
public class ScrollShowLinkage {
    private int topViewLastYPosition = 0, bottomViewLastYPosition = 0;
    private View topView;       // 在上方的View，如果传null则使用最顶端高度
    private View bottomView;    // 在下方的View
    private View showView;      // 需要切换可见性的View
    private int hideCode;       // 需要切换可见性时使用的属性，默认为View.INVISIBLE
    private ShowStrategy showStrategy;

    public ScrollShowLinkage(View scrollableView, View topView, View bottomView, View showView) {
        this(scrollableView, topView, bottomView, showView, View.INVISIBLE);
    }

    public ScrollShowLinkage(View scrollableView, View topView, View bottomView, View showView, int hideCode) {
        setScrollableViewListener(scrollableView);
        this.topView = topView;
        this.bottomView = bottomView;
        this.showView = showView;
        this.hideCode = hideCode;
        this.showStrategy = new BottomTopStrategy();
        showView.setVisibility(hideCode);
    }

    private void setScrollableViewListener(View scrollableView) {
        if (scrollableView instanceof NestedScrollView) {
            ((NestedScrollView) scrollableView).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    ScrollShowLinkage.this.onScrollChange();
                }
            });
        } else if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    ScrollShowLinkage.this.onScrollChange();
                }
            });
        }
    }

    private void onScrollChange() {
        /*
        如果bottomView不可见，则无法得到其Y轴位置，此时Y轴为0
        此时将会导致topPosition >= bottomPosition永远成立，也就是不满足条件的时候showView也会可见
        所以在这种情况下设置showView不可见
         */
        if (bottomView.getVisibility() == View.GONE) {
            showView.setVisibility(hideCode);
        } else if (showStrategy != null) {
            int topViewTopPosition = topView == null ? 0 : getTopViewYPosition();
            int topViewBottomPosition = topViewTopPosition + (topView == null ? 0 : topView.getHeight());
            int bottomViewTopPosition = bottomView == null ? 0 : getBottomViewYPosition();
            int bottomViewBottomPosition = bottomViewTopPosition + (bottomView == null ? 0 : bottomView.getHeight());
            boolean shouldShow = showStrategy.shouldShow(topViewTopPosition, topViewBottomPosition, bottomViewTopPosition, bottomViewBottomPosition);
            showView.setVisibility(shouldShow ? View.VISIBLE : hideCode);
        }
    }

    /**
     * 为兼容小米手机，跳过0这个特有的像素
     */

    private int getTopViewYPosition() {
        int[] position = new int[2];
        topView.getLocationInWindow(position);
        if (position[1] != 0) {
            topViewLastYPosition = position[1];
        }
        return topViewLastYPosition;
    }

    private int getBottomViewYPosition() {
        int[] position = new int[2];
        bottomView.getLocationInWindow(position);
        if (position[1] != 0) {
            bottomViewLastYPosition = position[1];
        }
        return bottomViewLastYPosition;
    }


    public ScrollShowLinkage setShowStrategy(ShowStrategy showStrategy) {
        this.showStrategy = showStrategy;
        return this;
    }

    public interface ShowStrategy {
        public boolean shouldShow(int topViewTopPosition, int topViewBottomPosition, int bottomViewTopPosition, int bottomViewBottomPosition);
    }

    /*
        topView、bottomView是根据其相对位置决定的。页面刚开始显示的时候在上面的为topView，在下面的为bottomView
     */

    /**
     * 当topView顶部低于bottomView顶部时显示
     *      在上滑过程中，bottomView的顶部逐步升高直到超过topView的顶部
     * 常用于超出容器
     */
    public static class TopTopStrategy implements ShowStrategy {
        public boolean shouldShow(int topViewTopPosition, int topViewBottomPosition, int bottomViewTopPosition, int bottomViewBottomPosition) {
            return topViewTopPosition >= bottomViewTopPosition;
        }
    }

    /**
     * 当topView顶部低于bottomView底部时显示
     *      在上滑过程中，bottomView的底部逐步升高直到超过topView的顶部
     * 常用于超出容器
     */
    public static class TopBottomStrategy implements ShowStrategy {
        public boolean shouldShow(int topViewTopPosition, int topViewBottomPosition, int bottomViewTopPosition, int bottomViewBottomPosition) {
            return topViewTopPosition >= bottomViewBottomPosition;
        }
    }

    /**
     * 当topView底部低于bottomView顶部时显示
     *      在上滑过程中，bottomView的顶部逐步升高直到超过topView的底部
     * 常用于上滑悬浮
     */
    public static class BottomTopStrategy implements ShowStrategy {
        public boolean shouldShow(int topViewTopPosition, int topViewBottomPosition, int bottomViewTopPosition, int bottomViewBottomPosition) {
            return topViewBottomPosition >= bottomViewTopPosition;
        }
    }

    /**
     * 当topView底部低于bottomView底部时显示
     *      在上滑过程中，bottomView的底部逐步升高直到超过topView的底部
     * 常用于标题置顶
     */
    public static class BottomBottomStrategy implements ShowStrategy {
        public boolean shouldShow(int topViewTopPosition, int topViewBottomPosition, int bottomViewTopPosition, int bottomViewBottomPosition) {
            return topViewBottomPosition >= bottomViewBottomPosition;
        }
    }
}
