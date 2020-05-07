package com.ulfy.android.ui_linkage;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 简单的ViewPager适配器，用于适配非动态生成的Fragment
 */
class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private List<Fragment> fragmentList = new ArrayList<>();

    public FragmentPagerAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
    }

    public FragmentPagerAdapter(FragmentActivity activity, Fragment... fragmentList) {
        this(activity, Arrays.asList(fragmentList));
    }

    public FragmentPagerAdapter(FragmentActivity activity, List<Fragment> fragmentList) {
        super(activity.getSupportFragmentManager());
        this.setFragmentList(fragmentList);
    }

    public FragmentPagerAdapter setFragmentList(Fragment... fragmentList) {
        return setFragmentList(Arrays.asList(fragmentList));
    }

    public FragmentPagerAdapter setFragmentList(List<Fragment> fragmentList) {
        this.fragmentList = fragmentList;
        return this;
    }

    @Override public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override public int getCount() {
        return fragmentList == null ? 0 : fragmentList.size();
    }
}
