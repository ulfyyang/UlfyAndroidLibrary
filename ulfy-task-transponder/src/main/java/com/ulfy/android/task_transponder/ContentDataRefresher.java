package com.ulfy.android.task_transponder;

import android.view.View;
import android.view.ViewGroup;

/**
 * 内容刷新器
 * 该容器只适合刷新在布局文件中布局好的view
 */
public class ContentDataRefresher extends NoNetConnectionTransponder {
    private ViewGroup container;                        // 执行过程中操作的容器
    private View refreshV;                              // 需要刷新的界面
    private ViewGroup.LayoutParams refreshLP;           // 刷新界面的布局参数
    private OnReloadListener onReloadListener;          // 当需要重新加载的时候的回调监听
    private TaskTransponderConfig.ContentDataRefresherConfig contentDataRefresherConfig;                // 表现配置

    public ContentDataRefresher(ViewGroup container, View refreshV) {
        this.container = container;
        this.refreshV = refreshV;
        this.refreshLP = refreshV.getLayoutParams();
        contentDataRefresherConfig = TaskTransponderConfig.Config.contentDataRefresherConfig;
    }

    @Override public void onNetError(Object data) {
        IReloadView netErrorView = contentDataRefresherConfig.getNetErrorView(container.getContext());
        netErrorView.setOnReloadListener(onReloadListener);
        if (netErrorView instanceof ITipView) {
            ((ITipView)netErrorView).setTipMessage(data);
        }
        UiUtils.displayViewOnViewGroup((View) netErrorView, container);
    }

    @Override public void onStart(Object tipData) {
        ITipView loadingView = contentDataRefresherConfig.getLoadingView(container.getContext());
        loadingView.setTipMessage(tipData);
        UiUtils.displayViewOnViewGroup((View) loadingView, container);
    }

    @Override public void onSuccess(Object tipData) {
        UiUtils.displayViewOnViewGroup(refreshV, container, refreshLP);
    }

    @Override public void onFail(Object tipData) {
        IReloadView failView = contentDataRefresherConfig.getFailView(container.getContext());
        failView.setOnReloadListener(onReloadListener);
        if (failView instanceof ITipView) {
            ((ITipView)failView).setTipMessage(tipData);
        }
        UiUtils.displayViewOnViewGroup((View) failView, container);
    }

    @Override protected void onUpdate(Object data) {
        if (container.getChildCount() > 0 && container.getChildAt(0) instanceof ITipView) {
            ITipView loadingView = (ITipView) container.getChildAt(0);
            loadingView.setTipMessage(data);
        }
    }

    /**
     * 设置当需要重新加载的回调监听
     */
    public final ContentDataRefresher setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
        return this;
    }

    /**
     * 设置页面显示样式配置
     */
    public final ContentDataRefresher setContentDataRefresherConfig(TaskTransponderConfig.ContentDataRefresherConfig contentDataRefresherConfig) {
        this.contentDataRefresherConfig = contentDataRefresherConfig;
        return this;
    }
}
