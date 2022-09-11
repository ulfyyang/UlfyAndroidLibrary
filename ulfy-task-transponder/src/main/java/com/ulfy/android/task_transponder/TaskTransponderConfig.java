package com.ulfy.android.task_transponder;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

public final class TaskTransponderConfig {
    static Application context;

    /**
     * 初始化任务详情器模块
     */
    static void init(Application context) {
        TaskTransponderConfig.context = context;
        NetStateListener.listenNetStateChanged(context);
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ActivityRepository.getInstance().notifyActivityCreated(activity);
            }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) {
                DialogRepository.getInstance().releaseDialogOnActivityDestory(activity);
                ActivityRepository.getInstance().notifyActivityDestroyed(activity);
            }
        });
    }

    public static final class Config {
        public static ContentDataLoaderInsideConfig contentDataLoaderInsideConfig;
        public static ContentDataLoaderConfig contentDataLoaderConfig;
        public static ContentDataRecyclerLoaderConfig contentDataRecyclerLoaderConfig;
        public static ContentDataRefresherConfig contentDataRefresherConfig;
        public static DialogProcesserConfig dialogProcesserConfig;
        public static ListPageLoaderConfig listPageLoaderConfig;

        static {
            contentDataLoaderInsideConfig = new DefaultContentDataLoaderInsideConfig();
            contentDataLoaderConfig = new DefaultContentDataLoaderConfig();
            contentDataRecyclerLoaderConfig = new DefaultContentDataRecyclerLoaderConfig();
            contentDataRefresherConfig = new DefaultContentDataRefresherConfig();
            dialogProcesserConfig = new DefaultDialogProcesserConfig();
            listPageLoaderConfig = new DefaultListPageLoaderConfig();
        }
    }

    /**
     * 内部容器加载器配置
     */
    public interface ContentDataLoaderInsideConfig {
        /**
         * 当网络错误的时候显示的页面
         */
        public IReloadView getNetErrorView(Context context);
        /**
         * 当加载失败后显示的页面
         */
        public IReloadView getFailView(Context context);
        /**
         * 加载中时显示的页面
         */
        public ITipView getLoadingView(Context context);
    }

    public static class DefaultContentDataLoaderInsideConfig implements ContentDataLoaderInsideConfig {
        @Override public IReloadView getNetErrorView(Context context) {
            return new ContentDataLoaderFailedView(context);
        }
        @Override public IReloadView getFailView(Context context) {
            return new ContentDataLoaderFailedView(context);
        }
        @Override public ITipView getLoadingView(Context context) {
            return new ContentDataLoaderLoadingView(context);
        }
    }

    /**
     * 内容加载器配置
     */
    public interface ContentDataLoaderConfig {
        /**
         * 当网络错误的时候显示的页面
         */
        public IReloadView getNetErrorView(Context context);
        /**
         * 当加载失败后显示的页面
         */
        public IReloadView getFailView(Context context);
        /**
         * 加载中时显示的页面
         */
        public ITipView getLoadingView(Context context);
    }

    public static class DefaultContentDataLoaderConfig implements ContentDataLoaderConfig {
        @Override public IReloadView getNetErrorView(Context context) {
            return new ContentDataLoaderFailedView(context);
        }
        @Override public IReloadView getFailView(Context context) {
            return new ContentDataLoaderFailedView(context);
        }
        @Override public ITipView getLoadingView(Context context) {
            return new ContentDataLoaderLoadingView(context);
        }
    }

    /**
     * Recycler内容加载器配置
     */
    public interface ContentDataRecyclerLoaderConfig {
        /**
         * 当网络错误的时候显示的页面
         */
        public IReloadView getNetErrorView(Context context);
        /**
         * 当加载失败后显示的页面
         */
        public IReloadView getFailView(Context context);
        /**
         * 加载中时显示的页面
         */
        public ITipView getLoadingView(Context context);
    }

    public static class DefaultContentDataRecyclerLoaderConfig implements ContentDataRecyclerLoaderConfig {
        @Override public IReloadView getNetErrorView(Context context) {
            return new ContentDataRecyclerLoaderFailedView(context);
        }
        @Override public IReloadView getFailView(Context context) {
            return new ContentDataRecyclerLoaderFailedView(context);
        }
        @Override public ITipView getLoadingView(Context context) {
            return new ContentDataRecyclerLoaderLoadingView(context);
        }
    }

    /**
     * 内部容器刷新器配置
     */
    public interface ContentDataRefresherConfig {
        /**
         * 当网络错误的时候显示的页面
         */
        public IReloadView getNetErrorView(Context context);
        /**
         * 当加载失败后显示的页面
         */
        public IReloadView getFailView(Context context);
        /**
         * 加载中时显示的页面
         */
        public ITipView getLoadingView(Context context);
    }

    public static class DefaultContentDataRefresherConfig implements ContentDataRefresherConfig {
        @Override public IReloadView getNetErrorView(Context context) {
            return new ContentDataLoaderFailedView(context);
        }
        @Override public IReloadView getFailView(Context context) {
            return new ContentDataLoaderFailedView(context);
        }
        @Override public ITipView getLoadingView(Context context) {
            return new ContentDataLoaderLoadingView(context);
        }
    }

    /**
     * 弹出框处理器
     */
    public interface DialogProcesserConfig {
        /**
         * 加载中时显示的页面
         */
        public ITipView getLoadingView(Context context);
        /**
         * 是否显示错误提示
         */
        public boolean tipError();
        /**
         * 点击外部是否自动关闭
         */
        public boolean touchOutsideDismiss();
        /**
         * 是否可以点击反馈取消
         */
        public boolean cancelable();
        /**
         * 是否采用默认的背景遮罩层
         */
        public boolean noBackground();
    }

    public static class DefaultDialogProcesserConfig implements DialogProcesserConfig {
        @Override public ITipView getLoadingView(Context context) {
            return new DialogProcessView(context);
        }
        @Override public boolean tipError() {
            return true;
        }
        @Override public boolean touchOutsideDismiss() {
            return false;
        }
        @Override public boolean cancelable() {
            return false;
        }
        @Override public boolean noBackground() {
            return false;
        }
    }

    /**
     * 上拉加载器配置（包括ListView和RecyclerView）
     */
    public interface ListPageLoaderConfig {
        /**
         * 获取上拉加载时底部显示的View
         */
        public IListPageFooterView getListPageFooterView(Context context);
    }

    public static class DefaultListPageLoaderConfig implements ListPageLoaderConfig {
        @Override public IListPageFooterView getListPageFooterView(Context context) {
            return new LoadListPageFooterView(context);
        }
    }
}
