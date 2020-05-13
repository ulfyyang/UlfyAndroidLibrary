package com.sy.comment.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.sy.comment.application.cm.MovieCM;
import com.sy.comment.application.cm.PopularMovieCM;
import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.task_transponder.RecyclerViewPageLoader;
import com.ulfy.android.task_transponder_smart.SmartRefresher;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.vm.MovieVM;
import com.sy.comment.ui.base.BaseView;
import com.ulfy.android.utils.RecyclerViewUtils;

@Layout(id = R.layout.view_movie)
public class MovieView extends BaseView {
    @ViewById(id = R.id.movieSRL) private SmartRefreshLayout movieSRL;
    @ViewById(id = R.id.popularRV) private RecyclerView popularRV;
    @ViewById(id = R.id.movieRV) private RecyclerView movieRV;
    private RecyclerAdapter<MovieCM> movieAdapter = new RecyclerAdapter<>();
    private RecyclerAdapter<PopularMovieCM> popularAdapter = new RecyclerAdapter<>();
    private SmartRefresher movieRefresher;
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
        RecyclerViewUtils.gridLayout(popularRV).vertical(3).dividerDp(Color.alpha(00), 10, 10, 0, 0);
        RecyclerViewUtils.linearLayout(movieRV).vertical().dividerDp(Color.alpha(00), 30, 0, 0);
        movieRV.setNestedScrollingEnabled(false);
        popularRV.setNestedScrollingEnabled(false);
        movieRV.setAdapter(movieAdapter);
        popularRV.setAdapter(popularAdapter);
        movieRefresher = new SmartRefresher(movieSRL, new SmartRefresher.OnRefreshSuccessListener() {
            @Override public void onRefreshSuccess(SmartRefresher smartRefresher) {
                bind(vm);
            }
        });
    }

    @Override public void bind(IViewModel model) {
        vm = (MovieVM) model;
        movieRefresher.updateExecuteBody(null);
        movieAdapter.setData(vm.movieCMList);
        popularAdapter.setData(vm.popularMovieCMList);
        movieAdapter.notifyDataSetChanged();
        popularAdapter.notifyDataSetChanged();
    }
}