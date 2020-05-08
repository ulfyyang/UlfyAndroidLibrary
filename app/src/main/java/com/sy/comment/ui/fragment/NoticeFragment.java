package com.sy.comment.ui.fragment;

import android.os.Bundle;
import android.view.View;

import com.sy.comment.application.vm.NoticeVM;
import com.sy.comment.ui.base.ContentFragment;
import com.sy.comment.ui.view.NoticeView;
import com.ulfy.android.task.TaskUtils;
import com.ulfy.android.task_transponder.ContentDataLoader;
import com.ulfy.android.task_transponder.OnReloadListener;

public class NoticeFragment extends ContentFragment {
    private NoticeVM vm;
    private NoticeView view;

    /**
     * 初始化方法
     */
    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        // initModel(savedInstanceState);
        // initContent(savedInstanceState);
    }

    /**
     * 用户首次可见
     */
    @Override public void onVisibleFirstToUser() {
        initModel(null);
        initContent(null);
    }

    /**
     * 初始化模型和界面
     */
    private void initModel(Bundle savedInstanceState) {
        vm = new NoticeVM();
    }

    /**
     * 初始化界面的数据
     */
    private void initContent(final Bundle savedInstanceState) {
        TaskUtils.loadData(getContext(), vm.loadDataOnExe(), new ContentDataLoader(contentFL, vm, false) {
                    @Override protected void onCreatView(ContentDataLoader loader, View createdView) {
                        view = (NoticeView) createdView;
                    }
                }.setOnReloadListener(new OnReloadListener() {
                    @Override public void onReload() {
                        initContent(savedInstanceState);
                    }
                })
        );
    }
}