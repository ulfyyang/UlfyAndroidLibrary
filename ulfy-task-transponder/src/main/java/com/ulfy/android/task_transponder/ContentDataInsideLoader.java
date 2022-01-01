package com.ulfy.android.task_transponder;

import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

/**
 * 内容加载器，该加载器会在页面的指定位置上进行操作
 */
public class ContentDataInsideLoader extends NoNetConnectionTransponder {
    private ViewGroup container;                        // 执行过程中操作的容器
    private IViewModel model;                           // 当任务执行成功后使用的数据模型
    private View view;                                  // 执行成功后保留的 View，保留是为了尽可能的复用
    private InsideLoader insideLoader;                  // 内部加载器
    private OnReloadListener onReloadListener;          // 当需要重新加载的时候的回调监听
    private TaskTransponderConfig.ContentDataLoaderInsideConfig contentDataLoaderInsideConfig;          // 表现配置

    public ContentDataInsideLoader(ViewGroup container, IViewModel model) {
        this.container = container;
        this.model = model;
        view = UiUtils.createViewFromClazz(container.getContext(), (Class<? extends View>) model.getViewClass());
        if (view instanceof InsideLoaderView) {
            insideLoader = ((InsideLoaderView) view).proviceInsideLoader();
        } else {
            throw new IllegalStateException("view must be a instance of InsideLoaderView");
        }
        contentDataLoaderInsideConfig = TaskTransponderConfig.Config.contentDataLoaderInsideConfig;
        onCreateView(this, view);
        UiUtils.displayViewOnViewGroup(view, container);
    }

    @Override public void onNetError(Object data) {
        IReloadView netErrorView = contentDataLoaderInsideConfig.getNetErrorView(container.getContext());
        netErrorView.setOnReloadListener(onReloadListener);
        if (netErrorView instanceof ITipView) {
            ((ITipView)netErrorView).setTipMessage(data);
        }
        UiUtils.displayViewOnViewGroup((View) netErrorView, insideLoader.container);
    }

    @Override public void onStart(Object tipData) {
        ITipView loadingView = contentDataLoaderInsideConfig.getLoadingView(container.getContext());
        loadingView.setTipMessage(tipData);
        UiUtils.displayViewOnViewGroup((View) loadingView, insideLoader.container);
    }

    @Override public void onSuccess(Object tipData) {
        ((IView)view).bind(model);
        UiUtils.displayViewOnViewGroup(insideLoader.insideView, insideLoader.container);
    }

    @Override public void onFail(Object tipData) {
        IReloadView failView = contentDataLoaderInsideConfig.getFailView(container.getContext());
        failView.setOnReloadListener(onReloadListener);
        if (failView instanceof ITipView) {
            ((ITipView)failView).setTipMessage(tipData);
        }
        UiUtils.displayViewOnViewGroup((View) failView, insideLoader.container);
    }

    @Override protected void onUpdate(Object data) {
        if (insideLoader.container.getChildCount() > 0 && insideLoader.container.getChildAt(0) instanceof ITipView) {
            ITipView loadingView = (ITipView) insideLoader.container.getChildAt(0);
            loadingView.setTipMessage(data);
        }
    }

    /**
     * 当创建view的时候会调用该方法
     */
    protected void onCreateView(ContentDataInsideLoader loader, View createdView) {}

    /**
     * 获取加载成功界面，该方法在onSuccess回调中调用才有内容
     */
    public final View getView() {
        return view;
    }

    /**
     * 设置当需要重新加载的回调监听
     */
    public final ContentDataInsideLoader setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
        return this;
    }

    /**
     * 设置页面显示样式配置
     */
    public final ContentDataInsideLoader setContentDataLoaderInsideConfig(TaskTransponderConfig.ContentDataLoaderInsideConfig contentDataLoaderInsideConfig) {
        this.contentDataLoaderInsideConfig = contentDataLoaderInsideConfig;
        return this;
    }

    /**
     * 内部加载器View，要实现内部加载特性需要实现该接口
     */
    public interface InsideLoaderView {
        public InsideLoader proviceInsideLoader();
    }

    /**
     * 内部加载器，需要提供加载占位的容器和加载完成后被还原的View
     */
    public static class InsideLoader {
        private ViewGroup container;
        private View insideView;

        public InsideLoader(ViewGroup container, View insideView) {
            this.container = container;
            this.insideView = insideView;
        }
    }
}
