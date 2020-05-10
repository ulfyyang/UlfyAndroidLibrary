package com.sy.comment.application.vm;

import com.sy.comment.application.cm.FollowCM;
import com.sy.comment.application.cm.RecommendCM;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.task.LoadDataUiTask;
import com.ulfy.android.task.LoadListPageUiTask;
import com.ulfy.android.utils.LogUtils;
import com.sy.comment.application.base.BaseVM;
import com.sy.comment.ui.view.RecommendView;

import java.util.ArrayList;
import java.util.List;

public class RecommendVM extends BaseVM {

    public List<RecommendCM> recommendCMList = new ArrayList<>();
    public LoadListPageUiTask.LoadListPageUiTaskInfo<RecommendCM> recommendTaskInfo = new LoadListPageUiTask.LoadListPageUiTaskInfo<>(recommendCMList);

    public LoadListPageUiTask.OnLoadListPage loadContentDataPerPageOnExe() {
        return new LoadListPageUiTask.OnLoadSimpleListPage() {
            @Override protected void loadSimplePage(LoadListPageUiTask task, List<Object> modelList, List<Object> tempList, int page, int pageSize) throws Exception {
                Thread.sleep(1000);
                for (int i = 0; i < 30; i++) {
                    tempList.add(new FollowCM((page - 1) * 30 + i));
                }
            }
        };
    }

    @Override public Class<? extends IView> getViewClass() {
        return RecommendView.class;
    }
}