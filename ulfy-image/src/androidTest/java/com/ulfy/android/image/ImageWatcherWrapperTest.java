package com.ulfy.android.image;

import android.app.Application;
import androidx.test.InstrumentationRegistry;
import androidx.test.espresso.action.ViewActions;
import androidx.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.novoda.espresso.ViewTestRule;
import com.ulfy.android.image.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.doubleClick;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 大图预览测试
 *      该测试设计到动画的展示，因此具体的效果还需要人眼跟踪测试的自动运行
 */
@RunWith(AndroidJUnit4.class)
public class ImageWatcherWrapperTest extends BaseAndroidTest {
    public static final int DELAY = 1000;
    public List<String> imageUrlList;

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
        imageUrlList.add("file:///android_asset/meinv1.jpg");
        imageUrlList.add("file:///android_asset/meinv2.jpg");
        imageUrlList.add("file:///android_asset/meinv3.jpg");
        imageUrlList.add("file:///android_asset/meinv4.jpg");
    }

    @Before public void initContext() {
        ImageConfig.init((Application) InstrumentationRegistry.getContext().getApplicationContext());
    }

    @Before public void loadAssertsImages() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                Glide.with(view).load(imageUrlList.get(0)).into((ImageView) view.findViewById(R.id.image1IV));
                Glide.with(view).load(imageUrlList.get(1)).into((ImageView) view.findViewById(R.id.image2IV));
                Glide.with(view).load(imageUrlList.get(2)).into((ImageView) view.findViewById(R.id.image3IV));
                Glide.with(view).load(imageUrlList.get(3)).into((ImageView) view.findViewById(R.id.image4IV));
            }
        });
    }

    /**
     * 基本使用测试（不跟踪ImageView）
     */
    @Test public void testContextlUse() {
        lunchPreviewFromContext(0);
        swipLeftThenSwipRight();
        singleClick();
    }

    /**
     * 基本使用测试（跟踪ImageView）
     */
    @Test public void testViewGroupUse() {
        lunchPreviewFromGroup(0);
        swipLeftThenSwipRight();
        singleClick();
    }

    /**
     * 双击放大测试
     */
    @Test public void testDoubleClickPreview() {
        lunchPreviewFromGroup(0);
        doubleClickForPreview();
    }

    /**
     * 从不同位置启动
     */
    @Test public void testPreviewPosition() {
        for (int i = 0; i < 4; i++) {
            lunchPreviewFromGroup(i);
            singleClick();
        }
    }

    /**
     * 测试点击返回关闭预览
     */
    @Test public void testOnBackPress() {
        lunchPreviewFromGroup(0);
        backPress();
    }

    /**
     * 测试Activity销毁后的资源释放
     */
    @Test public void testReleaseWhenActivityDestroy() {
        assertEquals(0, ImageWatcherWrapper.getInstance().getImageWatcherHelperSize());

        lunchPreviewFromGroup(0);
        assertEquals(1, ImageWatcherWrapper.getInstance().getImageWatcherHelperSize());

        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                activityRule.getActivity().finish();
            }
        });

        delay(DELAY);
        assertEquals(0, ImageWatcherWrapper.getInstance().getImageWatcherHelperSize());
    }

    /**
     * 打开大图预览界面（不跟踪ImageView）
     */
    private void lunchPreviewFromContext(int showPosition) {
        delay(DELAY);
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                ImageWatcherWrapper.getInstance().preview(view.findViewById(R.id.contentLL), imageUrlList, showPosition);
            }
        });
        delay(DELAY);
    }

    /**
     * 打开大图预览界面（跟踪ImageView）
     */
    private void lunchPreviewFromGroup(int showPosition) {
        delay(DELAY);
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                ImageWatcherWrapper.getInstance().preview(view.findViewById(R.id.contentLL), imageUrlList, showPosition);
            }
        });
        delay(DELAY);
    }

    /**
     * 依次向左滑动，到头后，依次向右滑动
     */
    private void swipLeftThenSwipRight() {
        for (int i = 0; i < 5; i++) {
            onView(isRoot()).perform(swipeLeft());delay(DELAY);
        }
        for (int i = 0; i < 5; i++) {
            onView(isRoot()).perform(ViewActions.swipeRight());delay(DELAY);
        }
    }

    /**
     * 单击关闭预览
     */
    private void singleClick() {
        onView(isRoot()).perform(click());delay(DELAY);
    }

    /**
     * 双击大图预览
     */
    private void doubleClickForPreview() {
        for (int i = 0; i < 4; i++) {
            onView(isRoot()).perform(doubleClick());delay(DELAY);
        }
    }

    /**
     * 点击返回
     */
    private void backPress() {
        final boolean[] handle = {false};
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<View>() {
            @Override public void run(View view) {
                handle[0] = ImageWatcherWrapper.getInstance().handlePreviewBackPressed(activityRule.getActivity());
            }
        });
        delay(DELAY);
        assertTrue(handle[0]);
    }
}
