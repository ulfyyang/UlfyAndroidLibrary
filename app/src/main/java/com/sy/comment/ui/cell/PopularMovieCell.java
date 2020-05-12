package com.sy.comment.ui.cell;

import android.content.Context;
import android.util.AttributeSet;

import com.sy.comment.application.cm.PopularMovieCM;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.sy.comment.R;
import com.sy.comment.ui.base.BaseCell;

@Layout(id = R.layout.cell_popular_movie)
public class PopularMovieCell extends BaseCell {
    
    private PopularMovieCM cm;

    public PopularMovieCell(Context context) {
        super(context);
        init(context, null);
    }

    public PopularMovieCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        cm = (PopularMovieCM) model;

    }
}