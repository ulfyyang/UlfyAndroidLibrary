package com.ulfy.android.ui_linkage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.ulfy.android.ui_linkage.indicator.DrawablePagerIndicator;

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
 * MagicIndicator联动，只能和ViewPager合作，暂不支持Fragment
 */
public class MagicTabPagerLinkage {
    // 基础属性
    private Fragment parentFragment;            // 如果实在Fragment里边嵌套Fragment，则该参数为父Fragment
    private MagicIndicator magicIndicator;
    private ViewPager viewPagerContainer;
    private List<String> tabStringList;
    private List<View> viewPageList;
    private List<Fragment> fragmentPageList;
    // 定制属性(文字部分)
    private boolean average;            // 指示器是否平均分配显示（平均以后不滚动）
    private int titleSize = 18;         // 指示器文字大小
    private int titleNormalColor = Color.parseColor("#616161");     // 指示器文字默认颜色
    private int titleSelectedColor = Color.parseColor("#f57c00");   // 指示器文字选中的颜色
    private boolean titleScale;         // 指示器文字是否采用缩放效果
    private boolean titleBold;          // 指示器文字是否采用加粗效果
    // 定制属性(指示器部分-线条)
    private boolean indicatorBounce;    // 设置指示线是否具有跳动效果
    private int indicatorMode = LinePagerIndicator.MODE_WRAP_CONTENT;   // 设置指示线的模式：包裹内容、填充父控件、设置具体值
    private int indicatorWidth = UiUtils.dp2px(20);                     // 设置横线的宽度：当indicatorMode设置为具体值时生效
    private int indicatorYOffset = 0;   // 设置指示线距离底部的距离，竖直越大则横线越靠上
    private int indicatorHeight = UiUtils.dp2px(2);     // 设置指示线的高度
    private int indicatorColor = Color.parseColor("#f57c00");   // 设置指示线的颜色
    // 定制属性(指示器部分-Drawable) 变量中的默认值表示未设置 线条和Drawable相互冲突，同时只能使用一种
    private int indicatorResource; // 指示器资源    --- 指示器Resource、Drawable设置了线条部分无效，两者设置会相互覆盖
    private Drawable indicatorDrawable; // 指示器图像
    private int indicatorPaddingLeft, indicatorPaddingTop, indicatorPaddingRight, indicatorPaddingBotton;
    // 辅助属性
    private List<OnTabSelectedListener> onTabSelectedListenerList = new ArrayList<>();
    private boolean init;       // 页面初始化且ViewPager在0位置不会触发OnTabSelectedListener，该变量用于记录页面初始化的情况

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

    public MagicTabPagerLinkage setIndicatorResource(int indicatorResource) {
        this.indicatorResource = indicatorResource;
        this.indicatorDrawable = null;
        return this;
    }
    public MagicTabPagerLinkage setIndicatorDrawable(Drawable indicatorDrawable) {
        this.indicatorDrawable = indicatorDrawable;
        this.indicatorResource = 0;
        return this;
    }
    public MagicTabPagerLinkage setIndicatorPaddingDp(int indicatorPadding) {
        return setIndicatorPadding(UiUtils.dp2px(indicatorPadding));
    }
    public MagicTabPagerLinkage setIndicatorPadding(int indicatorPadding) {
        this.indicatorPaddingLeft = this.indicatorPaddingTop
                = this.indicatorPaddingRight = this.indicatorPaddingBotton
                = indicatorPadding;
        return this;
    }

    public MagicTabPagerLinkage setIndicatorPaddingLeftDp(int indicatorPaddingLeft) {
        return setIndicatorPaddingLeft(UiUtils.dp2px(indicatorPaddingLeft));
    }
    public MagicTabPagerLinkage setIndicatorPaddingLeft(int indicatorPaddingLeft) {
        this.indicatorPaddingLeft = indicatorPaddingLeft;
        return this;
    }
    public MagicTabPagerLinkage setIndicatorPaddingTopDp(int indicatorPaddingTop) {
        return setIndicatorPaddingTop(UiUtils.dp2px(indicatorPaddingTop));
    }
    public MagicTabPagerLinkage setIndicatorPaddingTop(int indicatorPaddingTop) {
        this.indicatorPaddingTop = indicatorPaddingTop;
        return this;
    }
    public MagicTabPagerLinkage setIndicatorPaddingRightDp(int indicatorPaddingRight) {
        return setIndicatorPaddingRight(UiUtils.dp2px(indicatorPaddingRight));
    }
    public MagicTabPagerLinkage setIndicatorPaddingRight(int indicatorPaddingRight) {
        this.indicatorPaddingRight = indicatorPaddingRight;
        return this;
    }
    public MagicTabPagerLinkage setIndicatorPaddingBottonDp(int indicatorPaddingBotton) {
        return setIndicatorPaddingBotton(UiUtils.dp2px(indicatorPaddingBotton));
    }
    public MagicTabPagerLinkage setIndicatorPaddingBotton(int indicatorPaddingBotton) {
        this.indicatorPaddingBotton = indicatorPaddingBotton;
        return this;
    }

