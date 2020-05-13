package com.sy.comment.ui.view;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.sy.comment.R;
import com.sy.comment.application.vm.HomeVM;
import com.sy.comment.ui.base.BaseView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.ulfy.android.ui_linkage.TabPagerLinkage;

@Layout(id = R.layout.view_home)
public class HomeView extends BaseView {
    @ViewById(id = R.id.tabsTL) private TabLayout tabsTL;
    @ViewById(id = R.id.containerVP) private ViewPager containerVP;
    @ViewById(id = R.id.containerFL) private FrameLayout containerFL;
    private HomeVM vm;
    private TabPagerLinkage linkage = new TabPagerLinkage();

    public HomeView(Context context) {
        super(context);
        init(context, null);
    }

    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        linkage.setTabLayout(tabsTL).setContainer(containerVP)
                .setLineWidth(TabPagerLinkage.LINE_WIDTH_WRAP_CONTENT)
                .setAutoScrollMode(true)
                .setUseWrapperOnScrollMode(true);
    }

    @Override public void bind(IViewModel model) {
        vm = (HomeVM) model;
        linkage.initStringTabs("关注", "推荐", "奖池")
                .initViewPages(new FollowContentView(getContext()), new RecommendContentView(getContext()), new PrizeContentView(getContext()))
                .build().select(0);
    }
}