package com.sy.comment.ui.view;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.sy.comment.R;
import com.sy.comment.application.vm.AppreciateVM;
import com.sy.comment.ui.base.BaseView;
import com.ulfy.android.ui_injection.ViewById;
import com.ulfy.android.ui_linkage.TabPagerLinkage;

@Layout(id = R.layout.view_appreciate)
public class AppreciateView extends BaseView {
    @ViewById(id = R.id.tabsTL) private TabLayout tabsTL;
    @ViewById(id = R.id.movieFL) private FrameLayout movieFL;
    @ViewById(id = R.id.movieTV) private TextView movieTV;
    @ViewById(id = R.id.literatureFL) private FrameLayout literatureFL;
    @ViewById(id = R.id.literatureTV) private TextView literatureTV;
    @ViewById(id = R.id.pictureFL) private FrameLayout pictureFL;
    @ViewById(id = R.id.pictureTV) private TextView pictureTV;
    @ViewById(id = R.id.soundFL) private FrameLayout soundFL;
    @ViewById(id = R.id.soundTV) private TextView soundTV;
    @ViewById(id = R.id.containerVP) private ViewPager containerVP;
    @ViewById(id = R.id.containerFL) private FrameLayout containerFL;
    @ViewById(id = R.id.moviePageLL) private LinearLayout moviePageLL;
    @ViewById(id = R.id.literaturePageLL) private LinearLayout literaturePageLL;
    @ViewById(id = R.id.picturePageLL) private LinearLayout picturePageLL;
    @ViewById(id = R.id.soundPageLL) private LinearLayout soundPageLL;
    private AppreciateVM vm;
    private TabPagerLinkage linkage = new TabPagerLinkage();
    // 页面 - viewPager
    private MovieContentView movieContentView = new MovieContentView(getContext());
    private LiteratureContentView literatureContentView = new LiteratureContentView(getContext());
    private PictureContentView pictureContentView = new PictureContentView(getContext());
    private SoundContentView soundContentView = new SoundContentView(getContext());

    public AppreciateView(Context context) {
        super(context);
        init(context, null);
    }

    public AppreciateView(Context context, AttributeSet attrs) {
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
        vm = (AppreciateVM) model;
        linkage.initStringTabs("影视", "文学", "美图", "有声")
                .initViewPages(movieContentView, literatureContentView, pictureContentView, soundContentView)
                .build().select(0);
    }
}