    /*
    关键实现
     */

    public MagicTabPagerLinkage build() {
        // 设置指示器
        CommonNavigator commonNavigator = new CommonNavigator(magicIndicator.getContext());
        commonNavigator.setAdjustMode(average);
        commonNavigator.setScrollPivotX(0.8f);
        commonNavigator.setAdapter(buildNavigatorAdapter());
        magicIndicator.setNavigator(commonNavigator);
        ViewPagerHelper.bind(magicIndicator, viewPagerContainer);
        // 设置具体页-ViewPager
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
        // 设置ViewPager切换页面回调
        this.viewPagerContainer.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override public void onPageSelected(int position) {
                callOnTabSelected(position);
            }
        });
        return this;
    }

    public MagicTabPagerLinkage select(int position) {
        onPageShow(position);
        // 初次加载(页面初始化)且处在第一个页时，切换回调不会被触发，需要手动触发
        if (!init) {
            init = true;
            if (viewPagerContainer.getCurrentItem() == 0) {
                callOnTabSelected(position);
            }
        }
        return this;
    }

    private void onPageShow(int index) {
        viewPagerContainer.setCurrentItem(index);
    }

    private void callOnTabSelected(int position) {
        for (OnTabSelectedListener onTabSelectedListener : onTabSelectedListenerList) {
            onTabSelectedListener.onTabSelected(position);
        }
    }

    /**
     * 构造一个指示器适配器
     */
    private CommonNavigatorAdapter buildNavigatorAdapter() {
        return new CommonNavigatorAdapter() {
            @Override public IPagerIndicator getIndicator(Context context) {
                return buildIndicator(context);
            }
            @Override public IPagerTitleView getTitleView(Context context, final int index) {
                return buildTitleView(context, index);
            }
            @Override public int getCount() {
                return tabStringList == null ? 0 : tabStringList.size();
            }
        };
    }

    /**
     * 构造一个指示器线条
     */
    private IPagerIndicator buildIndicator(Context context) {
        if (indicatorResource > 0 || indicatorDrawable != null) {   // 设置了Drawable指示器
            DrawablePagerIndicator indicator = new DrawablePagerIndicator(context);
            if (indicatorResource > 0) {
                indicator.setIndicatorResource(indicatorResource);
            }
            if (indicatorDrawable != null) {
                indicator.setIndicatorDrawable(indicatorDrawable);
            }
            indicator.setPaddingLeft(indicatorPaddingLeft);
            indicator.setPaddingTop(indicatorPaddingTop);
            indicator.setPaddingRight(indicatorPaddingRight);
            indicator.setPaddingBottom(indicatorPaddingBotton);
            indicator.invalidate();
            return indicator;
        } else {
            LinePagerIndicator indicator = new LinePagerIndicator(context);
            if (indicatorBounce) {      // 设置移动过程中横线弹性伸缩
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(1.6f));
            }
            indicator.setMode(indicatorMode);           // 设置横线包裹长度模式：包裹内容、填充父控件、设置具体值
            indicator.setLineWidth(indicatorWidth);     // 设置横线的宽度：当indicatorMode设置为具体值时生效
            indicator.setYOffset(indicatorYOffset);     // 设置横线距离底部的距离，数值越大则横线越靠上
            indicator.setLineHeight(indicatorHeight);   // 设置横线的高度
            indicator.setColors(indicatorColor);        // 设置横线的颜色
            return indicator;
        }
    }

    /**
     * 构造一个指示器文字
     * @param index 点击指示器文字时回调的位置
     */
    private IPagerTitleView buildTitleView(Context context, final int index) {
        ScaleTransitionPagerTitleView scaleTransitionPagerTitleView = new ScaleTransitionPagerTitleView(context);
        scaleTransitionPagerTitleView.setText(tabStringList.get(index));    // 设置文字
        scaleTransitionPagerTitleView.setTextSize(titleSize);               // 设置字体大小
        scaleTransitionPagerTitleView.setNormalColor(titleNormalColor);     // 设置选中的文字颜色
        scaleTransitionPagerTitleView.setSelectedColor(titleSelectedColor); // 设置未选中的文字颜色
        scaleTransitionPagerTitleView.setScale(titleScale);                 // 设置是否缩放
        scaleTransitionPagerTitleView.setBold(titleBold);                   // 设置是否加粗
        scaleTransitionPagerTitleView.setOnClickListener(v -> onPageShow(index));   // 点击回调
        return scaleTransitionPagerTitleView;
    }

    /**
     * 指示器文字定制
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
