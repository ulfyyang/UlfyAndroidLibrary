package com.sy.comment.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import com.stx.xhb.xbanner.XBanner;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.vm.MovieBannerVM;
import com.sy.comment.ui.base.BaseView;

@Layout(id = R.layout.cell_movie)
public class MovieBannerView extends BaseView {
    @ViewById(id = R.id.movieBanner) private XBanner movieBanner;
    private MovieBannerVM vm;

    public MovieBannerView(Context context) {
        super(context);
        init(context, null);
    }

    public MovieBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        vm = (MovieBannerVM) model;
        movieBanner.setAutoPlayAble(vm.movieBannerList.size() > 1);
        movieBanner.setBannerData(vm.movieBannerList);
    }
}