package com.ulfy.android.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.novoda.espresso.ViewCreator;
import com.novoda.espresso.ViewTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 位图倒影测试
 */
@RunWith(AndroidJUnit4.class)
public class MirrorBitmapNodeTest extends BaseAndroidTest {
    public static final int DELAY = 200;
    public Bitmap bitmap;

    @Rule public ViewTestRule<ImageView> activityRule = new ViewTestRule<>(new ViewCreator<ImageView>() {
        @Override public ImageView createView(Context context, ViewGroup parentView) {
            ImageView view = new ImageView(context);
            matchParent(view);
            return view;
        }
    });

    @Before public void hideTitle() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                activityRule.getActivity().getActionBar().hide();
            }
        });
    }

    @Before public void initBitmap() {
        bitmap = BitmapFactory.decodeResource(InstrumentationRegistry.getContext().getResources(), com.ulfy.android.image.test.R.drawable.meinv);;
    }

    /**
     * 测试没有输入源的情况
     */
    @Test public void testWithoutBitmap() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new MirrorBitmapNode().processBitmap(null);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }

    /**
     * 测试正常使用
     */
    @Test public void testNormalUse() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new MirrorBitmapNode().processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }

    /**
     * 测试不同的间隙
     */
    @Test public void testDifferentDistance() {
        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new MirrorBitmapNode(-10).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new MirrorBitmapNode(0).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new MirrorBitmapNode(10).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new MirrorBitmapNode(15).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new MirrorBitmapNode(20).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }
}
