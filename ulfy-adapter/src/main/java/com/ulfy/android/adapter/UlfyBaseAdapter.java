package com.ulfy.android.adapter;

import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

/**
 * 适配器基类
 */
abstract class UlfyBaseAdapter extends BaseAdapter {
    private View emptyView;

    /**
     * 是否有数据为空显示的界面
     * @return 判定的结果
     */
    protected final boolean isHaveEmptyView() {
        return emptyView != null;
    }

    /**
     * 获取数据为空时显示的界面
     * @return 数据为空时显示的界面
     */
    protected final View getEmptyView() {
        UiUtils.clearParent(emptyView);
        if (emptyView.getLayoutParams() != null) {
            emptyView.setLayoutParams(new AbsListView.LayoutParams(emptyView.getLayoutParams()));
        }
        emptyView.setVisibility(View.VISIBLE);
        return emptyView;
    }

    /**
     * 设置数据为空时显示的界面
     * @param emptyView    数据为空时显示的界面
     */
    public final void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        UiUtils.clearParent(this.emptyView);
        emptyView.setVisibility(View.GONE);
    }
}
