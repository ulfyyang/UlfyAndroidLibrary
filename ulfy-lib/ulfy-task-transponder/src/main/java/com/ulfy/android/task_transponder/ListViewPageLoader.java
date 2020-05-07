package com.ulfy.android.task_transponder;

import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

import com.ulfy.android.task.LoadListPageUiTask;
import com.ulfy.android.task.NetUiTask;
import com.ulfy.android.task.TaskExecutor;

/**
 * 用于ListView的上拉加载分页加载器
 */
public class ListViewPageLoader extends NoNetConnectionTransponder {
    private ListView listView;                                      // 操作的ListView控件
    private IListPageFooterView footerView;                         // ListView下边显示的加载更多界面
    private NetUiTask netUiTask;                                    // 拥有网络控制功能任务代理
    private LoadListPageUiTask loadListPageUiTask;                  // 分页加载的任务
    private LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo;     // 任务执行的信息
    private AbsListView.OnScrollListener onScrollListener;          // 用于供外部使用的滚动监听器
    private OnLoadSuccessListener onLoadSuccessListener;            // 当上拉加载完成之后在数据更新之前调用
    private boolean updateingFooter;                                // 由于更新footer会触发ListView的滚动事件，容易导致显示错误，这里采用一个变量来忽略掉这些滚动处理

    public ListViewPageLoader(ListView listView, OnLoadSuccessListener onLoadSuccessListener) {
        this.listView = listView;
        this.onLoadSuccessListener = onLoadSuccessListener;
        footerView = TaskTransponderConfig.Config.listPageLoaderConfig.getListPageFooterView(listView.getContext());
        loadListPageUiTask = new LoadListPageUiTask(listView.getContext(), this);
        netUiTask = new NetUiTask(listView.getContext(), loadListPageUiTask, this);
        initListViewThenConfig();
    }

    private void initListViewThenConfig() {
        listView.addFooterView((View) footerView, null, false);
        // 为兼容某些机型，显示的设置分割线属性
        listView.setHeaderDividersEnabled(false);
        listView.setFooterDividersEnabled(false);
        // 隐藏滚动条
        listView.setVerticalScrollBarEnabled(false);
        // 隐藏加载更多界面
        footerView.gone();
        listView.setOnScrollListener(new OnScrollListenerInner());
        footerView.setOnReloadListener(new OnReloadListener() {
            @Override public void onReload() {
                taskInfo.loadNextPage();
                TaskExecutor.defaultExecutor().post(netUiTask);
            }
        });
    }


    @Override public void onNoNetConnection(Object data) {
        footerView.showError(data);
        updateingFooter = true;
    }

    @Override public void onNetError(Object data) {
        footerView.showError(data);
        updateingFooter = true;
    }

    @Override public void onStart(Object data) {
        footerView.showLoading();
        updateingFooter = true;
    }

    @Override public void onSuccess(Object data) {
        if (onLoadSuccessListener != null) {
            onLoadSuccessListener.onLoadSuccess(this);
        }
        // 加载成功后更新数据列表，当加载的是最后一页时显示加载到底的界面。
        // 如果加载的不是最后一页，则应该隐藏掉加载动画
        if (listView.getAdapter() != null) {
            HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) listView.getAdapter();
            BaseAdapter baseAdapter = (BaseAdapter) headerViewListAdapter.getWrappedAdapter();
            baseAdapter.notifyDataSetChanged();
        }
        if (taskInfo.isLoadedEndPage()) {
            footerView.showNoData();
        } else {
            footerView.gone();
        }
        updateingFooter = true;
    }

    @Override public void onFail(Object data) {
        footerView.showError(data);
        updateingFooter = true;
    }

    public final void updateExecuteBody(LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody) {
        this.taskInfo = taskInfo;
        loadListPageUiTask.setTaskInfo(taskInfo);
        loadListPageUiTask.setLoadListPageBody(executeBody);
    }

    public final void setOnScrollListener(AbsListView.OnScrollListener onScrollListener) {
        this.onScrollListener = onScrollListener;
    }

    /**
     * 当任务加载成功后执行的一个回调
     */
    public interface OnLoadSuccessListener {
        void onLoadSuccess(ListViewPageLoader listViewPageLoader);
    }

    /**
     * 滚动监听器，用于处理自动触发上拉加载的逻辑
     */
    private class OnScrollListenerInner implements AbsListView.OnScrollListener {

        @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
            if (taskInfo != null) {
                if (updateingFooter) {                  // 如果当前正在更新footer，则忽略
                    updateingFooter = false;
                } else {
                    if (visibleItemCount < totalItemCount) {            // 如果铺满一屏了且到数据加载完毕了显示无数据页面，否则显示加载中页面
                        if (taskInfo.isLoadedEndPage()) {
                            footerView.showNoData();
                        } else {
                            footerView.showLoading();
                        }
                    } else {
                        if (taskInfo.getDataList().size() > 0) {        // 如果铺不满一屏且存在数据则显示没有数据，否则显示一个空白的白板
                            footerView.showNoData();
                        } else {
                            footerView.gone();
                        }
                    }
                }
            }
        }

        @Override public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
            // 当滚动停止、数据够铺满一屏、最后一个可见时，根据条件触发是否应该上拉加载更多任务
            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && view.getChildCount() < view.getCount() && view.getLastVisiblePosition() == view.getCount() - 1) {
                // 如果任务已经加载到最后一页了则直接显示无数据页面
                if (taskInfo.isLoadedEndPage()) {
                    footerView.showNoData();
                }
                // 如果当前任务还在执行中则显示加载中页面
                else if (netUiTask.isRunning()) {
                    footerView.showLoading();
                }
                // 符合加载条件时调整任务加载状态，启动任务
                else {
                    taskInfo.loadNextPage();
                    TaskExecutor.defaultExecutor().post(netUiTask);
                }
            }
        }
    }

}
