package com.ulfy.android.ui_linkage;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PagerLinkage {
    private ViewPager viewPagerContainer;
    private ViewGroup viewGroupContainer;
    private int viewGroupContainerId;
    private List<View> viewTabList;
    private List<View> viewPageList;
    private List<Fragment> fragmentPageList;
    private List<OnTabSelectedListener> onTabSelectedListenerList = new ArrayList<>();
    private ClickFilter clickFilter;
    private int currentIndexRecord = -1;        // 记录当前显示的标签页，用于过滤点击事件的多次回调

    public PagerLinkage setContainer(ViewPager viewPager) {
        this.viewPagerContainer = viewPager;
        return this;
    }

    public PagerLinkage setContainer(ViewGroup viewGroupContainer) {
        this.viewGroupContainer = viewGroupContainer;
        return this;
    }

    public PagerLinkage setContainer(int viewGoroupId) {
        this.viewGroupContainerId = viewGoroupId;
        return this;
    }

    public PagerLinkage initViewTabs(View... tabs) {
        return initViewTabs(Arrays.asList(tabs));
    }

    public PagerLinkage initViewTabs(List<View> tabs) {
        this.viewTabList = tabs;
        return this;
    }

    public PagerLinkage initViewPages(View... pages) {
        return initViewPages(Arrays.asList(pages));
    }

    public PagerLinkage initViewPages(List<View> pages) {
        this.viewPageList = pages;
        return this;
    }

    public PagerLinkage initFragmentPages(Fragment... fragments) {
        return this.initFragmentPages(Arrays.asList(fragments));
    }

    public PagerLinkage initFragmentPages(List<Fragment> fragments) {
        this.fragmentPageList = fragments;
        return this;
    }

    public PagerLinkage addOnTabSelectedListener(final OnTabSelectedListener onTabSelectedListener) {
        this.onTabSelectedListenerList.add(onTabSelectedListener);
        return this;
    }

    public PagerLinkage setClickFilter(ClickFilter clickFilter) {
        this.clickFilter = clickFilter;
        return this;
    }

    /**
     * 点击过滤器
     *      用于确定点击的Tab是否生效，如果不生效则点击没有反应
     *      该过滤器只能控制Tab点击，如果有滑动支持则滑动仍然可以进行跳转
     */
    public interface ClickFilter {
        boolean canClick(int index);
    }

    public PagerLinkage build() {
        // 设置Tab标签的点击事件
        if (viewTabList != null && viewTabList.size() > 0) {
            for (View view : viewTabList) {
                view.setOnClickListener(new OnTabClickListenerInner());
            }
        }
        // 合并具体页到容器中
        if (viewPagerContainer != null) {
            combineWithViewPager();
        } else {
            combineWithoutViewPager();
        }
        return this;
    }

    public PagerLinkage select(int index) {
        onTabShow(index);
        onPageShow(index);
        if (currentIndexRecord != index) {
            currentIndexRecord = index;
            for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
                onTabSelectedListener.onTabSelected(index);
            }
        }
        return this;
    }

    // Tab标签的点击事件
    private class OnTabClickListenerInner implements View.OnClickListener {
        @Override public void onClick(View v) {
            if (viewTabList != null && viewTabList.size() > 0) {
                int index = viewTabList.indexOf(v);
                if (clickFilter == null || clickFilter.canClick(index)) {
                    select(index);
                }
            }
        }
    }

    // ViewPager页面切换事件
    private class OnViewPagerPageChangeListenerInner extends ViewPager.SimpleOnPageChangeListener {
        @Override public void onPageSelected(int position) {
            select(position);
        }
    }

    private PagerLinkage combineWithViewPager() {
        // 关联ViewPager
        if (viewPageList != null) {
            viewPagerContainer.setAdapter(new ViewPagerAdapter(viewPageList));
            viewPagerContainer.setOffscreenPageLimit(viewPageList.size() - 1);
        } else if (fragmentPageList != null) {
            viewPagerContainer.setAdapter(new FragmentPagerAdapter((FragmentActivity) viewPagerContainer.getContext(), fragmentPageList));
            viewPagerContainer.setOffscreenPageLimit(fragmentPageList.size() - 1);
        }
        viewPagerContainer.addOnPageChangeListener(new OnViewPagerPageChangeListenerInner());
        return this;
    }

    private PagerLinkage combineWithoutViewPager() {
        if (viewGroupContainer != null) {
            viewGroupContainer.removeAllViews();
        }
        // 如果设置了View则添加View到容器中
        if (viewPageList != null && viewPageList.size() > 0) {
            for (View viewPage : viewPageList) {
                UiUtils.clearParent(viewPage);
                viewGroupContainer.addView(viewPage, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
        // 如果设置了Fragment则添加Fragment到容器中
        if (fragmentPageList != null && fragmentPageList.size() > 0) {
            FragmentManager fragmentManager = ((FragmentActivity) UiUtils.findActivityFromContext(findBindActivityContext())).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (int i = 0; i < fragmentPageList.size(); i++) {
                String fragmentTagName = makeFragmentName(i);
                Fragment fragment = fragmentManager.findFragmentByTag(fragmentTagName);
                if (fragment == null) {
                    fragmentTransaction.add(viewGroupContainerId, fragmentPageList.get(i), fragmentTagName);
                } else {
                    fragmentTransaction.attach(fragment);
                    fragmentPageList.set(i, fragment);
                }
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
        return this;
    }

    private PagerLinkage onTabShow(int index) {
        // 处理Tab选中的效果，默认通过selecte状态来改变
        if (viewTabList != null && viewTabList.size() > 0) {
            for (int i = 0; i < viewTabList.size(); i++) {
                viewTabList.get(i).setSelected(i == index);
            }
        }
        return this;
    }

    private PagerLinkage onPageShow(int index) {
        // 针对ViewPager的页面切换
        if (viewPagerContainer != null) {
            viewPagerContainer.setCurrentItem(index);
        }
        // 针对ViewGroup中普通View的页面切换
        else if (viewGroupContainer != null) {
            for (int i = 0; i < viewPageList.size(); i++) {
                viewPageList.get(i).setVisibility(i == index ? View.VISIBLE : View.GONE);
            }
        }
        // 针对ViewGroup中Fragment的页面切换
        else if (viewGroupContainerId > 0) {
            FragmentManager fragmentManager = ((FragmentActivity) UiUtils.findActivityFromContext(findBindActivityContext())).getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (int i = 0; i < fragmentPageList.size(); i++) {
                if (i == index) {
                    fragmentTransaction.show(fragmentPageList.get(i));
                } else {
                    fragmentTransaction.hide(fragmentPageList.get(i));
                }
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
        return this;
    }

    /**
     * 借鉴FragmentPagerAdapter的处理，防止在Activity不可见时ViewPager被保存时界面处理失败
     */
    private String makeFragmentName(long id) {
        return "android:switcher:" + viewGroupContainerId + ":" + id;
    }

    private Context findBindActivityContext() {
        if (viewPagerContainer != null) {
            return viewPagerContainer.getContext();
        }
        if (viewGroupContainer != null) {
            return viewGroupContainer.getContext();
        }
        if (viewTabList != null && viewTabList.size() > 0) {
            return viewTabList.get(0).getContext();
        }
        if (viewPageList != null && viewPageList.size() > 0) {
            return viewPageList.get(0).getContext();
        }
        if (fragmentPageList != null && fragmentPageList.size() > 0) {
            return fragmentPageList.get(0).getContext();
        }
        return null;
    }
}
