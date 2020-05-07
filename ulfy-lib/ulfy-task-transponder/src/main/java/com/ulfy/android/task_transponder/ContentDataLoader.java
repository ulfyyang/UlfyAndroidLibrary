package com.ulfy.android.task_transponder;

import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

/**
 * 内容加载器，该加载器会在页面的指定位置上进行操作
 */
public class ContentDataLoader extends NoNetConnectionTransponder {
    private ViewGroup container;                        // 执行过程中操作的容器
    private IViewModel model;                           // 当任务执行成功后使用的数据模型
    private View view;                                  // 执行成功后保留的 View，保留是为了尽可能的复用
    private boolean showFirst;                          // 是否先显示出来
    private OnReloadListener onReloadListener;          // 当需要重新加载的时候的回调监听
    private TaskTransponderConfig.ContentDataLoaderConfig contentDataLoaderConfig;                      // 表现配置

    public ContentDataLoader(ViewGroup container, IViewModel model, boolean showFirst) {
        this.container = container;
        this.model = model;
        this.showFirst = showFirst;
        this.contentDataLoaderConfig = TaskTransponderConfig.Config.contentDataLoaderConfig;
        if (showFirst) {        // 如果优先显示则直接显示到界面上
            this.view = UiUtils.createViewFromClazz(container.getContext(), (Class<? extends View>) model.getViewClass());
            onCreatView(this, this.view);
            UiUtils.displayViewOnViewGroup(this.view, container);
        }
    }

    @Override public void onNetError(Object data) {
        IReloadView netErrorView = contentDataLoaderConfig.getNetErrorView(container.getContext());
        netErrorView.setOnReloadListener(onReloadListener);
        if (netErrorView instanceof ITipView) {
            ((ITipView)netErrorView).setTipMessage(data);
        }
        UiUtils.displayViewOnViewGroup((View) netErrorView, container);
    }

    @Override public void onStart(Object tipData) {
        if (!showFirst) {       // 如果不优先显示，则显示加载动画
            ITipView loadingView = contentDataLoaderConfig.getLoadingView(container.getContext());
            loadingView.setTipMessage(tipData);
            UiUtils.displayViewOnViewGroup((View) loadingView, container);
        }
    }

    @Override public void onSuccess(Object tipData) {
        if (showFirst) {
            ((IView)view).bind(model);
        } else {
            view = UiUtils.createViewFromClazz(container.getContext(), (Class<? extends View>) model.getViewClass());
            ((IView)view).bind(model);
            onCreatView(this, view);
            UiUtils.displayViewOnViewGroup(view, container);
        }
    }

    @Override public void onFail(Object tipData) {
        IReloadView failView = contentDataLoaderConfig.getFailView(container.getContext());
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
     * 当创建view的时候会调用该方法
     */
    protected void onCreatView(ContentDataLoader loader, View createdView) {}

    /**
     * 获取加载成功界面，该方法在onSuccess回调中调用才有内容
     */
    public final View getView() {
        return view;
    }

    /**
     * 设置当需要重新加载的回调监听
     */
    public final ContentDataLoader setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
        return this;
    }

    /**
     * 设置页面显示样式配置
     */
    public final ContentDataLoader setContentDataLoaderConfig(TaskTransponderConfig.ContentDataLoaderConfig contentDataLoaderConfig) {
        this.contentDataLoaderConfig = contentDataLoaderConfig;
        return this;
    }
}
