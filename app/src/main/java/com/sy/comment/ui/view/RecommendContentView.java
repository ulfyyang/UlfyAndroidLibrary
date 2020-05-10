package com.sy.comment.ui.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import com.sy.comment.application.vm.RecommendVM;
import com.sy.comment.ui.base.ContentView;
import com.ulfy.android.task.TaskUtils;
import com.ulfy.android.task_transponder.ContentDataLoader;
import com.ulfy.android.task_transponder.OnReloadListener;

public class RecommendContentView extends ContentView {
    private RecommendVM vm;
    private RecommendView view;

    public RecommendContentView(Context context) {
        super(context);
        initModel(null);
        initContent(null);
    }

    /**
     * 初始化模型和界面
     */
    private void initModel(Bundle savedInstanceState) {
        vm = new RecommendVM();
    }

    /**
     * 初始化界面的数据
     */
    private void initContent(final Bundle savedInstanceState) {
        TaskUtils.loadData(getContext(), vm.recommendTaskInfo, vm.loadContentDataPerPageOnExe(), new ContentDataLoader(contentFL, vm, false) {
                    @Override protected void onCreatView(ContentDataLoader loader, View createdView) {
                        view = (RecommendView) createdView;
                    }
                }.setOnReloadListener(new OnReloadListener() {
                    @Override public void onReload() {
                        initContent(savedInstanceState);
                    }
                })
        );
    }
}