package com.ulfy.android.task_transponder_smart;

import android.widget.AbsListView;
import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.ulfy.android.task.ITaskExecutor;
import com.ulfy.android.task.LoadDataUiTask;
import com.ulfy.android.task.LoadListPageUiTask;
import com.ulfy.android.task.NetUiTask;
import com.ulfy.android.task.TaskExecutor;
import com.ulfy.android.task.Transponder;

public class SmartRefresher extends Transponder {
    private SmartRefreshLayout smartRefreshLayout;
    private NetUiTask netUiTask;
    private LoadDataUiTask loadDataUiTask;
    private LoadListPageUiTask loadListPageUiTask;
    private ITaskExecutor taskExecutor;
    private OnRefreshSuccessListener onRefreshSuccessListener;

    public SmartRefresher(SmartRefreshLayout smartRefreshLayout, OnRefreshSuccessListener onRefreshSuccessListener) {
        this.smartRefreshLayout = smartRefreshLayout;
        this.onRefreshSuccessListener = onRefreshSuccessListener;
        this.initSetting();
    }

    private void initSetting() {
        RefreshHeader refreshHeader = SmartConfig.smartRefreshConfig.getRefreshHeaderView(smartRefreshLayout.getContext());
        smartRefreshLayout.setRefreshHeader(refreshHeader);
        smartRefreshLayout.setEnableLoadMore(false);
        smartRefreshLayout.setEnableOverScrollBounce(false);
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh(RefreshLayout refreshLayout) {
                if (loadListPageUiTask != null) {
                    loadListPageUiTask.getTaskInfo().loadStartPage();
                }
                if (taskExecutor == null) {
                    TaskExecutor.defaultConcurrentTaskExecutor().post(netUiTask);
                } else {
                    taskExecutor.post(netUiTask);
                }
            }
        });
    }

    public SmartRefresher updateExecuteBody(LoadDataUiTask.OnExecute executeBody) {
        loadDataUiTask = new LoadDataUiTask(smartRefreshLayout.getContext(), this);
        netUiTask = new NetUiTask(smartRefreshLayout.getContext(), loadDataUiTask, this);
        loadDataUiTask.setExecuteBody(executeBody);
        return this;
    }

    public SmartRefresher updateExecuteBody(LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody) {
        loadListPageUiTask = new LoadListPageUiTask(smartRefreshLayout.getContext(), this);
        netUiTask = new NetUiTask(smartRefreshLayout.getContext(), loadListPageUiTask, this);
        loadListPageUiTask.setTaskInfo(taskInfo);
        loadListPageUiTask.setLoadListPageBody(executeBody);
        return this;
    }

    public void autoRefresh() {
        smartRefreshLayout.autoRefresh(0, 400, 0.5f, false);
    }

    public SmartRefresher setTaskExecutor(ITaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        return this;
    }

    @Override public final void onNoNetConnection(Object data) {
        super.onNoNetConnection(data);
        smartRefreshLayout.finishRefresh();
    }

    @Override public final void onNetError(Object data) {
        smartRefreshLayout.finishRefresh();
        Toast.makeText(smartRefreshLayout.getContext(), data.toString(), Toast.LENGTH_LONG).show();
    }

    @Override protected final void onSuccess(Object tipData) {
        // 下拉刷新后会重置数据，需要矫正AbsListView的内部数据
        if (smartRefreshLayout.getChildCount() > 0 && smartRefreshLayout.getChildAt(0) instanceof AbsListView) {
            smartRefreshLayout.getChildAt(0).requestLayout();
        }
        if (onRefreshSuccessListener != null) {
            onRefreshSuccessListener.onRefreshSuccess(this);
        }
    }

    @Override protected void onFinish(Object tipData) {
        smartRefreshLayout.finishRefresh();
    }

    public interface OnRefreshSuccessListener {
        void onRefreshSuccess(SmartRefresher smartRefresher);
    }
}
