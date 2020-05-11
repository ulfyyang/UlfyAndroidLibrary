package com.sy.comment.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.vm.MovieVM;
import com.sy.comment.ui.base.BaseView;

@Layout(id = R.layout.view_movie)
public class MovieView extends BaseView {
    private MovieVM vm;

    public MovieView(Context context) {
        super(context);
        init(context, null);
    }

    public MovieView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        vm = (MovieVM) model;
    }
}