package com.ulfy.android.utils;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public final class DrawableUtils {

    /**
     * 最终生成GradientDrawable，对应的时xml形状的Shape
     */
    public static GradientBuilder gradientBuilder() {
        return new GradientBuilder();
    }

    public static class GradientBuilder {
        private GradientDrawable gradientDrawable = new GradientDrawable();

        public GradientBuilder shapeRectangle() {
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            return this;
        }
        public GradientBuilder shapeOval() {
            gradientDrawable.setShape(GradientDrawable.OVAL);
            return this;
        }
        public GradientBuilder shapeLine() {
            gradientDrawable.setShape(GradientDrawable.LINE);
            return this;
        }
        public GradientBuilder shapeRing() {
            gradientDrawable.setShape(GradientDrawable.RING);
            return this;
        }
        public GradientBuilder color(String color) {
            return color(Color.parseColor(color));
        }
        public GradientBuilder color(int color) {
            gradientDrawable.setColor(color);
            return this;
        }
        public GradientBuilder sizeDp(float width, float height) {
            return sizePx(UiUtils.dp2px(width), UiUtils.dp2px(height));
        }
        public GradientBuilder sizePx(int width, int height) {
            gradientDrawable.setSize(width, height);
            return this;
        }
        public GradientBuilder strokeDp(float width, String color) {
            return strokePx(UiUtils.dp2px(width), Color.parseColor(color));
        }
        public GradientBuilder strokeDp(float width, int color) {
            return strokePx(UiUtils.dp2px(width), color);
        }
        public GradientBuilder strokePx(int width, String color) {
            return strokePx(width, Color.parseColor(color));
        }
        public GradientBuilder strokePx(int width, int color) {
            gradientDrawable.setStroke(width, color);
            return this;
        }
        public GradientBuilder cornerDp(float corner) {
            return cornerDp(corner, corner, corner, corner);
        }
        public GradientBuilder cornerDp(float left, float top, float right, float bottom) {
            return cornerPx(UiUtils.dp2px(left), UiUtils.dp2px(top), UiUtils.dp2px(right), UiUtils.dp2px(bottom));
        }
        public GradientBuilder cornerPx(float corner) {
            return cornerPx(corner, corner, corner, corner);
        }
        public GradientBuilder cornerPx(float left, float top, float right, float bottom) {
            gradientDrawable.setCornerRadii(new float[]{
                    left, left, top, top, right, right, bottom, bottom
            });
            return this;
        }
        public GradientDrawable build() {
            return gradientDrawable;
        }
    }
}
