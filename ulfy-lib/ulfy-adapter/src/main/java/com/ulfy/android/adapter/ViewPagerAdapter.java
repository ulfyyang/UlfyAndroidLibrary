package com.ulfy.android.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 简单的ViewPager适配器，用于适配非动态生成的View
 */
public class ViewPagerAdapter extends PagerAdapter {
    private List<View> viewList = new ArrayList<>();

    public ViewPagerAdapter() { }

    public ViewPagerAdapter(View... views) {
        this(Arrays.asList(views));
    }

    public ViewPagerAdapter(List<View> viewList) {
        setViewList(viewList);
    }

    public ViewPagerAdapter setViewList(View... views) {
        setViewList(Arrays.asList(views));
        return this;
    }

    public ViewPagerAdapter setViewList(List<View> viewList) {
        this.viewList = viewList;
        if (viewList != null && !viewList.isEmpty()) {
            for (View view : viewList) {
                UiUtils.clearParent(view);
            }
        }
        return this;
    }


    @Override public Object instantiateItem(ViewGroup container, int position) {
        View view = viewList.get(position);
        UiUtils.clearParent(view);
        container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return view;
    }

    @Override public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override public int getCount() {
        return viewList == null ? 0 : viewList.size();
    }

    @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
