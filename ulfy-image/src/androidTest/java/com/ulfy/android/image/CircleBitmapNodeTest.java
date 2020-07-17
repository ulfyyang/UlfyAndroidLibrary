package com.ulfy.android.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.novoda.espresso.ViewCreator;
import com.novoda.espresso.ViewTestRule;
import com.ulfy.android.image.test.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 位图内切圆测试
 */
@RunWith(AndroidJUnit4.class)
public class CircleBitmapNodeTest extends BaseAndroidTest {
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
        bitmap = BitmapFactory.decodeResource(InstrumentationRegistry.getContext().getResources(), R.drawable.meinv);;
    }

    /**
     * 测试没有输入源的情况
     */
    @Test public void testWithoutBitmap() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode().processBitmap(null);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }

    /**
     * 测试常规内切圆
     */
    @Test public void testNormalUse() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode().processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }

    /**
     * 测试默认边框内切圆
     */
    @Test public void testDefaultBorder() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode(Color.RED).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }

    /**
     * 测试指定边框内切圆（胖边框）
     */
    @Test public void testFatBorder() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode(Color.RED, 20).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }

    /**
     * 测试指定边框内切圆（瘦边框）
     */
    @Test public void testThinBorder() {
        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode(Color.RED, 1).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode(Color.RED, 2).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode(Color.RED, 3).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode(Color.RED, 4).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                bitmap = new CircleBitmapNode(Color.RED, 5).processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }
}