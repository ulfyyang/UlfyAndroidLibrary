package com.ulfy.android.ui_linkage;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TableLayout联动
 * 可选择于ViewPager合作
 * 也可选择直接于View合作
 */
public class TabPagerLinkage {
    // 线条宽度：包裹内容
    public static final int LINE_WIDTH_MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int LINE_WIDTH_WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;
    // 基础属性
    private TabLayout tabLayout;
    private ViewPager viewPagerContainer;
    private ViewGroup viewGroupContainer;
    private int viewGroupContainerId;
    private List<String> tabStringList;
    private List<View> viewTabList;
    private List<View> viewPageList;
    private List<Fragment> fragmentPageList;
    private int lineWidth = LINE_WIDTH_MATCH_PARENT;      // 分割线宽度：包裹内容、填充父控件、指定宽度
    private boolean autoScrollMode;                         // 当内容显示不下时是否自动切换到滚动模式
    private boolean useWrapperOnScrollMode;                // 在滚动模式下使用包裹方案显示下划线
    private int dividerWidth = 0;                            // 间距
    // 辅助属性
    private List<OnTabSelectedListener> onTabSelectedListenerList = new ArrayList<>();
    // 用于记录容器的初始宽度
    private boolean init;
    private List<Integer> tabViewContainerInitWidthList = new ArrayList<>();
    // 用于记录真正使用的TabView
    private List<View> convertedTabViewList = new ArrayList<>();

