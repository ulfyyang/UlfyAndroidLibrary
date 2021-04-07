package com.ulfy.android.task_transponder_smart;

import android.widget.Toast;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.ulfy.android.task.ITaskExecutor;
import com.ulfy.android.task.LoadDataUiTask;
import com.ulfy.android.task.LoadListPageUiTask;
import com.ulfy.android.task.NetUiTask;
import com.ulfy.android.task.TaskExecutor;
import com.ulfy.android.task.Transponder;

public class SmartLoader extends Transponder {
    private SmartRefreshLayout smartRefreshLayout;
    private NetUiTask netUiTask;
    private LoadDataUiTask loadDataUiTask;
    private LoadListPageUiTask loadListPageUiTask;
    private ITaskExecutor taskExecutor;
    private OnLoadSuccessListener onLoadSuccessListener;

    public SmartLoader(SmartRefreshLayout smartRefreshLayout, OnLoadSuccessListener onLoadSuccessListener) {
        this.smartRefreshLayout = smartRefreshLayout;
        this.onLoadSuccessListener = onLoadSuccessListener;

        RefreshFooter refreshFooter = SmartConfig.smartRefreshConfig.getRefreshFooterView(smartRefreshLayout.getContext());
        smartRefreshLayout.setRefreshFooter(refreshFooter);
        smartRefreshLayout.setEnableLoadMore(true);
        smartRefreshLayout.setEnableOverScrollBounce(false);

        smartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override public void onLoadMore(RefreshLayout refreshLayout) {
                if (loadListPageUiTask != null) {
                    loadListPageUiTask.getTaskInfo().loadNextPage();
                }
                if (taskExecutor == null) {
                    TaskExecutor.defaultConcurrentTaskExecutor().post(netUiTask);
                } else {
                    taskExecutor.post(netUiTask);
                }
            }
        });
    }

    public SmartLoader updateExecuteBody(LoadDataUiTask.OnExecute executeBody) {
        loadDataUiTask = new LoadDataUiTask(smartRefreshLayout.getContext(), this);
        netUiTask = new NetUiTask(smartRefreshLayout.getContext(), loadDataUiTask, this);
        loadDataUiTask.setExecuteBody(executeBody);
        return this;
    }

    public SmartLoader updateExecuteBody(LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody) {
        loadListPageUiTask = new LoadListPageUiTask(smartRefreshLayout.getContext(), this);
        netUiTask = new NetUiTask(smartRefreshLayout.getContext(), loadListPageUiTask, this);
        loadListPageUiTask.setTaskInfo(taskInfo);
        loadListPageUiTask.setLoadListPageBody(executeBody);
        return this;
    }

    public SmartLoader setTaskExecutor(ITaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        return this;
    }

    public SmartLoader setSmartRefreshConfig(SmartConfig.SmartRefreshConfig smartRefreshConfig) {
        RefreshFooter refreshFooter = smartRefreshConfig.getRefreshFooterView(smartRefreshLayout.getContext());
        smartRefreshLayout.setRefreshFooter(refreshFooter);
        return this;
    }

    public void autoLoadMore() {
        smartRefreshLayout.autoLoadMore(0, 400, 0.5f, false);
    }

    @Override public final void onNoNetConnection(Object data) {
        super.onNoNetConnection(data);
        smartRefreshLayout.finishLoadMore();
    }

    @Override public final void onNetError(Object data) {
        smartRefreshLayout.finishLoadMore();
        Toast.makeText(smartRefreshLayout.getContext(), data.toString(), Toast.LENGTH_LONG).show();
    }

    @Override protected final void onSuccess(Object tipData) {
        if (onLoadSuccessListener != null) {
            onLoadSuccessListener.onLoadSuccess(this);
        }
    }

    @Override protected void onFinish(Object tipData) {
        smartRefreshLayout.finishLoadMore();
    }

    public interface OnLoadSuccessListener {
        void onLoadSuccess(SmartLoader smartLoader);
    }
}
