package com.sy.comment.ui.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    @ViewById(id = R.id.containerFL) private FrameLayout containerFL;
    @ViewById(id = R.id.tabTL) private TabLayout tabTL;
    @ViewById(id = R.id.homeLL) private LinearLayout homeLL;
    @ViewById(id = R.id.homeIV) private ImageView homeIV;
    @ViewById(id = R.id.homeTV) private TextView homeTV;
    @ViewById(id = R.id.appreciateLL) private LinearLayout appreciateLL;
    @ViewById(id = R.id.appreciateIV) private ImageView appreciateIV;
    @ViewById(id = R.id.appreciateTV) private TextView appreciateTV;
    @ViewById(id = R.id.noticeLL) private LinearLayout noticeLL;
    @ViewById(id = R.id.noticeIV) private ImageView noticeIV;
    @ViewById(id = R.id.noticeTV) private TextView noticeTV;
    @ViewById(id = R.id.mineLL) private LinearLayout mineLL;
    @ViewById(id = R.id.mineIV) private ImageView mineIV;
    @ViewById(id = R.id.mineTV) private TextView mineTV;
    private TabPagerLinkage linkage = new TabPagerLinkage();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linkage.setTabLayout(tabTL).setContainer(containerVP)
                .initViewTabs(homeLL, appreciateLL, noticeLL, mineLL)
                .initFragmentPages(new HomeFragment(), new AppreciateFragment(), new NoticeFragment(), new MineFragment())
                .build().select(0);
    }

    @Override public void onBackPressed() {
        AppUtils.exitTwice("再按一次退出 " + AppUtils.getAppName());
    }
}
