package com.ulfy.android.ui_linkage;

import android.view.View;
import android.view.ViewGroup;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 目前只支持NestedScrollView、RecyclerView。NestedScrollView会占用设置混动回调
 */
public class ScrollPullLinkage {
    private View targetView;
    private float speed = 1.0f;                         // 跟随滚动的速录，越小速度越慢
    private int lastScrollViewTop = -1, delta = 0;      // 记录上次滚动的位置，滚动便宜量（向上为正，向下为负）

    public ScrollPullLinkage(View scrollableView, View targetView) {
        this(scrollableView, targetView, 1.0f);
    }

    public ScrollPullLinkage(View scrollableView, View targetView, float speed) {
        if (scrollableView instanceof NestedScrollView) {
            ((NestedScrollView) scrollableView).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    ScrollPullLinkage.this.onScrollChange(scrollY);
                }
            });
        } else if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    ScrollPullLinkage.this.onScrollChange(dy);
                }
            });
        }
        this.targetView = targetView;
        this.speed = speed;
        this.targetView.setVisibility(View.INVISIBLE);
    }



    private void onScrollChange(int scrollViewTop) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) targetView.getLayoutParams();
        // 首次滚动进行初始化
        if (targetView.getVisibility() == View.INVISIBLE) {
            lastScrollViewTop = scrollViewTop;
            layoutParams.topMargin = -targetView.getHeight();
            targetView.requestLayout();
            targetView.setVisibility(View.VISIBLE);
        }
        // 跟随滚动调整目标位置
        else {
            delta = scrollViewTop - lastScrollViewTop; lastScrollViewTop = scrollViewTop;
            layoutParams.topMargin = (int) (layoutParams.topMargin - delta * speed);
            if (layoutParams.topMargin < -targetView.getHeight()) {
                layoutParams.topMargin = -targetView.getHeight();
            }
            if (layoutParams.topMargin > 0) {
                layoutParams.topMargin = 0;
            }
            targetView.requestLayout();
        }
    }
}
