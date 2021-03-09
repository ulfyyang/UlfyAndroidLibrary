package com.ulfy.android.image;

import android.app.Application;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ImageView;

import com.novoda.espresso.ViewTestRule;
import com.ulfy.android.image.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * 对Glide包装的测试
 *      该测试加载的是真实的网络图片，因此需要联网的设备
 */
@RunWith(AndroidJUnit4.class)
public class GlideWrapperTest extends BaseAndroidTest {
    public static final int DELAY = 10000;
    public List<String> imageUrlList;
    public ImageView imageView1;
    public ImageView imageView2;
    public ImageView imageView3;
    public ImageView imageView4;

    @Rule public ViewTestRule<View> activityRule = new ViewTestRule<>(R.layout.view_image_watcher_wrapper);

    @Before public void hideTitle() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                activityRule.getActivity().getActionBar().hide();
            }
        });
    }

    @Before public void initImageUrlList() {
        imageUrlList = new ArrayList<>();
        imageUrlList.add("https://img.gq.com.tw/_rs/645/userfiles/sm/sm1024_images_A1/40721/2019092061159601.jpg");
        imageUrlList.add("http://www.sxzf.org.cn/wp-content/uploads/2019/04/201904011554159980.jpg");
        imageUrlList.add("http://n.sinaimg.cn/fashion/24_img/upload/32e29b47/560/w1080h1080/20190902/b8cc-ieaiqii0121848.jpg");
        imageUrlList.add("http://pic1.win4000.com//pic/2/e2/df130a592d.jpg");
    }

    @Before public void initContext() {
        ImageConfig.Config.imageLoadingAnimator = false;
        ImageConfig.init((Application) InstrumentationRegistry.getContext().getApplicationContext());
        GlideWrapper.getInstance().clearImageDiskCache();
    }

    @Before public void findImageViewById() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                imageView1 = view.findViewById(R.id.image1IV);
                imageView2 = view.findViewById(R.id.image2IV);
                imageView3 = view.findViewById(R.id.image3IV);
                imageView4 = view.findViewById(R.id.image4IV);
            }
        });
    }

    /**
     * 单个ImageView加载单个url测试
     */
    @Test public void testOneUrlOneImageView() {
        loadImageViewOneUrlOneImageView();
        delay(DELAY);
    }

    /**
     * 多个ImageView加载同一个url测试
     */
    @Test public void testOneUrlMoreImageView() {
        loadImageViewOneUrlMoreImageView();
        delay(DELAY);
    }

    /**
     * 单个ImageView加载单个url测试（占位图动画）
     */
    @Test public void testOneUrlOneImageViewWithAnimator() {
        ImageConfig.Config.imageLoadingAnimator = true;
        loadImageViewOneUrlOneImageView();
        delay(DELAY);
        assertEquals(0, ImageLoadingAnimatorRepository.getInstance().getUrlAnimatorMap().size());
    }

    /**
     * 多个ImageView加载同一个url测试（占位图动画）
     */
    @Test public void testOneUrlMoreImageViewWithAnimator() {
        ImageConfig.Config.imageLoadingAnimator = true;
        loadImageViewOneUrlMoreImageView();
        delay(DELAY);
        assertEquals(0, ImageLoadingAnimatorRepository.getInstance().getUrlAnimatorMap().size());
    }

    /**
     * 图片转换加载测试
     */
    @Test public void testImageTransform() {
        loadImageViewWithTransform();
        delay(DELAY);
    }

    /**
     * 在Activity被销毁的情况下加载测试
     */
    @Test public void testLoadingAfterActivityDestroy() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                activityRule.getActivity().finish();
            }
        });
        loadImageViewOneUrlOneImageView();
    }

    /**
     * 一个ImageView加载一个url
     */
    private void loadImageViewOneUrlOneImageView() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                GlideWrapper.getInstance().loadImage(imageUrlList.get(0), R.drawable.placeholder_loading_animator, 0, imageView1, null);
                GlideWrapper.getInstance().loadImage(imageUrlList.get(1), R.drawable.placeholder_loading_animator, 0, imageView2, null);
                GlideWrapper.getInstance().loadImage(imageUrlList.get(2), R.drawable.placeholder_loading_animator, 0, imageView3, null);
                GlideWrapper.getInstance().loadImage(imageUrlList.get(3), R.drawable.placeholder_loading_animator, 0, imageView4, null);
            }
        });
    }

    /**
     * 多个ImageView加载一个url
     */
    private void loadImageViewOneUrlMoreImageView() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                GlideWrapper.getInstance().loadImage(imageUrlList.get(0), R.drawable.placeholder_loading_animator, 0, imageView1, null);
                GlideWrapper.getInstance().loadImage(imageUrlList.get(0), R.drawable.placeholder_loading_animator, 0, imageView2, null);
                GlideWrapper.getInstance().loadImage(imageUrlList.get(0), R.drawable.placeholder_loading_animator, 0, imageView3, null);
                GlideWrapper.getInstance().loadImage(imageUrlList.get(0), R.drawable.placeholder_loading_animator, 0, imageView4, null);
            }
        });
    }

    /**
     * 图片转换方式加载图片
     */
    private void loadImageViewWithTransform() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                GlideWrapper.getInstance().loadImage(imageUrlList.get(0), R.drawable.placeholder_loading_animator, 0, imageView1, new CircleBitmapNode());
                GlideWrapper.getInstance().loadImage(imageUrlList.get(1), R.drawable.placeholder_loading_animator, 0, imageView2, new BlackWhiteBitmapNode());
                GlideWrapper.getInstance().loadImage(imageUrlList.get(2), R.drawable.placeholder_loading_animator, 0, imageView3, new BlurBitmapNode());
                GlideWrapper.getInstance().loadImage(imageUrlList.get(3), R.drawable.placeholder_loading_animator, 0, imageView4, new RectBitmapNode(50));
            }
        });
    }
}
