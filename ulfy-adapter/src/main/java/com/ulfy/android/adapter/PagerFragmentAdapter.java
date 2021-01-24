package com.ulfy.android.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * ViewPager 适配器：该适配器用于简单的 Fragment 列表，可以快速的将 Fragment 和 ViewPager 连接起来
 */
public class PagerFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList;

    public PagerFragmentAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
    }

    public PagerFragmentAdapter(FragmentActivity activity, Fragment... fragments) {
        super(activity.getSupportFragmentManager());
        setFragmentList(fragments);
    }

    public PagerFragmentAdapter(FragmentActivity activity, List<Fragment> fragmentList) {
        super(activity.getSupportFragmentManager());
        setFragmentList(fragmentList);
    }

    public PagerFragmentAdapter setFragmentList(Fragment... fragmentList) {
        Objects.requireNonNull(fragmentList, "fragment list can not be null");
        return setFragmentList(Arrays.asList(fragmentList));
    }

    public PagerFragmentAdapter setFragmentList(List<Fragment> fragmentList) {
        Objects.requireNonNull(fragmentList, "fragment list can not be null");
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
