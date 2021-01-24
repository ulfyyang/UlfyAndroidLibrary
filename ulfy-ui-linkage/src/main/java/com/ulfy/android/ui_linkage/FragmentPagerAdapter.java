package com.ulfy.android.ui_linkage;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * 简单的ViewPager适配器，用于适配非动态生成的Fragment
 */
class FragmentPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragmentList;

    public FragmentPagerAdapter(FragmentManager fragmentManager, List<Fragment> fragmentList) {
        super(fragmentManager);
        this.fragmentList = fragmentList;
    }

    @Override public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override public int getCount() {
        return fragmentList == null ? 0 : fragmentList.size();
    }
}
