package com.ulfy.android.image;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 图片加载动画仓库
 *      url -- animator     1对1，且不可更换，始终保持一个url对应一个属性动画
 *      url -- ImageVIew    1对多，一个url可以对应0到多个ImageView。当ImageView对应的url不是新增而是替换关系的时候要做好同步
 *      一个ImageView同时只能加载一个url，一个url可以同时被多个ImageView加载
 */
class ImageLoadingAnimatorRepository {
    private static final ImageLoadingAnimatorRepository instance = new ImageLoadingAnimatorRepository();
    private Map<String, ImageViewLoader> urlAnimatorMap = new ConcurrentHashMap<>();
    private Map<ImageView, String> imageViewUrlMap = new ConcurrentHashMap<>();
    private Map<String, List<ImageView>> urlImageViewMap = new ConcurrentHashMap<>();

    private ImageLoadingAnimatorRepository() { }

    static ImageLoadingAnimatorRepository getInstance() {
        return instance;
    }

    /**
     * 更新ImageView对应的url映射关系，随着不断更新ImageView加载的目标url为改变
     */
    synchronized void updateImageUrlMapping(ImageView imageView, String url) {
        String oldUrl = imageViewUrlMap.put(imageView, url);
        boolean rebind = oldUrl != null && oldUrl.length() > 0;

        // 如果是重新绑定ImageView的url，则需要将该ImageView从之前的url关联中移除
        if (rebind) {
            List<ImageView> oldUrlImageViewList = getUrlImageViewList(oldUrl);

            Iterator<ImageView> iterator = oldUrlImageViewList.iterator();
            while (iterator.hasNext()) {
                ImageView iv = iterator.next();
                if (iv == imageView) {
                    iterator.remove();
                }
            }

            if (oldUrlImageViewList.size() == 0) {
                urlImageViewMap.remove(oldUrl);
            }
        }

        // 将ImageView添加到url对应的容器中
        List<ImageView> imageViewList = getUrlImageViewList(url);
        imageViewList.add(imageView);
    }

    private List<ImageView> getUrlImageViewList(String url) {
        List<ImageView> imageViewList = urlImageViewMap.get(url);

        if (imageViewList == null) {
            imageViewList = new ArrayList<>();
            urlImageViewMap.put(url, imageViewList);
        }

        return imageViewList;
    }

    /**
     * 开始加载指定的url动画
     */
    synchronized void startLoading(String url) {
        ImageViewLoader animator = urlAnimatorMap.get(url);
        if (animator == null) {
            animator = new ImageViewLoader(url);
            urlAnimatorMap.put(url, animator);
        }
        animator.start();
    }

    /**
     * 停止加载指定的url动画
     */
    synchronized void stopLoading(String url) {
        ImageViewLoader animator = urlAnimatorMap.get(url);
        if (animator != null) {
            animator.stop();
            releaseOnUrlStopLoading(url);
        }
    }

    synchronized void releaseOnUrlStopLoading(String url) {
        urlAnimatorMap.remove(url);

        Iterator<Map.Entry<ImageView, String>> iterator1 = imageViewUrlMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry<ImageView, String> next = iterator1.next();
            if (next.getValue().equals(url)) {
                iterator1.remove();
            }
        }

        Iterator<Map.Entry<String, List<ImageView>>> iterator2 = urlImageViewMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<String, List<ImageView>> next = iterator2.next();
            if (next.getKey().equals(url)) {
                iterator2.remove();
            }
        }
    }

    synchronized void releaseOnActivityDestoryed(Context context) {
        Iterator<Map.Entry<ImageView, String>> iterator1 = imageViewUrlMap.entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry<ImageView, String> next = iterator1.next();
            if (next.getKey().getContext() == context) {
                iterator1.remove();
            }
        }

        Iterator<Map.Entry<String, List<ImageView>>> iterator2 = urlImageViewMap.entrySet().iterator();
        while (iterator2.hasNext()) {
            Map.Entry<String, List<ImageView>> next = iterator2.next();
            List<ImageView> imageViewList = next.getValue();

            if (imageViewList != null && imageViewList.size() > 0) {
                Iterator<ImageView> iterator = imageViewList.iterator();
                while (iterator.hasNext()) {
                    ImageView iv = iterator.next();
                    if (iv.getContext() == context) {
                        iterator.remove();
                    }
                }
            }

            if (imageViewList != null && imageViewList.size() == 0) {
                iterator2.remove();
            }
        }
    }

    class ImageViewLoader implements ValueAnimator.AnimatorUpdateListener {
        private String url;
        private ValueAnimator animator;

        ImageViewLoader(String url) {
            this.url = url;
            animator = ValueAnimator.ofInt(0, 10000);
            animator.setDuration(1500);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(this);
        }

        @Override public void onAnimationUpdate(ValueAnimator animation) {
            List<ImageView> imageViewList = findImageListViewByUrl(url);
            if (imageViewList != null && imageViewList.size() > 0) {
                for (ImageView imageView : imageViewList) {
                    imageView.setImageLevel((Integer) animation.getAnimatedValue());
                }
            }
        }

        void start() {
            if (!animator.isStarted()) {
                animator.start();
            }
        }

        void stop() {
            animator.cancel();
        }
    }

    /**
     * 根据url找到当前加载这个url的ImageView
     */
    synchronized List<ImageView> findImageListViewByUrl(String url) {
        return urlImageViewMap.get(url);
    }



    /*
    ---------------------- 这些方法只是为了方便测试而使用的纯get方法 -----------------------
     */

    void clear() {
        urlAnimatorMap.clear();
        imageViewUrlMap.clear();
        urlImageViewMap.clear();
    }

    Map<String, ImageViewLoader> getUrlAnimatorMap() {
        return urlAnimatorMap;
    }

    Map<ImageView, String> getImageViewUrlMap() {
        return imageViewUrlMap;
    }

    Map<String, List<ImageView>> getUrlImageViewMap() {
        return urlImageViewMap;
    }
}
