package com.ulfy.android.ui_linkage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MagicIndicator联动
 *      只能和ViewPager合作，且不支持Fragment
 *      如果支持Fragment则Activity被半销毁后无法正确的恢复
 */
public class MagicTabPagerLinkage {
    // 基础属性
    private Fragment parentFragment;            // 如果实在Fragment里边嵌套Fragment，则该参数为父Fragment
    private MagicIndicator magicIndicator;
    private ViewPager viewPagerContainer;
    private List<String> tabStringList;
    private List<View> viewPageList;
    private List<Fragment> fragmentPageList;
    // 定制属性
    private boolean average;            // 指示器是否平均分配显示（平均以后不滚动）
    private int titleSize = 18;         // 指示器文字大小
    private int titleNormalColor = Color.parseColor("#616161");     // 指示器文字默认颜色
    private int titleSelectedColor = Color.parseColor("#f57c00");   // 指示器文字选中的颜色
    private boolean titleScale;         // 指示器文字是否采用缩放效果
    private boolean titleBold;          // 指示器文字是否采用加粗效果
    private boolean indicatorBounce;    // 设置指示线是否具有跳动效果
    private int indicatorMode = LinePagerIndicator.MODE_WRAP_CONTENT;   // 设置指示线的模式：包裹内容、填充父控件、设置具体值
    private int indicatorWidth = UiUtils.dp2px(20);                     // 设置横线的宽度：当indicatorMode设置为具体值时生效
    private int indicatorYOffset = 0;   // 设置指示线距离底部的距离，竖直越大则横线越考上
    private int indicatorHeight = UiUtils.dp2px(2);     // 设置指示线的高度
    private int indicatorColor = Color.parseColor("#f57c00");   // 设置指示线的颜色
    // 辅助属性
    private List<OnTabSelectedListener> onTabSelectedListenerList = new ArrayList<>();
    private boolean init;

    /*
    基础属性设置
     */

    public MagicTabPagerLinkage setParentFragment(Fragment parentFragment) {
        this.parentFragment = parentFragment;
        return this;
    }

    public MagicTabPagerLinkage setMagicIndicator(MagicIndicator magicIndicator) {
        this.magicIndicator = magicIndicator;
        return this;
    }

    public MagicTabPagerLinkage setContainer(ViewPager viewPager) {
        this.viewPagerContainer = viewPager;
        return this;
    }

    public MagicTabPagerLinkage initStringTabs(String... tabs) {
        return initStringTabs(Arrays.asList(tabs));
    }

    public MagicTabPagerLinkage initStringTabs(List<String> tabs) {
        this.tabStringList = tabs;
        return this;
    }

    public MagicTabPagerLinkage initViewPages(View... pages) {
        return initViewPages(Arrays.asList(pages));
    }

    public MagicTabPagerLinkage initViewPages(List<View> pages) {
        this.viewPageList = pages;
        if (pages != null) {
            for (View page : pages) {
                UiUtils.clearParent(page);
            }
        }
        return this;
    }

    public MagicTabPagerLinkage initFragmentPages(Fragment... fragments) {
        return this.initFragmentPages(Arrays.asList(fragments));
    }

    public MagicTabPagerLinkage initFragmentPages(List<Fragment> fragments) {
        this.fragmentPageList = fragments;
        return this;
    }

    public MagicTabPagerLinkage addOnTabSelectedListener(final OnTabSelectedListener onTabSelectedListener) {
        this.onTabSelectedListenerList.add(onTabSelectedListener);
        return this;
    }

    /*
    定制属性设置
     */

    public MagicTabPagerLinkage setAverage(boolean average) {
        this.average = average;
        return this;
    }

    public MagicTabPagerLinkage setTitleSize(int titleSize) {
        this.titleSize = titleSize;
        return this;
    }

    public MagicTabPagerLinkage setTitleNormalColor(int titleNormalColor) {
        this.titleNormalColor = titleNormalColor;
        return this;
    }

    public MagicTabPagerLinkage setTitleSelectedColor(int titleSelectedColor) {
        this.titleSelectedColor = titleSelectedColor;
        return this;
    }

    public MagicTabPagerLinkage setTitleScale(boolean titleScale) {
        this.titleScale = titleScale;
        return this;
    }

    public MagicTabPagerLinkage setTitleBold(boolean titleBold) {
        this.titleBold = titleBold;
        return this;
    }

    public MagicTabPagerLinkage setIndicatorBounce(boolean indicatorBounce) {
        this.indicatorBounce = indicatorBounce;
        return this;
    }

    public MagicTabPagerLinkage setIndicatorMode(int indicatorMode) {
        this.indicatorMode = indicatorMode;
        return this;
    }

    public MagicTabPagerLinkage setIndicatorWidthDP(int indicatorWidth) {
        this.indicatorWidth = UiUtils.dp2px(indicatorWidth);
        return this;
    }

    public MagicTabPagerLinkage setIndicatorWidth(int indicatorWidth) {
        this.indicatorWidth = indicatorWidth;
        return this;
    }

    public MagicTabPagerLinkage setIndicatorYOffsetDp(int indicatorYOffset) {
        this.indicatorYOffset = UiUtils.dp2px(indicatorYOffset);
        return this;
    }

    public MagicTabPagerLinkage setIndicatorYOffset(int indicatorYOffset) {
        this.indicatorYOffset = indicatorYOffset;
        return this;
    }

