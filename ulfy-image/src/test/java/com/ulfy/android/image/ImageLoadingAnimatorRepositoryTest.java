package com.ulfy.android.image;

import android.app.Activity;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 加载图片占位图动画仓库测试
 */
public class ImageLoadingAnimatorRepositoryTest {
    public ImageLoadingAnimatorRepository repository;
    public Map<String, ImageLoadingAnimatorRepository.ImageViewLoader> urlAnimatorMap;
    public Map<ImageView, String> imageViewUrlMap;
    public Map<String, List<ImageView>> urlImageViewMap;

    public Activity activity;
    public ImageView imageView1;
    public ImageView imageView2;

    public String beforeUrl = "beforeUrl";
    public String afterUrl = "afterUrl";

    @Before public void init() {
        repository = ImageLoadingAnimatorRepository.getInstance();
        urlAnimatorMap = repository.getUrlAnimatorMap();
        imageViewUrlMap = repository.getImageViewUrlMap();
        urlImageViewMap = repository.getUrlImageViewMap();

        repository.clear();

        activity = mock(Activity.class);
        imageView1 = mock(ImageView.class);
        imageView2 = mock(ImageView.class);

        when(imageView1.getContext()).thenReturn(activity);
        when(imageView2.getContext()).thenReturn(activity);
    }

    /**
     * ImageView加载url的过程中切换为了其它的url
     */
    @Test public void testOneImageViewOneUrl() {
        repository.updateImageUrlMapping(imageView1, beforeUrl);

        assertEquals(1, imageViewUrlMap.size());
        assertEquals(beforeUrl, imageViewUrlMap.get(imageView1));
        assertEquals(1, urlImageViewMap.size());
        assertEquals(1, urlImageViewMap.get(beforeUrl).size());
        assertEquals(imageView1, urlImageViewMap.get(beforeUrl).get(0));

        repository.updateImageUrlMapping(imageView1, afterUrl);

        assertEquals(1, imageViewUrlMap.size());
        assertEquals(afterUrl, imageViewUrlMap.get(imageView1));
        assertEquals(1, urlImageViewMap.size());
        assertEquals(1, urlImageViewMap.get(afterUrl).size());
        assertEquals(imageView1, urlImageViewMap.get(afterUrl).get(0));
    }

    /**
     * 多个ImageView加载同一个url
     */
    @Test public void testOneUrlMoreImageView() {
        repository.updateImageUrlMapping(imageView1, beforeUrl);
        repository.updateImageUrlMapping(imageView2, beforeUrl);

        assertEquals(2, imageViewUrlMap.size());
        assertEquals(beforeUrl, imageViewUrlMap.get(imageView1));
        assertEquals(beforeUrl, imageViewUrlMap.get(imageView2));
        assertEquals(1, urlImageViewMap.size());
        assertEquals(2, urlImageViewMap.get(beforeUrl).size());
        assertEquals(imageView1, urlImageViewMap.get(beforeUrl).get(0));
        assertEquals(imageView2, urlImageViewMap.get(beforeUrl).get(1));
    }

    /**
     * 测试当url加载完毕后的资源释放
     */
    @Test public void testReleaseOnStopLoading() {
        repository.updateImageUrlMapping(imageView1, beforeUrl);
        repository.updateImageUrlMapping(imageView2, afterUrl);
        repository.releaseOnUrlStopLoading(beforeUrl);

        assertEquals(1, imageViewUrlMap.size());
        assertEquals(afterUrl, imageViewUrlMap.get(imageView2));
        assertEquals(1, urlImageViewMap.size());
        assertEquals(imageView2, urlImageViewMap.get(afterUrl).get(0));
    }

    /**
     * 测试Activity销毁时资源释放
     */
    @Test public void testReleaseOnActivityDestroy() {
        repository.updateImageUrlMapping(imageView1, beforeUrl);
        repository.updateImageUrlMapping(imageView2, afterUrl);
        repository.releaseOnActivityDestoryed(activity);

        assertEquals(0, imageViewUrlMap.size());
        assertEquals(0, urlImageViewMap.size());
    }
}