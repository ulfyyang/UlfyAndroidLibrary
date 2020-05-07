package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.View;

import com.ulfy.android.adapter.RecyclerAdapter;

/**
 * Recycler加载器，该方法会在RecyclerView内部显示一个加载中的加载效果
 */
public class ContentDataRecyclerLoader extends NoNetConnectionTransponder {
    private Context context;                            // 当前所处的上下文
    private RecyclerAdapter adapter;                    // 显示数据的adapter
    private OnReloadListener onReloadListener;          // 当需要重新加载的时候的回调监听
    private TaskTransponderConfig.ContentDataRecyclerLoaderConfig contentDataRecyclerLoaderConfig;                      // 表现配置

    public ContentDataRecyclerLoader(Context context, RecyclerAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
        this.contentDataRecyclerLoaderConfig = TaskTransponderConfig.Config.contentDataRecyclerLoaderConfig;
    }

    @Override public void onNetError(Object data) {
        IReloadView netErrorView = contentDataRecyclerLoaderConfig.getNetErrorView(context);
        netErrorView.setOnReloadListener(onReloadListener);
        if (netErrorView instanceof ITipView) {
            ((ITipView)netErrorView).setTipMessage(data);
        }
        adapter.setLoadingView((View) netErrorView);
        adapter.notifyDataSetChanged();
    }

    @Override public void onStart(Object tipData) {
        ITipView loadingView = contentDataRecyclerLoaderConfig.getLoadingView(context);
        loadingView.setTipMessage(tipData);
        adapter.setLoadingView((View) loadingView);
        adapter.notifyDataSetChanged();
    }

    @Override public void onSuccess(Object tipData) {
        adapter.setLoadingView(null);
        adapter.notifyDataSetChanged();
    }

    @Override public void onFail(Object tipData) {
        IReloadView failView = contentDataRecyclerLoaderConfig.getFailView(context);
        failView.setOnReloadListener(onReloadListener);
        if (failView instanceof ITipView) {
            ((ITipView)failView).setTipMessage(tipData);
        }
        adapter.setLoadingView((View) failView);
        adapter.notifyDataSetChanged();
    }

    /**
     * 设置当需要重新加载的回调监听
     */
    public final ContentDataRecyclerLoader setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
        return this;
    }

    /**
     * 设置页面显示样式配置
     */
    public final ContentDataRecyclerLoader setContentDataRecyclerLoaderConfig(TaskTransponderConfig.ContentDataRecyclerLoaderConfig contentDataRecyclerLoaderConfig) {
        this.contentDataRecyclerLoaderConfig = contentDataRecyclerLoaderConfig;
        return this;
    }
}