    public TabPagerLinkage setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
        return this;
    }

    public TabPagerLinkage setContainer(ViewPager viewPager) {
        this.viewPagerContainer = viewPager;
        return this;
    }

    public TabPagerLinkage setContainer(ViewGroup viewGroupContainer) {
        this.viewGroupContainer = viewGroupContainer;
        return this;
    }

    public TabPagerLinkage setContainer(int viewGoroupId) {
        this.viewGroupContainerId = viewGoroupId;
        return this;
    }

    public TabPagerLinkage initStringTabs(String... tabs) {
        return initStringTabs(Arrays.asList(tabs));
    }

    public TabPagerLinkage initStringTabs(List<String> tabs) {
        this.tabStringList = tabs;
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

    public TabPagerLinkage setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        return this;
    }

    public TabPagerLinkage setAutoScrollMode(boolean autoScrollMode) {
        this.autoScrollMode = autoScrollMode;
        return this;
    }

    public TabPagerLinkage setUseWrapperOnScrollMode(boolean useWrapperOnScrollMode) {
        this.useWrapperOnScrollMode = useWrapperOnScrollMode;
        return this;
    }

    public TabPagerLinkage setDividerWidth(int dividerWidth) {
        this.dividerWidth = dividerWidth;
        return this;
    }

    public TabPagerLinkage addOnTabSelectedListener(final OnTabSelectedListener onTabSelectedListener) {
        this.onTabSelectedListenerList.add(onTabSelectedListener);
        return this;
    }

    public TabPagerLinkage build() {
        // 将客户端设置的标题参数转化为统一的View列表
        generateConvertedTabViewList();
        // 合并具体页到容器中
        if (viewPagerContainer != null) {
            combineWithViewPager();
        } else {
            combineWithoutViewPager();
        }
        // 绑定相关事件
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            public void onTabUnselected(TabLayout.Tab tab) { }
            public void onTabReselected(TabLayout.Tab tab) { }
            @Override public void onTabSelected(TabLayout.Tab tab) {
                for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
                    onTabSelectedListener.onTabSelected(tab.getPosition());
                }
            }
        });
        /*
        先进行一轮测量，当所需宽度不够时根据模式自动切换到滚动模式
         */
        changeScrollModeIfContainerNotBigEnough();
        /*
        在Fix模式下通过设置Item的左右边距设置间隔
         */
        setDividerWidthForFixed();
        /*
        如果线条为包裹内容
            则需要先记录各个Item容器的初始宽度
            然后通过设置左右边距的方式缩短线条宽度
         */
        recordTabViewContainerInitWidth();
        wrapContentIfNeed();
        /*
        如果线条为固定长度
            则通过一个Wrap布局替换原有的线
         */
        wrapTabViewForFixLineWidth();
        /*
        在Scroll模式下通过设置线的宽度达到间距的设置
         */
        setDividerWidthForScroll();
        /*
        根据模式设置原生下划线
         */
        setTabLayoutIndicatorHeight();
        return this;
    }

    public TabPagerLinkage select(int position) {
        // 内部有代码维护，点击同一个tab不会重复触发相应的回调
        tabLayout.getTabAt(position).select();
        // 第一次加载且处在第一个页时，切换回调不会被出发，因此需要手动触发
        if (!init && position == 0) {
            init = true;
            for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
                onTabSelectedListener.onTabSelected(position);
            }
        }
        return this;
    }



    // ========================================= 具体显示页相关设置 ====================================



    private TabPagerLinkage combineWithViewPager() {
        // 关联ViewPager
        if (viewPageList != null) {
            viewPagerContainer.setAdapter(new ViewPagerAdapter(viewPageList));
            viewPagerContainer.setOffscreenPageLimit(viewPageList.size() - 1);
        } else if (fragmentPageList != null) {
            viewPagerContainer.setAdapter(new FragmentPagerAdapter((FragmentActivity) viewPagerContainer.getContext(), fragmentPageList));
            viewPagerContainer.setOffscreenPageLimit(fragmentPageList.size() - 1);
        }
        tabLayout.setupWithViewPager(viewPagerContainer);
        for (int i = 0; i < convertedTabViewList.size(); i++) {
            tabLayout.getTabAt(i).setCustomView(convertedTabViewList.get(i));
        }
        return this;
    }

    private TabPagerLinkage combineWithoutViewPager() {
        // 必须先添加，因为后添加的话由于添加Tab的时候会触发点击回调导致在Fragment还没有恢复的时候进行则切换选择
        addViewsToContainerIfNeed();
        addFragmentsToContainerIfNeed();
        // 在添加Tab的时候会默认选中第一个，并触发一次回调
        tabLayout.addOnTabSelectedListener(new OnTabSelected());
        for (int i = 0; i < convertedTabViewList.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setCustomView(convertedTabViewList.get(i)));
        }
        return this;
    }

    /**
     * 生成转化后的TabView列表
     *      如果是字符串列表则转为TextView
     *      如果是View，则直接使用
     */
    private void generateConvertedTabViewList() {
        convertedTabViewList.clear();
        if (tabStringList != null) {
            for (int i = 0; i < tabStringList.size(); i++) {
                TextView textView = new TextView(UiUtils.findActivityFromContext(tabLayout.getContext()));
                textView.setText(tabStringList.get(i));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(tabLayout.getTabTextColors());
                convertedTabViewList.add(textView);
            }
        } else {
            convertedTabViewList.addAll(viewTabList);
        }
    }

    private class OnTabSelected implements TabLayout.OnTabSelectedListener {
        public void onTabUnselected(TabLayout.Tab tab) { }
        public void onTabReselected(TabLayout.Tab tab) { }
        public void onTabSelected(TabLayout.Tab tab) {
            onPageShow(tab.getPosition());
        }
    }



    // ========================================= TabLayout相关设置 ====================================



    /**
     * 当TabView需要的宽度超出了容器的宽度，切换为可滚动模式
     */
    private void changeScrollModeIfContainerNotBigEnough() {
        tabLayout.post(new Runnable() {
            @Override public void run() {
                if (autoScrollMode) {
                    int totleTabViewNeedWidth = 0;
                    // 计算每个TabView的宽度和
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabView = getTabViewFromTabViewContainer(mTabStrip.getChildAt(i));
                        tabView.measure(0, 0);
                        totleTabViewNeedWidth += tabView.getMeasuredWidth();
                    }
                    // 添加它们需要的间隔
                    totleTabViewNeedWidth += dividerWidth * (mTabStrip.getChildCount() - 1);
                    // 根据是否超出范围切换为滚动模式
                    if (totleTabViewNeedWidth > tabLayout.getWidth()) {
                        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                        if (lineWidth == LINE_WIDTH_MATCH_PARENT) {
                            lineWidth = LINE_WIDTH_WRAP_CONTENT;
                        }
                    }
                }
            }
        });
    }

    /**
     * 设置每一项之间的间隔
     */
    private void setDividerWidthForFixed() {
        tabLayout.post(new Runnable() {
            @Override public void run() {
                if (tabLayout.getTabMode() == TabLayout.MODE_FIXED) {
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabViewContainer = mTabStrip.getChildAt(i);
                        tabViewContainer.setPadding(0, 0, 0, 0);
                        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tabViewContainer.getLayoutParams();
                        layoutParams.leftMargin = i == 0 ? 0 : dividerWidth / 2;
                        layoutParams.rightMargin = i == mTabStrip.getChildCount() - 1 ? 0 : dividerWidth / 2;
                        tabViewContainer.invalidate();
                    }
                }
            }
        });
    }

    /**
     * 记录TabView容器的原始宽度
     */
    private void recordTabViewContainerInitWidth() {
        tabLayout.post(new Runnable() {
            @Override public void run() {
                if (lineWidth == LINE_WIDTH_WRAP_CONTENT) {
                    tabViewContainerInitWidthList.clear();
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        tabViewContainerInitWidthList.add(mTabStrip.getChildAt(i).getWidth());
                    }
                }
            }
        });
    }

    /**
     * 设置线条宽度为包裹内容长度
     *      MODE_FIXED:         不支持间隔设置
     *      MODE_SCROLLABLE:    以内边距方式实现间隔（线条变长）
     */
    public void wrapContentIfNeed() {
        tabLayout.post(new Runnable() {
            @Override public void run() {
                if (lineWidth == LINE_WIDTH_WRAP_CONTENT) {
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);

                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabViewContainer = mTabStrip.getChildAt(i);

                        View tabView = getTabViewFromTabViewContainer(tabViewContainer);
                        tabView.measure(0, 0);
                        int tabViewWidth = tabView.getMeasuredWidth();

                        LinearLayout.LayoutParams tabViewContainerLayoutParams = (LinearLayout.LayoutParams) tabViewContainer.getLayoutParams();

                        // 线条包裹内容下均分不支持间隔
                        if (tabLayout.getTabMode() == TabLayout.MODE_FIXED) {
                            tabViewContainer.setPadding(0, 0, 0, 0);
                            int harfMargin = (tabViewContainerInitWidthList.get(i) - tabViewWidth) / 2;
                            tabViewContainerLayoutParams.width = tabViewWidth;
                            tabViewContainerLayoutParams.leftMargin = harfMargin;
                            tabViewContainerLayoutParams.rightMargin = harfMargin;
                        }
                        // 线条包裹内容下滚动支持间隔，但是是通过padding的方式实现的，因此线会多出一点
                        else {
                            if (useWrapperOnScrollMode) {
                                wrapTabViewFromTabViewContainer(tabViewContainer, i, tabViewWidth);
                            } else {
                                tabViewContainer.setPadding(dividerWidth / 2, 0, dividerWidth / 2, 0);
                                tabViewContainerLayoutParams.width = tabViewWidth + dividerWidth;
                                tabViewContainerLayoutParams.leftMargin =  0;
                                tabViewContainerLayoutParams.rightMargin = 0;
                            }
                        }

                        tabViewContainer.setLayoutParams(tabViewContainerLayoutParams);
                        tabViewContainer.invalidate();
                    }
                }
            }
        });
    }

    /**
     * 针对固定宽度的线条执行TabView包裹操作
     */
    private void wrapTabViewForFixLineWidth() {
        tabLayout.post(new Runnable() {
            @Override public void run() {
                if (lineWidth != LINE_WIDTH_MATCH_PARENT && lineWidth != LINE_WIDTH_WRAP_CONTENT) {
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);

                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabViewContainer = mTabStrip.getChildAt(i);
                        wrapTabViewFromTabViewContainer(tabViewContainer, i, lineWidth);

                        View tabView = getTabViewFromTabViewContainer(tabViewContainer);
                        tabView.measure(0, 0);
                        int tabViewWidth = tabView.getMeasuredWidth();

                        LinearLayout.LayoutParams tabViewContainerLayoutParams = (LinearLayout.LayoutParams) tabViewContainer.getLayoutParams();

                        tabViewContainer.setPadding(dividerWidth / 2, 0, dividerWidth / 2, 0);
                        tabViewContainerLayoutParams.width = tabViewWidth + dividerWidth;
                        tabViewContainerLayoutParams.leftMargin =  0;
                        tabViewContainerLayoutParams.rightMargin = 0;
                    }
                }
            }
        });
    }

    /**
     * 获取TabView容器中的View
     */
    private View getTabViewFromTabViewContainer(View tabViewContainer) {
        try {
            Field mTextViewField = tabViewContainer.getClass().getDeclaredField("mTextView");
            mTextViewField.setAccessible(true);
            Field mCustomViewField = tabViewContainer.getClass().getDeclaredField("mCustomView");
            mCustomViewField.setAccessible(true);
            // 优先查找自定义的View
            View mTextView = (View) mTextViewField.get(tabViewContainer);
            View mCustomView = (View) mCustomViewField.get(tabViewContainer);
            return mCustomView != null ? mCustomView : mTextView;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    /**
     * 为TabView添加线条包裹
     */
    private void wrapTabViewFromTabViewContainer(View tabViewContainer, int index, int lineWidth) {
        try {
            Field mCustomViewField = tabViewContainer.getClass().getDeclaredField("mCustomView");
            mCustomViewField.setAccessible(true);
            // 查找View
            View mCustomView = (View) mCustomViewField.get(tabViewContainer);
            // 生成包裹
            TabWrapperCell tabWrapperCell = new TabWrapperCell(UiUtils.findActivityFromContext(tabLayout.getContext()));
            tabWrapperCell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tabWrapperCell.setContent(mCustomView, tabLayout, lineWidth);
            // 设置回去，覆盖原有的
            tabLayout.getTabAt(index).setCustomView(tabWrapperCell);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    /**
     * 设置滚动模式下的分割线宽度
     */
    private void setDividerWidthForScroll() {
        tabLayout.post(new Runnable() {
            @Override public void run() {
                if (tabLayout.getTabMode() == TabLayout.MODE_SCROLLABLE) {
                    LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);
                    for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                        View tabViewContainer = mTabStrip.getChildAt(i);

                        View tabView = getTabViewFromTabViewContainer(tabViewContainer);
                        tabView.measure(0, 0);
                        int tabViewWidth = tabView.getMeasuredWidth();

                        LinearLayout.LayoutParams tabViewContainerLayoutParams = (LinearLayout.LayoutParams) tabViewContainer.getLayoutParams();
                        tabViewContainer.setPadding(dividerWidth / 2, 0, dividerWidth / 2, 0);
                        tabViewContainerLayoutParams.width = tabViewWidth + dividerWidth;
                        tabViewContainerLayoutParams.leftMargin =  0;
                        tabViewContainerLayoutParams.rightMargin = 0;
                        tabViewContainer.invalidate();
                    }
                }
            }
        });
    }

    /**
     * 根据模式设置原生下划线
     */
    private void setTabLayoutIndicatorHeight() {
        tabLayout.post(new Runnable() {
            @Override public void run() {
                // 当设置了固定宽度后取消原生下划线显示
                if (lineWidth != LINE_WIDTH_MATCH_PARENT && lineWidth != LINE_WIDTH_WRAP_CONTENT) {
                    tabLayout.setSelectedTabIndicatorHeight(0);
                }
                // 当在滚动模式下且设置了自动使用包裹器则取消原生下划线显示
                else if (tabLayout.getTabMode() == TabLayout.MODE_SCROLLABLE && useWrapperOnScrollMode) {
                    tabLayout.setSelectedTabIndicatorHeight(0);
                }
            }
        });
    }

    private TabPagerLinkage onPageShow(int index) {
        // ViewPager与TabLayout内部有维护关联关系，因此这里不需要维护
        if (viewPageList != null) {
            showViewByIndex(index);
        } else if (fragmentPageList != null) {
            showFragmentByIndex(index);
        }
        return this;
    }

    private void addViewsToContainerIfNeed() {
        if (viewGroupContainer != null) {
            viewGroupContainer.removeAllViews();
        }
        if (viewPageList != null) {
            for (View viewPage : viewPageList) {
                UiUtils.clearParent(viewPage);
                viewGroupContainer.addView(viewPage, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
    }

    /**
     * 这里添加时异步的，因此重复的调用两次该方法并不能防止重复添加的情况，而这种情况下将会导致异常
     */
    private void addFragmentsToContainerIfNeed() {
        if (fragmentPageList != null) {
            FragmentManager fragmentManager = ((FragmentActivity) UiUtils.findActivityFromContext(tabLayout.getContext())).getSupportFragmentManager();
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
    }

    private void showViewByIndex(int index) {
        for (int i = 0; i < viewPageList.size(); i++) {
            viewPageList.get(i).setVisibility(i == index ? View.VISIBLE : View.GONE);
        }
    }

    private void showFragmentByIndex(int index) {
        FragmentManager fragmentManager = ((FragmentActivity) UiUtils.findActivityFromContext(tabLayout.getContext())).getSupportFragmentManager();
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

    /**
     * 借鉴FragmentPagerAdapter的处理，防止在Activity不可见时ViewPager被保存时界面处理失败
     */
    private String makeFragmentName(long id) {
        return "android:switcher:" + viewGroupContainerId + ":" + id;
    }

    private static class TabWrapperCell extends FrameLayout {
        private FrameLayout containerFL;
        private View lineV;

        public TabWrapperCell(@NonNull Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.ulfy_ui_linkage_cell_tab_page_linkage_wrapper, this);
            containerFL = findViewById(R.id.containerFL);
            lineV = findViewById(R.id.lineV);
        }

        public TabWrapperCell setContent(View view, TabLayout tabLayout, int lineWidth) {
            this.containerFL.removeAllViews();
            UiUtils.clearParent(view);
            this.containerFL.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

            try {
                LinearLayout mTabStrip = (LinearLayout) tabLayout.getChildAt(0);

                Field mSelectedIndicatorPaintField = mTabStrip.getClass().getDeclaredField("mSelectedIndicatorPaint");
                mSelectedIndicatorPaintField.setAccessible(true);
                Field mSelectedIndicatorHeightField = mTabStrip.getClass().getDeclaredField("mSelectedIndicatorHeight");
                mSelectedIndicatorHeightField.setAccessible(true);

                Paint mSelectedIndicatorPaint = (Paint) mSelectedIndicatorPaintField.get(mTabStrip);
                int mSelectedIndicatorHeight = (int) mSelectedIndicatorHeightField.get(mTabStrip);

                lineV.setBackgroundColor(mSelectedIndicatorPaint.getColor());
                lineV.getLayoutParams().width = lineWidth;
                lineV.getLayoutParams().height = mSelectedIndicatorHeight;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return this;
        }

        @Override public void setSelected(boolean selected) {
            super.setSelected(selected);
            lineV.setVisibility(selected ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
