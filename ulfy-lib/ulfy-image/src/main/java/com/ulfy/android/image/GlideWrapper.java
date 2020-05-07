package com.ulfy.android.image;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.ulfy.android.cache.ICache;

import java.io.File;
import java.io.Serializable;
import java.security.MessageDigest;

/**
 * 图片加载底层实现，对Glide的包装
 */
class GlideWrapper {
    private static final GlideWrapper instance = new GlideWrapper();

    private GlideWrapper() { }

    static GlideWrapper getInstance() {
        return instance;
    }

    /**
     * 加载图片
     * @param url                   图片地址
     * @param placeholder           加载占位图
     * @param errorholder           错误占位图
     * @param imageView             目标ImageView
     * @param bitmapProcessNode     图片转换
     */
    void loadImage(String url, int placeholder, int errorholder, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        loadImageInner(url, placeholder, errorholder, imageView, bitmapProcessNode);
    }

    /**
     * 预加载图片
     *      不会阻塞线程且会将图片预加载到多级缓存中
     */
    void preload(String url) {
        if (url != null && url.length() != 0) {
            Glide.with(ImageConfig.context).load(url).signature(new ObjectKey(GlideCache.getInstance().getGlideSignature())).preload();
        }
    }

    /**
     * 下载图片，返回下载图片的路径
     *      会阻塞线程，只会将图片下载硬盘缓存中
     */
    File download(String url) throws Exception {
        if (url == null || url.length() == 0) {
            return null;
        } else {
            return Glide.with(ImageConfig.context).download(url).signature(new ObjectKey(GlideCache.getInstance().getGlideSignature())).submit().get();
        }
    }

    /**
     * 获取缓存大小
     */
    long getCacheSize() {
        File cacheDir = Glide.getPhotoCacheDir(ImageConfig.context);
        return cacheDir == null ? 0 : getFolderSize(cacheDir);
    }

    /*
    清理缓存需要在主线成和后台线程中分别执行，同时生成新的glide加载签名，这里只在清理硬盘缓存中做了该操作
    但是一定要调用
     */

    /**
     * 清除内存缓存
     *      只能在主线程执行
     */
    void clearImageMemoryCache() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("clear memory cache run only in main thread");
        }
        Glide.get(ImageConfig.context).clearMemory();
    }

    /**
     * 清除硬盘缓存
     *      只能在后台线程运行
     */
    void clearImageDiskCache() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("clear disk cache run only in background thread");
        }
        Glide.get(ImageConfig.context).clearDiskCache();
        GlideCache.getInstance().generateNewGildeSignature();
    }



    ///////////////////////////////////////////////////////////////////////////
    // 辅助方法
    ///////////////////////////////////////////////////////////////////////////

    @SuppressLint("CheckResult")
    private void loadImageInner(String url, int placeholder, int errorholder, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        if (imageView == null || isDestroy(imageView.getContext())) {
            return;
        }

        if (url == null || url.length() == 0) {
            if (errorholder > 0) {
                imageView.setImageResource(errorholder);
            } else if (placeholder > 0) {
                imageView.setImageResource(placeholder);
            }
            return;
        }

        Context context;

        /*
        Glide在局部上下文中会根据onPause、onResume中动态的停止或回复加载过程
        当设置了加载动画或错误占位图时如果图片加载失败了在onResume后Glide会显示占位图且不会调用相关的回调导致展示出错
        因此如果有加载占位图动画或错误占位图则使用管全局上下文，只设置占位图的情况下才使用局部上下文
         */

        // 当开启加载动画或设置错误占位图时，要使用全局上下文（）
        if (ImageConfig.Config.imageLoadingAnimator || errorholder > 0) {
            context = ImageConfig.context;
        } else {
            context = imageView.getContext();
        }

        RequestBuilder<Drawable> requestBuilder = Glide.with(context).load(url);

        // 基本设置
        if (placeholder > 0) {                                  // 占位图
            requestBuilder.placeholder(placeholder);
        }
        if (errorholder > 0) {                                  // 错误图
            requestBuilder.error(errorholder);
        }
        if (ImageConfig.Config.scale != 0) {                    // 缩略图
            requestBuilder.thumbnail(ImageConfig.Config.scale);
        }
        if (bitmapProcessNode != null) {                        // 图片转换
            requestBuilder.transform(new ImageTransform(bitmapProcessNode));
        }
        if (ImageConfig.Config.imageTransitionAnimator) {       // 过渡动画
            requestBuilder.transition(DrawableTransitionOptions.withCrossFade());
        }

        // url地址失效
        requestBuilder.signature(new ObjectKey(GlideCache.getInstance().getGlideSignature()));

        // 加载时占位图动画
        if (ImageConfig.Config.imageLoadingAnimator) {
            ImageLoadingAnimatorRepository.getInstance().updateImageUrlMapping(imageView, url);
            ImageLoadingAnimatorRepository.getInstance().startLoading(url);

            requestBuilder.listener(new RequestListener<Drawable>() {
                @Override public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    ImageLoadingAnimatorRepository.getInstance().stopLoading(model.toString());
                    return false;
                }
                @Override public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    ImageLoadingAnimatorRepository.getInstance().stopLoading(model.toString());
                    return false;
                }
            });
        }

        requestBuilder.into(imageView);
    }

    /**
     * 判断一个上下文对应的Activity是否被销毁了
     */
    private boolean isDestroy(Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return activity.isFinishing() || activity.isDestroyed();
        } else {
            return true;
        }
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     */
    private long getFolderSize(File folder) {
        long size = 0;
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                size += getFolderSize(file);
            } else {
                size += file.length();
            }
        }
        return size;
    }

    /**
     * 图片转换
     *      尽量不要使用该类做图像裁切，处于扩展性的考虑该类没有完全遵循Glide的建议
     *      这样会导致其复用机制失效，因此平时的图片裁切应当使用ShapeLayout
     */
    static class ImageTransform extends BitmapTransformation {
        private BitmapProcessNode bitmapProcessNode;

        ImageTransform(BitmapProcessNode bitmapProcessNode) {
            this.bitmapProcessNode = bitmapProcessNode;
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            if (toTransform == null) {
                return null;
            } else if (bitmapProcessNode == null) {
                return toTransform;
            } else {
                return bitmapProcessNode.processBitmap(toTransform);
            }
        }

        @Override public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) { }
    }

    /**
     * 用于存储当前图片对应的url是否过期的缓存
     */
    static class GlideCache implements Serializable {
        private static final long serialVersionUID = -3521121137343819538L;
        /**
         * glide 加载图片使用的一个标记，用于表示图片是否过期
         *      若是同一个url的图片被替换掉了，则此时glide是不知道的，因此需要在清除缓存的时候人为的标记一下
         */
        private long glideSignature = System.currentTimeMillis();

        /**
         * 私有化构造方法
         */
        private GlideCache() { }

        /**
         * 获取实例
         */
        static GlideCache getInstance() {
            ICache cache = ImageConfig.cache;
            return cache.isCached(GlideCache.class) ? cache.getCache(GlideCache.class) : cache.cache(new GlideCache());
        }

        /**
         * 获取glide加载标记
         */
        long getGlideSignature() {
            return glideSignature;
        }

        /**
         * 生成一个新的加载标记
         */
        void generateNewGildeSignature() {
            glideSignature = System.currentTimeMillis();
            ImageConfig.cache.cache(this);
        }
    }
}
