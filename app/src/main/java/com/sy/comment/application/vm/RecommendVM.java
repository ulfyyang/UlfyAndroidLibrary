package com.sy.comment.application.vm;

import com.sy.comment.application.base.BaseVM;
import com.sy.comment.application.cm.FollowCM;
import com.sy.comment.ui.view.RecommendView;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.task.LoadListPageUiTask;

import java.util.ArrayList;
import java.util.List;

public class RecommendVM extends BaseVM {
    public List<FollowCM> recommendCMList = new ArrayList<>();
    public LoadListPageUiTask.LoadListPageUiTaskInfo<FollowCM> recommendTaskInfo = new LoadListPageUiTask.LoadListPageUiTaskInfo<>(recommendCMList);

    public LoadListPageUiTask.OnLoadListPage loadContentDataPerPageOnExe() {
        return new LoadListPageUiTask.OnLoadSimpleListPage() {
            @Override protected void loadSimplePage(LoadListPageUiTask task, List<Object> modelList, List<Object> tempList, int page, int pageSize) throws Exception {
                Thread.sleep(1000);
                for (int i = 0; i < 30; i++) {
                    tempList.add(new FollowCM());
                }
            }
        };
    }

    @Override public Class<? extends IView> getViewClass() {
        return RecommendView.class;
    }
}