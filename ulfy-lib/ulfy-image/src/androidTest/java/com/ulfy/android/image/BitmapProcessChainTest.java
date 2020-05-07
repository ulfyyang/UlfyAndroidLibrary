package com.ulfy.android.image;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.novoda.espresso.ViewCreator;
import com.novoda.espresso.ViewTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 位图处理链测试
 *      链接链时要按照先整体处理再局部处理的顺序链接，比如先处理黑白、模糊在进行图片裁切
 */
@RunWith(AndroidJUnit4.class)
public class BitmapProcessChainTest extends BaseAndroidTest {
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

    @Before public void initContext() {
        ImageConfig.init((Application) InstrumentationRegistry.getContext().getApplicationContext());
    }

    @Before public void initBitmap() {
        bitmap = BitmapFactory.decodeResource(InstrumentationRegistry.getContext().getResources(), com.ulfy.android.image.test.R.drawable.meinv);;
    }

    /**
     * 测试有空节点的情况
     */
    @Test public void testWithoutBitmap() {
        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                BitmapProcessNode headNode = new BitmapProcessChain().connect(null).connect(new CircleBitmapNode()).build();
                bitmap = headNode.processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                BitmapProcessNode headNode = new BitmapProcessChain()
                        .connect(new CircleBitmapNode()).connect(null).build();
                bitmap = headNode.processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);

        initBitmap();
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                BitmapProcessNode headNode = new BitmapProcessChain()
                        .connect(new BlackWhiteBitmapNode()).connect(null).connect(new CircleBitmapNode()).build();
                bitmap = headNode.processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }

    /**
     * 测试常规使用
     */
    @Test public void testNormalUse() {
        activityRule.runOnMainSynchronously(new ViewTestRule.Runner<ImageView>() {
            @Override public void run(ImageView view) {
                BitmapProcessNode headNode = new BitmapProcessChain()
                        .connect(new BlackWhiteBitmapNode())
                        .connect(new BlurBitmapNode()).connect(new CircleBitmapNode())
                        .build();
                bitmap = headNode.processBitmap(bitmap);
                view.setImageBitmap(bitmap);
            }
        });
        screenshot(DELAY);
    }
}
