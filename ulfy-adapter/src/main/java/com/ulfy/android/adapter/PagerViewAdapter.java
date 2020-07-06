package com.ulfy.android.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * ViewPager 适配器：该适配器用于简单的 View 列表，可以快速的将 View 和 ViewPager 连接起来
 */
public class PagerViewAdapter extends PagerAdapter {
    private List<View> viewList;

    public PagerViewAdapter() { }

    public PagerViewAdapter(View... views) {
        setViewList(views);
    }

    public PagerViewAdapter(List<View> viewList) {
        setViewList(viewList);
    }

    public PagerViewAdapter setViewList(View... views) {
        Objects.requireNonNull(views, "view list can not be null");
        setViewList(Arrays.asList(views));
        return this;
    }

    public PagerViewAdapter setViewList(List<View> viewList) {
        Objects.requireNonNull(viewList, "view list can not be null");
        for (View view : viewList) {
            UiUtils.clearParent(view);
        }
        this.viewList = viewList;
        return this;
    }

    @Override public Object instantiateItem(ViewGroup container, int position) {
        View view = viewList.get(position);
        UiUtils.clearParent(view);
        container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return view;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override public int getCount() {
        return viewList == null ? 0 : viewList.size();
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
