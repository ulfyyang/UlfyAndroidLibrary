package com.sy.comment.application.vm;

import com.sy.comment.domain.entity.Banner;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.task.LoadDataUiTask;
import com.ulfy.android.utils.LogUtils;
import com.sy.comment.application.base.BaseVM;
import com.sy.comment.ui.view.MovieBannerView;
import java.util.ArrayList;
import java.util.List;

public class MovieBannerVM extends BaseVM {
    public List<Banner> movieBannerList = new ArrayList<>();

    public LoadDataUiTask.OnExecute loadDataOnExe() {
        return new LoadDataUiTask.OnExecute() {
            @Override public void onExecute(LoadDataUiTask task) {
                try {
                    task.notifyStart("正在加载...");
                    movieBannerList.add(new Banner("drawable-xhdpi/drawable_banner_default.png"));
                    movieBannerList.add(new Banner("drawable-xhdpi/drawable_banner_default.png"));
                    movieBannerList.add(new Banner("drawable-xhdpi/drawable_banner_default.png"));
                    movieBannerList.add(new Banner("drawable-xhdpi/drawable_banner_default.png"));
                    task.notifySuccess("加载完成");
                } catch (Exception e) {
                    LogUtils.log("加载失败", e);
                    task.notifyFail(e);
                }
            }
        };
    }

    @Override public Class<? extends IView> getViewClass() {
        return MovieBannerView.class;
    }
}