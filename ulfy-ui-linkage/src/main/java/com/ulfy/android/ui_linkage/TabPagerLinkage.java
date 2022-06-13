package com.ulfy.android.ui_linkage;

import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * TableLayout联动，使用方式：
 *  1. 设置基础属性
 *  2. 调用 {@link #build()} -> {@link #select(int)}
 *  3. 页面显示
 */
public class TabPagerLinkage {
    private static final String TAG = TabPagerLinkage.class.getSimpleName();
    // 下划线宽度：填充可用空间 (仅在 MODE_FIXED 下有效)
    public static final int LINE_WIDTH_MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    // 下划线宽度：包裹标签内容
    public static final int LINE_WIDTH_WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    // 基础属性，调用下方一系列的设置方法中设置
    private Fragment parentFragment;                    // 如果在 Fragment 里边嵌套 Fragment，则该参数为父 Fragment
    private TabLayout tabLayout;                        // 设置系统 TabLayout
    private ViewPager viewPagerContainer;               // 设置内容显示区域容器：ViewPager
    private ViewGroup viewGroupContainer;               // 设置内容显示区域容器：ViewGroup
    private int viewGroupContainerId;                   // 设置内容显示区域容器：ViewGroup，当显示内容为Fragment时使用
    private List<String> stringTabList;                 // 设置标签：字符串数组(目前暂未开放定制样式接口)
    private List<View> viewTabList;                     // 设置标签：View数组
    private List<View> viewPageList;                    // 设置显示内容：View数组
    private List<Fragment> fragmentPageList;            // 设置显示内容：Fragment数组(注意和显示容器配合)
    private boolean autoScrollMode;                     // 当内容显示不下时是否自动切换到滚动模式
    private int lineWidth = LINE_WIDTH_MATCH_PARENT;    // 分割线宽度：包裹内容、填充父控件、指定宽度(仅在Api23中有效)
    private int dividerWidth = 0;                       // 标签 Tab 之间的间距，单位像素
    private final List<OnTabSelectedListener> onTabSelectedListenerList = new ArrayList<>();
    // 辅助属性，不对外开放
    private boolean init;       // 用于记录容器的初始宽度
    private final List<View> convertedTabViewList = new ArrayList<>();  // 将传入的Tab参数包装为统一的View，便于统一处理

    public TabPagerLinkage setParentFragment(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        return this;
    }

    public TabPagerLinkage setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
        return this;
    }

    public TabPagerLinkage setContainer(ViewPager viewPager) {
        this.viewPagerContainer = viewPager;
        return this;
    }

    public TabPagerLinkage setContainer(ViewGroup viewGroup) {
        this.viewGroupContainer = viewGroup;
        return this;
    }

    public TabPagerLinkage setContainer(int viewGroupId) {
        this.viewGroupContainerId = viewGroupId;
        return this;
    }

    public TabPagerLinkage initStringTabs(String... tabs) {
        return initStringTabs(Arrays.asList(tabs));
    }

    public TabPagerLinkage initStringTabs(List<String> tabs) {
        this.stringTabList = tabs;
        return this;
    }

    public TabPagerLinkage initViewTabs(View... tabs) {
        return initViewTabs(Arrays.asList(tabs));
    }

    public TabPagerLinkage initViewTabs(List<View> tabs) {
        this.viewTabList = tabs;
        return this;
    }

    public TabPagerLinkage initViewPages(View... pages) {
        return initViewPages(Arrays.asList(pages));
    }

    public TabPagerLinkage initViewPages(List<View> pages) {
        this.viewPageList = pages;
        return this;
    }

    public TabPagerLinkage initFragmentPages(Fragment... fragments) {
        return this.initFragmentPages(Arrays.asList(fragments));
    }

    public TabPagerLinkage initFragmentPages(List<Fragment> fragments) {
        this.fragmentPageList = fragments;
        return this;
    }

    public TabPagerLinkage setAutoScrollMode(boolean autoScrollMode) {
        this.autoScrollMode = autoScrollMode;
        return this;
    }

    public TabPagerLinkage setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public TabPagerLinkage setLineWidthDP(int lineWidth) {
        return setLineWidth(UiUtils.dp2px(lineWidth));
    }

    public TabPagerLinkage setDividerWidth(int dividerWidth) {
        this.dividerWidth = dividerWidth;
        return this;
    }

    public TabPagerLinkage setDividerWidthDP(int dividerWidth) {
        return setDividerWidth(UiUtils.dp2px(dividerWidth));
    }

    public TabPagerLinkage addOnTabSelectedListener(final OnTabSelectedListener onTabSelectedListener) {
        this.onTabSelectedListenerList.add(onTabSelectedListener);
        return this;
    }


    /**
     * 构建方法，将设置的基础属性绑定到 {@link TabLayout}，只支持调用一次
     */
    public TabPagerLinkage build() {
        Log.d(TAG, "build TabPager start ...");
        // 将客户端设置的标题参数转化为统一的View列表
        convertToTabViewList();
        // 合并具体页到容器中
        if (viewPagerContainer != null) {
            combineWithViewPager();
        } else {
            combineWithoutViewPager();
        }
        // 绑定相关事件
        bindOnTabSelectedListener();
        updateOnLayoutChanged();
        Log.d(TAG, "build TabPager end");
        return this;
    }

    /**
     * 将传入的Tab参数包装为统一的View，便于统一处理。作为 {@link #build()} 的一个步骤
     *  1. 如果是字符串则包装为 TextView，未来考虑开放转换过程
     *  2. 其它的 View 则直接使用
     * 将 {@link #stringTabList} {@link #viewTabList} 包装到 {@link #convertedTabViewList} 中
     */
    private void convertToTabViewList() {
        convertedTabViewList.clear();
        if (stringTabList != null) {
            for (int i = 0; i < stringTabList.size(); i++) {
                TextView textView = new TextView(UiUtils.findActivityFromContext(tabLayout.getContext()));
                textView.setText(stringTabList.get(i));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(tabLayout.getTabTextColors());
                convertedTabViewList.add(textView);
            }
            Log.d(TAG, "converted string list to tabs");
        } else {
            convertedTabViewList.addAll(viewTabList);
            Log.d(TAG, "converted view list to tabs");
        }
    }

    /**
     * 关联ViewPager。作为 {@link #build()} 的一个步骤
     *  1. ViewPager 对应的显示容器为 {@link #viewPagerContainer}
     *  2. TabLayout 和 ViewPager 之间的联动关系由 {@link TabLayout#setupWithViewPager(ViewPager)} 自动维护
     * 对于显示内容，主要处理两种情况：普通View({@link #viewPageList})、Fragment({@link #fragmentPageList})
     */
    private void combineWithViewPager() {
        if (viewPageList != null) {
            viewPagerContainer.setAdapter(new ViewPagerAdapter(viewPageList));
            viewPagerContainer.setOffscreenPageLimit(viewPageList.size() - 1);
            Log.d(TAG, "combined view page list to ViewPager");
        } else if (fragmentPageList != null) {
            FragmentManager fragmentManager = findFragmentManager();
            viewPagerContainer.setAdapter(new FragmentPagerAdapter(fragmentManager, fragmentPageList));
            viewPagerContainer.setOffscreenPageLimit(fragmentPageList.size() - 1);
            Log.d(TAG, "combined fragment page list to ViewPager");
        }
        tabLayout.setupWithViewPager(viewPagerContainer);
        for (int i = 0; i < convertedTabViewList.size(); i++) {
            View view = convertedTabViewList.get(i);
            TabLayout.Tab tab = Objects.requireNonNull(tabLayout.getTabAt(i));
            tab.setCustomView(view);
        }
        Log.d(TAG, "combined tab list to TabLayout");
        Log.d(TAG, "TabLayout combined with ViewPager");
    }

    /**
     * 关联ViewGroup。作为 {@link #build()} 的一个步骤
     *  1. ViewGroup 对应的显示容器为 {@link #viewGroupContainer}
     *  2. TabLayout 和 ViewGroup 之间的联动关系由 {@link #onPageShowForViewGroup(int)} 自动维护
     * 对于显示内容，主要处理两种情况：普通View({@link #viewPageList})、Fragment({@link #fragmentPageList})
     *
     * Fragment 会被安卓系统托管，当从缓存中恢复后，{@link #fragmentPageList} 内部会替换为恢复的 Fragment
     *  1. 这会导致外部保存的 Fragment 与实际显示的 Fragment 不相同，外部调用 Fragment 方法不生效
     */
    private void combineWithoutViewPager() {
        // 必须先添加，因为后添加的话由于添加Tab的时候会触发点击回调导致在Fragment还没有恢复的时候进行则切换选择
        if (viewPageList != null) {
            viewGroupContainer.removeAllViews();
            for (View viewPage : viewPageList) {
                UiUtils.clearParent(viewPage);
                viewGroupContainer.addView(viewPage, -1, -1);
            }
            Log.d(TAG, "combined view page list to ViewGroup");
        } else if (fragmentPageList != null) {
            FragmentManager fragmentManager = findFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (int i = 0; i < fragmentPageList.size(); i++) {
                String tagName = makeFragmentName(i);
                Fragment fragment = fragmentManager.findFragmentByTag(tagName);
                if (fragment == null) {
                    fragment = fragmentPageList.get(i);
                    fragmentTransaction.add(viewGroupContainerId, fragment, tagName);
                } else {
                    fragmentTransaction.attach(fragment);
                    fragmentPageList.set(i, fragment);
                    Log.d(TAG, "attach fragment from finding tag: " + tagName);
                }
            }
            fragmentTransaction.commitAllowingStateLoss();
            Log.d(TAG, "combined fragment page list to ViewGroup");
        }
        // 在添加Tab的时候会默认选中第一个，并触发一次回调
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
            @Override public void onTabSelected(TabLayout.Tab tab) {
                onPageShowForViewGroup(tab.getPosition());
            }
        });
        for (int i = 0; i < convertedTabViewList.size(); i++) {
            View view = convertedTabViewList.get(i);
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setCustomView(view);
            tabLayout.addTab(tab);
        }
        Log.d(TAG, "combined tab list to TabLayout");
        Log.d(TAG, "TabLayout combined with ViewGroup");
    }

    /**
     * 当切换显示页面时，调用该方法切换显示内容
     *  1. ViewPager 与 TabLayout 的切换显示会自动维护，无需再这里实现
     *  2. 该方法仅提供 {@link #combineWithoutViewPager()} 使用
     */
    private TabPagerLinkage onPageShowForViewGroup(int index) {
        if (viewPageList != null) {
            for (int i = 0; i < viewPageList.size(); i++) {
                viewPageList.get(i).setVisibility(i == index ? View.VISIBLE : View.GONE);
            }
        } else if (fragmentPageList != null) {
            FragmentManager fragmentManager = findFragmentManager();
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
     * 绑定选中回调，当内部触发选中状态时会回调设置的多个回调监听。作为 {@link #build()} 的一个步骤
     */
    private void bindOnTabSelectedListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
            @Override public void onTabSelected(TabLayout.Tab tab) {
                for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
                    onTabSelectedListener.onTabSelected(tab.getPosition());
                }
            }
        });
    }

    /**
     * 当布局更新后更新 TabLayout 相关布局和样式。作为 {@link #build()} 的一个步骤
     *  1. 只能注册一次，如果不移除会造成频繁回调，引发界面显示BUG
     *  2. 当样式、大小更改后需要手动调用该方法
     *  3. 必须要包装到 OnGlobalLayoutListener 中，否则界面初始化时无法获取控件大小
     */
    public void updateOnLayoutChanged() {
        // 当宽或高存在时，说明页面布局已经完成，可以直接更新
        if (tabLayout.getWidth() != 0 || tabLayout.getHeight() != 0) {
            Log.d(TAG, "layout changed, adjust TabLayout start ...");
            updateOnLayoutChangeInner();
            Log.d(TAG, "layout changed, adjust TabLayout end");
        } else {
            tabLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                    Log.d(TAG, "layout changed, adjust TabLayout start ...");
                    updateOnLayoutChangeInner();
                    Log.d(TAG, "layout changed, adjust TabLayout end");
                    tabLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
    }

    /**
     * 更新自己，调整空间大小与协调位置
     */
    private void updateOnLayoutChangeInner() {
        resetTabSetting();                      // 重置Tab默认的设置，这些设置会影响布局
        modeFixedToScrollIfNotBig();            // 先进行一轮测量，当所需宽度不够时根据模式自动切换到滚动模式
        adjustDividerWidthForNormal();          // 调整左右外边距以形成Tab之间间隔的效果
        adjustDividerWithForFixedWrap();        // 调整左右外边距以适应FIXED模式下指示器包裹内容的效果
        adjustLineWidthForValue();              // 对指定长度的指示器长度做调整
    }

    /**
     * 选中页面进行显示，该方法会触发 {@link OnTabSelectedListener#onTabSelected(int)} 方法
     *  1. 内部实现有做仿重复触发机制，当前正在显示的 Tab 不会重复触发
     * @param index 显示的位置
     */
    public TabPagerLinkage select(int index) {
        Objects.requireNonNull(tabLayout.getTabAt(index)).select();
        if (init) {
            return this;
        }
        init = true;
        this.addOnTabSelectedListener(i -> Log.d(TAG, "TabLayout tab selected index: " + i));
        if (index == 0) {   // 第一次加载且处在第一页时，onTabSelected 回调不会被触发，需要手动触发
            for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
                onTabSelectedListener.onTabSelected(index);
            }
        }
        return this;
    }


    /**
     * 移除 Tab 内置的一些设置
     */
    private void resetTabSetting() {
        // 移除Tab内置的左右内边距
        LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            View tabView = mTabStrip.getChildAt(i);
            tabView.setPadding(0, 0, 0, 0);
        }
        // 移除Tab最小宽度限制
        // 这样，在滚动模式下无需做其它设置即可实现：下划线包裹内容
        try {
            Field field = tabLayout.getClass().getDeclaredField("requestedTabMinWidth");
            field.setAccessible(true);
            field.set(tabLayout, 0);
            field = tabLayout.getClass().getDeclaredField("scrollableTabMinWidth");
            field.setAccessible(true);
            field.set(tabLayout, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            Objects.requireNonNull(tabLayout.getTabAt(i)).view.setMinimumWidth(0);
        }
    }

    /**
     * 当TabView需要的宽度超出了容器的宽度，切换为可滚动模式
     *  1. 滚动模式下会自动将 {@link #lineWidth} 从 {@link #LINE_WIDTH_MATCH_PARENT} 切换到 {@link #LINE_WIDTH_WRAP_CONTENT}
     * 仅支持的模式：
     *  1. 手动开启了自动转滚动模式，且当前模式为MODE_FIXED
     * 测试方法：
     *  1. 在 {@link TabLayout#MODE_FIXED} 模式下放置大量的Tab，迫使触发自动切换
     */
    private void modeFixedToScrollIfNotBig() {
        if (!autoScrollMode || tabLayout.getTabMode() != TabLayout.MODE_FIXED) {
            return;
        }
        int containerViewWith = calculateTabLayoutWidth();
        int totalTabViewNeedWidth = calculateTabsWidth() + dividerWidth * (tabLayout.getTabCount() - 1);
        if (totalTabViewNeedWidth < 0) {
            totalTabViewNeedWidth = 0;
        }
        // 根据是否超出范围切换为滚动模式
        if (totalTabViewNeedWidth > containerViewWith) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            if (lineWidth == LINE_WIDTH_MATCH_PARENT) { // 只对MATCH_PARENT切换，固定值不切换
                lineWidth = LINE_WIDTH_WRAP_CONTENT;
            }
            Log.d(TAG, String.format(
                "%d is not enough for tabs need %d, convert TabLayout mode to MODE_SCROLLABLE",
                containerViewWith, totalTabViewNeedWidth
            ));
        } else {
            Log.d(TAG, String.format(
                "%d is enough for tabs need %d, no need to convert mode",
                containerViewWith, totalTabViewNeedWidth
            ));
        }
    }

    /**
     * 通过调整 View 两侧的外边距，设置每一项之间的间隔
     *  1. 在 {@link TabLayout#MODE_FIXED} 模式下，每个Tab平均分配可用的空间，调整Tab两侧外边距会减少Tab的宽度；
     *  2. 在 {@link TabLayout#MODE_SCROLLABLE} 模式下，Tab大小为内容大小，调整Tab两侧外边距会增加滚动内容宽度；
     * 不支持的模式：
     *  1. MODE_FIXED 下包裹内容下划线，这种情况由 {@link #adjustDividerWithForFixedWrap()} 方法处理
     * 测试方法：
     *  1. 分别在 MODE_FIXED、MODE_SCROLLABLE、MODE_AUTO 下启动，快速切换 Tab 产生渐变色，观察边距情况
     */
    private void adjustDividerWidthForNormal() {
        if (tabLayout.getTabMode() == TabLayout.MODE_FIXED && lineWidth == LINE_WIDTH_WRAP_CONTENT) {
            return;
        }
        if (dividerWidth <= 0) {
            return;
        }
        LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            View tabView = mTabStrip.getChildAt(i);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tabView.getLayoutParams();
            layoutParams.leftMargin = i == 0 ? 0 : dividerWidth / 2;
            layoutParams.rightMargin = i == mTabStrip.getChildCount() - 1 ? 0 : dividerWidth / 2;
        }
        mTabStrip.requestLayout();
        Log.d(TAG, "set Tab divider with: " + dividerWidth);
    }

    /**
     * 在 MODE_FIXED 模式下处理下划线为包裹内容，方案为在每个 TAB 前后添加外边距
     */
    private void adjustDividerWithForFixedWrap() {
        // 滚动模式本身就是WRAP_CONTENT，所以不需要处理
        // 非WRAP_CONTENT设置也就不需要处理了
        if (tabLayout.getTabMode() != TabLayout.MODE_FIXED || lineWidth != LINE_WIDTH_WRAP_CONTENT) {
            return;
        }
        // 计算容器大小与Tab需要的大小，产生一个剩余空白空间大小
        // 将这份空白空间均匀分配到Tab左右两次，包括起始、结束位置
        int emptySpaceWidth = calculateTabLayoutWidth() - calculateTabsWidth();
        int dividerSpaceWidth = emptySpaceWidth / (tabLayout.getTabCount() + 1);
        LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mTabStrip.getChildAt(i).getLayoutParams();
            layoutParams.weight = 0;
            layoutParams.width = -2;    // 必须设置包裹内容，否则各个Tab等宽，较长内容的Tab会被折行
            layoutParams.leftMargin = i == 0 ? dividerSpaceWidth : dividerSpaceWidth / 2;
            layoutParams.rightMargin = i == mTabStrip.getChildCount() - 1 ? dividerSpaceWidth : dividerSpaceWidth / 2;
        }
        mTabStrip.requestLayout();
        Log.d(TAG, "set Tab divider with wrap: " + dividerSpaceWidth / 2 + " " + dividerSpaceWidth);
    }

    /**
     * 对数值型(指定具体长度)的指示器长度做出调整，仅支持API23(安卓6.0)之上的版本，否则将不生效
     */
    private void adjustLineWidthForValue() {
        if (lineWidth == LINE_WIDTH_MATCH_PARENT || lineWidth == LINE_WIDTH_WRAP_CONTENT) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        tabLayout.setSelectedTabIndicator(R.drawable.drawable_tablayout_indicator);
        LayerDrawable tabSelectedIndicator = (LayerDrawable) tabLayout.getTabSelectedIndicator();
        Objects.requireNonNull(tabSelectedIndicator).setLayerWidth(0, lineWidth);
        Log.d(TAG, "set indicator line width: " + lineWidth);
    }


    /**
     * 查找 FragmentManager，对于嵌套 Fragment 会自动寻找对应的 Manager
     */
    private FragmentManager findFragmentManager() {
        FragmentManager fragmentManager;
        if (parentFragment == null) {
            FragmentActivity fragmentActivity = (FragmentActivity) UiUtils
                    .findActivityFromContext(tabLayout.getContext());
            fragmentManager = fragmentActivity.getSupportFragmentManager();
        } else {
            fragmentManager = parentFragment.getChildFragmentManager();
        }
        return fragmentManager;
    }

    /**
     * 借鉴 FragmentPagerAdapter 的处理，防止在 Activity 不可见时 ViewPager 被保存导致界面处理失败
     */
    private String makeFragmentName(long id) {
        return "android:switcher:" + viewGroupContainerId + ":" + id;
    }

    /**
     * 计算容器的宽度
     */
    private int calculateTabLayoutWidth() {
        return tabLayout.getChildAt(0).getMeasuredWidth();
    }

    /**
     * 计算内部Tab需要的宽度，不包括边距等额外空间，纯粹的Tab占用空间
     */
    private int calculateTabsWidth() {
        int totalWidth = 0;
        LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < mTabStrip.getChildCount(); i++) {
            View tabView = getTabViewFromTabViewContainer(mTabStrip.getChildAt(i));
            tabView.measure(0, 0);
            totalWidth += tabView.getMeasuredWidth();
        }
        return totalWidth;
    }

    /**
     * 获取TabView容器中的View
     */
    private View getTabViewFromTabViewContainer(View tabViewContainer) {
        try {
            // 查找 customView
            Field mCustomViewField = tabViewContainer.getClass().getDeclaredField("customView");
            mCustomViewField.setAccessible(true);
            View mCustomView = (View) mCustomViewField.get(tabViewContainer);
            if (mCustomView != null) {
                return mCustomView;
            }
            // 查找 textView
            Field mTextViewField = tabViewContainer.getClass().getDeclaredField("textView");
            mTextViewField.setAccessible(true);
            View mTextView = (View) mTextViewField.get(tabViewContainer);
            if (mTextView != null) {
                return mTextView;
            }
            // 查找不到抛出异常
            throw new IllegalStateException("cant find view from container");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

}
