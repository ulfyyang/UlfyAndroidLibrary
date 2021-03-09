package com.ulfy.android.image;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.ulfy.android.cache.CacheConfig;
import com.ulfy.android.cache.ICache;

public final class ImageConfig {
    static boolean configured;
    static Application context;
    static ICache cache;

    /**
     * 初始化图片模块
     */
    public static void init(Application context) {
        if (!configured) {

            configured = true;
            ImageConfig.context = context;
            cache = CacheConfig.newMemoryDiskCache(context, Config.recordInfoCacheDirName);

            ImageWatcherWrapper.getInstance().init(context);

            context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
                @Override public void onActivityStarted(Activity activity) { }
                @Override public void onActivityResumed(Activity activity) { }
                @Override public void onActivityPaused(Activity activity) { }
                @Override public void onActivityStopped(Activity activity) { }
                @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
                @Override public void onActivityDestroyed(Activity activity) {
                    ImageLoadingAnimatorRepository.getInstance().releaseOnActivityDestoryed(activity);
                }
            });
        }
    }

    /**
     * 模块配置
     */
    public static final class Config {
        /**
         * 加载图片时是否使用占位图动画（加载中的占位图动画）
         *      该动画的开启需要指定具有ImageLevel属性的Drawable作为占位图
         *      使用占位图动画需要指定加载失败的占位图，否则当加载失败时动画停止会看起来怪怪的
         */
        public static boolean imageLoadingAnimator = true;
        /**
         * 加载图片时是否使用过渡动画（加载完成时缓慢显示的动画）
         *      尽量不要开启，设置占位图且ImageView设置了CENTER_CROP属性会导致图片显示错乱或抖动错位
         */
        public static boolean imageTransitionAnimator = false;
        /**
         * 是否开始缩略图支持，0表示不开启
         *      尽量开启缩略图，因为大部分页面开启缩略图肉眼看不出来区别；开启缩略图能延迟加载真正图片的时间
         *      提高框架获取真实ImageView大小的概率
         */
        public static float scale = 0.5f;
        /**
         * 用于跟踪下载信息的缓存目录
         */
        public static String recordInfoCacheDirName = "image_cache";
    }

    static void throwExceptionIfConfigNotConfigured() {
        if (!configured) {
            throw new IllegalStateException("Image not configured in Application entrace, please add ImageConfig.init(this); to Application");
        }
    }
}