    public MagicTabPagerLinkage setIndicatorHeightDP(int indicatorHeight) {
        this.indicatorHeight = UiUtils.dp2px(indicatorHeight);
        return this;
    }

    public MagicTabPagerLinkage setIndicatorHeight(int indicatorHeight) {
        this.indicatorHeight = indicatorHeight;
        return this;
    }

    public MagicTabPagerLinkage setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        return this;
    }

    /*
    关键实现
     */

    public MagicTabPagerLinkage build() {
        // 基础设置
        CommonNavigator commonNavigator = new CommonNavigator(magicIndicator.getContext());
        commonNavigator.setAdjustMode(average);
        commonNavigator.setScrollPivotX(0.8f);
        commonNavigator.setAdapter(new CommonNavigatorAdapter() {
            @Override public int getCount() {
                return tabStringList == null ? 0 : tabStringList.size();
            }
            @Override public IPagerTitleView getTitleView(Context context, final int index) {
                ScaleTransitionPagerTitleView scaleTransitionPagerTitleView = new ScaleTransitionPagerTitleView(context);
                scaleTransitionPagerTitleView.setText(tabStringList.get(index));
                // 设置字体大小
                scaleTransitionPagerTitleView.setTextSize(titleSize);
                // 设置选中和为选中的文字颜色
                scaleTransitionPagerTitleView.setNormalColor(titleNormalColor);
                scaleTransitionPagerTitleView.setSelectedColor(titleSelectedColor);
                // 设置是否缩放
                scaleTransitionPagerTitleView.setScale(titleScale);
                // 设置是否加错
                scaleTransitionPagerTitleView.setBold(titleBold);
                // 与ViewPager联动
                scaleTransitionPagerTitleView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        onPageShow(index);
                    }
                });
                return scaleTransitionPagerTitleView;
            }
            @Override public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
                // 设置移动过程中横线弹性伸缩
                if (indicatorBounce) {
                    indicator.setStartInterpolator(new AccelerateInterpolator());
                    indicator.setEndInterpolator(new DecelerateInterpolator(1.6f));
                }
                // 设置横线包裹长度模式：包裹内容、填充父控件、设置具体值
                indicator.setMode(indicatorMode);
                // 设置横线的宽度：当indicatorMode设置为具体值时生效
                indicator.setLineWidth(indicatorWidth);
                // 设置横线距离底部的距离，数值越大则横线越靠上
                indicator.setYOffset(indicatorYOffset);
                // 设置横线的高度
                indicator.setLineHeight(indicatorHeight);
                // 设置横线的颜色
                indicator.setColors(indicatorColor);
                return indicator;
            }
        });
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, viewPagerContainer);
        if (viewPageList != null) {
            viewPagerContainer.setAdapter(new ViewPagerAdapter(viewPageList));
            viewPagerContainer.setOffscreenPageLimit(viewPageList.size() - 1);
        } else if (fragmentPageList != null) {
            if (parentFragment == null) {
                viewPagerContainer.setAdapter(new FragmentPagerAdapter(
                        ((FragmentActivity) viewPagerContainer.getContext()).getSupportFragmentManager(), fragmentPageList));
            } else {
                viewPagerContainer.setAdapter(new FragmentPagerAdapter(
                        parentFragment.getChildFragmentManager(), fragmentPageList));
            }
            viewPagerContainer.setOffscreenPageLimit(fragmentPageList.size() - 1);
        }
        this.viewPagerContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override public void onPageScrollStateChanged(int state) { }
            @Override public void onPageSelected(int position) {
                for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
                    onTabSelectedListener.onTabSelected(position);
                }
            }
        });
        return this;
    }

    public MagicTabPagerLinkage select(int position) {
        onPageShow(position);
        // 第一次加载且处在第一个页时，切换回调不会被出发，因此需要手动触发
        if (!init && viewPagerContainer.getCurrentItem() == 0) {
            init = true;
            for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
                onTabSelectedListener.onTabSelected(position);
            }
        }
        return this;
    }

    private MagicTabPagerLinkage onPageShow(int index) {
        viewPagerContainer.setCurrentItem(index);
        return this;
    }

    /**
     * 指示器文字
     */
    private static class ScaleTransitionPagerTitleView extends ColorTransitionPagerTitleView {
        private float mMinScale = 0.75f;
        private boolean scale;
        private boolean bold;

        public ScaleTransitionPagerTitleView(Context context) {
            super(context);
        }

        @Override public void onEnter(int index, int totalCount, float enterPercent, boolean leftToRight) {
            super.onEnter(index, totalCount, enterPercent, leftToRight);    // 实现颜色渐变
            if (scale) {
                setScaleX(mMinScale + (1.0f - mMinScale) * enterPercent);
                setScaleY(mMinScale + (1.0f - mMinScale) * enterPercent);
            }
            if (bold) {
                setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
        }

        @Override public void onLeave(int index, int totalCount, float leavePercent, boolean leftToRight) {
            super.onLeave(index, totalCount, leavePercent, leftToRight);    // 实现颜色渐变
            if (scale) {
                setScaleX(1.0f + (mMinScale - 1.0f) * leavePercent);
                setScaleY(1.0f + (mMinScale - 1.0f) * leavePercent);
            }
            if (bold) {
                setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }
        }

        public float getMinScale() {
            return mMinScale;
        }

        public void setMinScale(float minScale) {
            mMinScale = minScale;
        }

        public void setScale(boolean scale) {
            this.scale = scale;
        }

        public void setBold(boolean bold) {
            this.bold = bold;
        }
    }
}
