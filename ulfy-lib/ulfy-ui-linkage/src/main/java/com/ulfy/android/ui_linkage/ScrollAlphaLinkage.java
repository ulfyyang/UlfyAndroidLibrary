package com.ulfy.android.ui_linkage;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * 目前只支持NestedScrollView、RecyclerView。NestedScrollView会占用设置混动回调
 */
public class ScrollAlphaLinkage {
    /**
     * 操作透明度的目标，默认为背景
     * 如果采用背景色，则布局中的背景色不可以设置透明度
     */
    public static final int TYPE_BACKGROUND = 1;
    public static final int TYPE_VIEW = 2;
    /**
     * 从不显示到显示需要的距离
     */
    public static final float DISTANCE_SHORT = 0.5f;
    public static final float DISTANCE_MIDDLE = 1f;
    public static final float DISTANCE_LONG = 2f;
    /**
     * 对象字段
     */
    private View targetView;
    private int type;
    private float distance = DISTANCE_MIDDLE;

    public ScrollAlphaLinkage(View scrollableView, View targetView) {
        this(scrollableView, targetView, TYPE_BACKGROUND, DISTANCE_MIDDLE);
    }

    public ScrollAlphaLinkage(View scrollableView, View targetView, int type) {
        this(scrollableView, targetView, type, DISTANCE_MIDDLE);
    }

    public ScrollAlphaLinkage(View scrollableView, View targetView, int type, float distance) {
        if (scrollableView instanceof NestedScrollView) {
            ((NestedScrollView) scrollableView).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    ScrollAlphaLinkage.this.onScrollChange(scrollY);
                }
            });
        } else if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    ScrollAlphaLinkage.this.onScrollChange(dy);
                }
            });
        }
        this.targetView = targetView;
        this.type = type;
        this.distance = distance;
        if (type == TYPE_BACKGROUND) {
            if (targetView.getBackground() != null) {
                targetView.getBackground().mutate().setAlpha(0);
            }
        } else if (type == TYPE_VIEW) {
            targetView.setAlpha(0);
        }
    }

    private void onScrollChange(int scrollViewTop) {
        scrollViewTop /= distance;      // 计算新距离
        if (scrollViewTop > 255) {      // 限定距离范围
            scrollViewTop = 255;
        }
        if (type == TYPE_BACKGROUND) {  // 设置透明度
            if (targetView.getBackground() != null) {
                targetView.getBackground().mutate().setAlpha(scrollViewTop);
            }
        } else {
            targetView.setAlpha(scrollViewTop * 1.0f / 255);
        }
    }
}
