package com.ulfy.android.task_transponder;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.task.LoadListPageUiTask;
import com.ulfy.android.task.NetUiTask;
import com.ulfy.android.task.TaskExecutor;

public class RecyclerViewPageLoader extends NoNetConnectionTransponder {
    private RecyclerView recyclerView;                              // 操作的RecyclerView组件
    private RecyclerAdapter recyclerAdapter;                        // 数据适配器
    private IListPageFooterView footerView;                         // 底部显示的View
    private NetUiTask netUiTask;                                    // 拥有网络控制功能任务代理
    private LoadListPageUiTask loadListPageUiTask;                  // 分页加载的任务
    private LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo;     // 任务执行的信息
    private OnLoadSuccessListener onLoadSuccessListener;            // 当上拉加载完成之后在数据更新之前调用

    public RecyclerViewPageLoader(RecyclerView recyclerView, RecyclerAdapter recyclerAdapter, OnLoadSuccessListener onLoadSuccessListener) {
        this.recyclerView = recyclerView;
        this.recyclerAdapter = recyclerAdapter;
        this.onLoadSuccessListener = onLoadSuccessListener;
        footerView = TaskTransponderConfig.Config.listPageLoaderConfig.getListPageFooterView(recyclerView.getContext());
        loadListPageUiTask = new LoadListPageUiTask(recyclerView.getContext(), this);
        netUiTask = new NetUiTask(recyclerView.getContext(), loadListPageUiTask, this);
        initRecyclerViewThenConfig();
    }

    private void initRecyclerViewThenConfig() {
        recyclerAdapter.setFooterView((View) footerView);
        footerView.showNoData();
        recyclerView.addOnScrollListener(new OnScrollListenerInner(recyclerView));
        footerView.setOnReloadListener(new OnReloadListener() {
            @Override public void onReload() {
                taskInfo.loadNextPage();
                TaskExecutor.defaultExecutor().post(netUiTask);
            }
        });
    }

    @Override public void onNoNetConnection(Object data) {
        footerView.showError(data);
    }

    @Override public void onNetError(Object data) {
        footerView.showError(data);
    }

    @Override public void onStart(Object data) {
        footerView.showLoading();
    }

    @Override public void onSuccess(Object data) {
        if (onLoadSuccessListener != null) {
            onLoadSuccessListener.onLoadSuccess(this);
        }
        // 加载成功后更新数据列表，当加载的是最后一页时显示加载到底的界面。
        // 如果加载的不是最后一页，则应该隐藏掉加载动画
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
        if (taskInfo.isLoadedEndPage()) {
            footerView.showNoData();
        } else {
            footerView.gone();
        }
    }

    @Override public void onFail(Object data) {
        footerView.showError(data);
    }

    public final void updateExecuteBody(LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody) {
        this.taskInfo = taskInfo;
        loadListPageUiTask.setTaskInfo(taskInfo);
        loadListPageUiTask.setLoadListPageBody(executeBody);
    }

    /**
     * 当外部调用了adapter.notifyDataSetChanged方法时，最好调用一下该方法
     *      该方法可以解决当数据不满一屏时的状态显示问题
     */
    public final void notifyDataSetChanged() {
        if (taskInfo != null) {
            // 如果铺满一屏了且到数据加载完毕了显示无数据页面，否则显示加载中页面
            if (recyclerView.getChildCount() < recyclerView.getAdapter().getItemCount()) {
                if (taskInfo.isLoadedEndPage()) {
                    footerView.showNoData();
                } else {
                    footerView.showLoading();
                }
            }
            // 如果铺不满一屏且存在数据则显示没有数据，否则显示一个空白的白板
            else {
                if (!taskInfo.isLoadedEndPage()) {
                    taskInfo.setEndPage(LoadListPageUiTask.DEFAULT_START_PAGE);
                    if (taskInfo.getDataList().size() > 0) {
                        footerView.showNoData();
                    } else {
                        footerView.gone();
                    }
                }
            }
        }
    }

    /**
     * 当任务加载成功后执行的一个回调
     */
    public interface OnLoadSuccessListener {
        void onLoadSuccess(RecyclerViewPageLoader recyclerViewPageLoader);
    }

    private class OnScrollListenerInner extends RecyclerView.OnScrollListener {
        private RecyclerView.LayoutManager layoutManager;

        public OnScrollListenerInner(RecyclerView recyclerView) {
            this.layoutManager = recyclerView.getLayoutManager();
            if (!(layoutManager instanceof LinearLayoutManager) && !(layoutManager instanceof StaggeredGridLayoutManager)) {
                throw new IllegalStateException("LayoutManager must be a instance of LinearLayoutManager or StaggeredGridLayoutManager");
            }
        }

        @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (taskInfo != null) {
                // 如果铺满一屏了且到数据加载完毕了显示无数据页面，否则显示加载中页面
                if (recyclerView.getChildCount() < recyclerView.getAdapter().getItemCount()) {
                    if (taskInfo.isLoadedEndPage()) {
                        footerView.showNoData();
                    } else {
                        footerView.showLoading();
                    }
                }
                // 如果铺不满一屏且存在数据则显示没有数据，否则显示一个空白的白板
                else {
                    if (!taskInfo.isLoadedEndPage()) {
//                        taskInfo.setEndPage(LoadListPageUiTask.DEFAULT_START_PAGE);
                        if (taskInfo.getDataList().size() > 0) {
                            footerView.showNoData();
                        } else {
                            footerView.gone();
                        }
                    }
                }
            }
        }

        @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            // 当滚动停止、数据够铺满一屏、最后一个可见时，根据条件触发是否应该上拉加载更多任务
            if (newState == RecyclerView.SCROLL_STATE_IDLE && recyclerView.getChildCount() < recyclerView.getAdapter().getItemCount() && getLastVisiableItemPosition() == layoutManager.getItemCount() - 1) {
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

        private int getLastVisiableItemPosition() {
            int lastVisibleItemPosition = 0;
            if (layoutManager instanceof LinearLayoutManager) {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                int[] lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                if (lastPositions.length > 0) {
                    lastVisibleItemPosition = lastPositions[0];
                    for (int position : lastPositions) {
                        lastVisibleItemPosition = Math.max(lastVisibleItemPosition, position);
                    }
                }
            }
            return lastVisibleItemPosition;
        }
    }
}
