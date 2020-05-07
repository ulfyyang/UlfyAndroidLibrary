package com.ulfy.android.image;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.github.ielse.imagewatcher.ImageWatcher;
import com.github.ielse.imagewatcher.ImageWatcherHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 大图预览功能，包装自三方库
 */
class ImageWatcherWrapper {
    private static final ImageWatcherWrapper instance = new ImageWatcherWrapper();
    private Map<Activity, ImageWatcherHelper> imageWatcherHelperMap = new HashMap<>();

    private ImageWatcherWrapper() { }

    static ImageWatcherWrapper getInstance() {
        return instance;
    }

    /**
     * 初始化上下文
     */
    void init(Application context) {
        context.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) {
                removeImageWatcherOnActivityDestroy(activity);
            }
        });
    }

    /**
     * 启动大图预览
     *      不会跟踪ImageView的位置
     */
    void preview(Context context, List<String> imagePathList, int showPosition) {
        if (context == null || imagePathList == null) {
            return;
        }
        if (showPosition >= imagePathList.size()) {
            showPosition = imagePathList.size() - 1;
        }

        List<Uri> uriList = new ArrayList<>();
        for (String url : imagePathList) {
            uriList.add(Uri.parse(url));
        }

        findImageWatcherByActivity((Activity) context).show(uriList, showPosition);
    }

    /**
     * 启动大图预览
     *      会跟踪ImageView的位置
     */
    void preview(ViewGroup viewGroup, List<String> imagePathList, int showPosition) {
        if (viewGroup == null || imagePathList == null) {
            return;
        }
        if (showPosition >= imagePathList.size()) {
            showPosition = imagePathList.size() - 1;
        }

        List<Uri> uriList = new ArrayList<>();
        for (String url : imagePathList) {
            uriList.add(Uri.parse(url));
        }

        SparseArray<ImageView> imageViewSparseArray = new SparseArray<>();
        List<ImageView> imageViewList = findAllImageView(viewGroup, new LinkedList<>());
        for (int i = 0; i < imageViewList.size(); i++) {
            imageViewSparseArray.put(i, imageViewList.get(i));
        }

        boolean positionInImageViews = showPosition < imageViewSparseArray.size();

        if (positionInImageViews) {
            findImageWatcherByActivity((Activity) viewGroup.getContext())
                    .show(imageViewSparseArray.get(showPosition), imageViewSparseArray, uriList);
        } else {
            findImageWatcherByActivity((Activity) viewGroup.getContext()).show(uriList, showPosition);
        }
    }

    /**
     * 是否处理Activity的返回事件
     */
    boolean handlePreviewBackPressed(Activity activity) {
        ImageWatcherHelper imageWatcherHelper = findImageWatcherByActivity(activity);
        return imageWatcherHelper != null && imageWatcherHelper.handleBackPressed();
    }

    /**
     * 获取打图预览的数量
     */
    int getImageWatcherHelperSize() {
        return imageWatcherHelperMap.size();
    }

    private ImageWatcherHelper findImageWatcherByActivity(Activity activity) {
        ImageWatcherHelper imageWatcherHelper = imageWatcherHelperMap.get(activity);

        if (imageWatcherHelper == null) {
            imageWatcherHelper = ImageWatcherHelper.with(activity, new GlideLoader());
            imageWatcherHelperMap.put(activity, imageWatcherHelper);
        }

        return imageWatcherHelper;
    }

    private void removeImageWatcherOnActivityDestroy(Activity activity) {
        imageWatcherHelperMap.remove(activity);
    }

    private List<ImageView> findAllImageView(ViewGroup viewGroup, List<ImageView> imageViewList) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childView = viewGroup.getChildAt(i);

            if (childView instanceof ImageView) {
                imageViewList.add((ImageView) childView);
            } else if (childView instanceof ViewGroup) {
                findAllImageView((ViewGroup) childView, imageViewList);
            }
        }

        return imageViewList;
    }

    private static class GlideLoader implements ImageWatcher.Loader {
        @Override public void load(Context context, Uri uri, final ImageWatcher.LoadCallback lc) {
            Glide.with(context).load(uri).signature(new ObjectKey(GlideWrapper.GlideCache.getInstance().getGlideSignature()))
                    .into(new CustomTarget<Drawable>() {
                        @Override public void onLoadCleared(@Nullable Drawable placeholder) { }
                        @Override public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            lc.onResourceReady(resource);
                        }
                        @Override public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            lc.onLoadFailed(errorDrawable);
                        }
//                        @Override public void onLoadStarted(@Nullable Drawable placeholder) {
//                            lc.onLoadStarted(placeholder);      // 这里会引发一直显示加载中的bug，因此这里先不要
//                        }
                    });
        }
    }
}
