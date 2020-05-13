package com.sy.comment.application.vm;

import com.sy.comment.application.cm.HotLiteratureCM;
import com.sy.comment.application.cm.LiteratureCM;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.task.LoadDataUiTask;
import com.ulfy.android.utils.LogUtils;
import com.sy.comment.application.base.BaseVM;
import com.sy.comment.ui.view.LiteratureView;
import java.util.ArrayList;
import java.util.List;

public class LiteratureVM extends BaseVM {
    public List<LiteratureCM> literatureCMList = new ArrayList<>();
    public List<HotLiteratureCM> hotLiteratureCMList = new ArrayList<>();

    public LoadDataUiTask.OnExecute loadDataOnExe() {
        return new LoadDataUiTask.OnExecute() {
            @Override public void onExecute(LoadDataUiTask task) {
                try {
                    task.notifyStart("正在加载...");
                    hotLiteratureCMList.clear();
                    literatureCMList.clear();
                    for (int i = 0; i < 6; i++) {
                        hotLiteratureCMList.add(new HotLiteratureCM(i));
                        literatureCMList.add(new LiteratureCM(i));
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
        return LiteratureView.class;
    }
}