package com.ulfy.android.task;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public final class TaskConfig {

    /**
     * 初始化任务模块
     */
    public static void init(Application context) {
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) {
                TaskRepository.getInstance().releaseUiTaskOnActivityDestoryed(activity);
            }
        });
    }

    /**
     * 任务模块配置
     */
    public static final class Config {
        /**
         * 是否检查网络，默认不检查
         *      如果检查网络，则当无网络是会触发onNoNetConnection回调，正常的业务逻辑会被拦截
         *      客户端可以在无网络回调中作相应的页面处理
         */
        public static boolean checkInternet = false;
        /**
         * 网络检查时无网络提示的提示文字
         */
        public static String NO_NET_CONNECTION_TIP = "无网络链接";
        /**
         * 当没有执行体时常规加载任务会直接执行成功
         *      这时候的提示文字
         */
        public static String LOAD_DATA_SUCCESS_TIP = "加载完成";
        /**
         * 加载分页任务的默认实现
         *      任务开始时的提示文字
         */
        public static String LOAD_LIST_PAGE_START_TIP = "正在刷新数据...";
        /**
         * 加载分页任务的默认实现
         *      任务成功时：加载起始页的提示文字
         */
        public static String LOAD_LIST_PAGE_SUCCESS_START_PAGE_TIP = "刷新成功";
        /**
         * 加载分页任务的默认实现
         *      任务成功时：重新加载的提示文字
         */
        public static String LOAD_LIST_PAGE_SUCCESS_RELOAD_ALL_TIP = "刷新成功";
        /**
         * 加载分页任务的默认实现
         *      任务成功时：加载下一页的提示文字
         */
        public static String LOAD_LIST_PAGE_SUCCESS_NEXT_PAGE_TIP = "加载成功";
    }

}
