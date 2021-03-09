package com.ulfy.android.utils;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Field;

public final class ViewPagerUtils {

    public static void initViewPagerScrollSpeed(ViewPager viewPager) {
        try {
            FixedSpeedScroller scroller = new FixedSpeedScroller(viewPager.getContext(), new LinearOutSlowInInterpolator());
            Field field = Class.forName("androidx.viewpager.widget.ViewPager").getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(viewPager, scroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class FixedSpeedScroller extends Scroller {
        private int mDuration = 400;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override public void startScroll(int startX, int startY, int dx, int dy) {
            startScroll(startX,startY,dx,dy,mDuration);
        }

        @Override public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }
}
