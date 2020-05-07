package com.sy.comment.ui.activity;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import com.sy.comment.R;
import com.sy.comment.ui.base.BaseActivity;
import com.sy.comment.ui.fragment.AppreciateFragment;
import com.sy.comment.ui.fragment.HomeFragment;
import com.sy.comment.ui.fragment.MineFragment;
import com.sy.comment.ui.fragment.NoticeFragment;
import com.ulfy.android.system.AppUtils;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.ulfy.android.ui_linkage.TabPagerLinkage;


@Layout(id = R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @ViewById(id = R.id.containerVP) private ViewPager containerVP;
    @ViewById(id = R.id.tabTL) private TabLayout tabTL;
    @ViewById(id = R.id.homeIV) private ImageView homeIV;
    @ViewById(id = R.id.appreciateIV) private ImageView appreciateIV;
    @ViewById(id = R.id.noticeIV) private ImageView noticeIV;
    @ViewById(id = R.id.mineIV) private ImageView mineIV;

    private TabPagerLinkage linkage = new TabPagerLinkage();

    private HomeFragment homeFragment = new HomeFragment();
    private AppreciateFragment appreciateFragment = new AppreciateFragment();
    private NoticeFragment noticeFragment = new NoticeFragment();
    private MineFragment mineFragment = new MineFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        linkage.setTabLayout(tabTL)
                .setContainer(containerVP)
                .initViewTabs(homeIV, appreciateIV, noticeIV, mineIV)
                .initFragmentPages(homeFragment, appreciateFragment, noticeFragment, mineFragment)
                .build().select(0);
    }

    @Override public void onBackPressed() {
        AppUtils.exitTwice("再按一次退出 " + AppUtils.getAppName());
    }

}
