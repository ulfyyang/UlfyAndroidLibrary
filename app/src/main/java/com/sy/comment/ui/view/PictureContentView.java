package com.sy.comment.ui.view;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.sy.comment.application.vm.PictureVM;
import com.sy.comment.ui.base.ContentView;
import com.ulfy.android.task.TaskUtils;
import com.ulfy.android.task_transponder.ContentDataLoader;
import com.ulfy.android.task_transponder.OnReloadListener;

public class PictureContentView extends ContentView {
    private PictureVM vm;
    private PictureView view;

    public PictureContentView(Context context) {
        super(context);
        initModel(null);
        initContent(null);
    }

    /**
     * 初始化模型和界面
     */
    private void initModel(Bundle savedInstanceState) {
        vm = new PictureVM();
    }

    /**
     * 初始化界面的数据
     */
    private void initContent(final Bundle savedInstanceState) {
        TaskUtils.loadData(getContext(), vm.loadDataOnExe(), new ContentDataLoader(contentFL, vm, false) {
                    @Override protected void onCreatView(ContentDataLoader loader, View createdView) {
                        view = (PictureView) createdView;
                    }
                }.setOnReloadListener(new OnReloadListener() {
                    @Override public void onReload() {
                        initContent(savedInstanceState);
                    }
                })
        );
    }
}