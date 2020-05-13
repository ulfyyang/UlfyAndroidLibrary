package com.sy.comment.application.vm;

import com.sy.comment.application.cm.MovieCM;
import com.sy.comment.application.cm.PopularMovieCM;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.task.LoadDataUiTask;
import com.ulfy.android.utils.LogUtils;
import com.sy.comment.application.base.BaseVM;
import com.sy.comment.ui.view.MovieView;
import java.util.ArrayList;
import java.util.List;

public class MovieVM extends BaseVM {
    public List<MovieCM> movieCMList = new ArrayList<>();
    public List<PopularMovieCM> popularMovieCMList = new ArrayList<>();

    public LoadDataUiTask.OnExecute loadDataOnExe() {
        return new LoadDataUiTask.OnExecute() {
            @Override public void onExecute(LoadDataUiTask task) {
                try {
                    task.notifyStart("正在加载...");
                    popularMovieCMList.clear();
                    movieCMList.clear();
                    for (int i = 0; i < 6; i++) {
                        popularMovieCMList.add(new PopularMovieCM(i));
                        movieCMList.add(new MovieCM(i));
                    }
                    task.notifySuccess("加载完成");
                } catch (Exception e) {
                    LogUtils.log("加载失败", e);
                    task.notifyFail(e);
                }
            }
        };
    }

    @Override public Class<? extends IView> getViewClass() {
        return MovieView.class;
    }
}