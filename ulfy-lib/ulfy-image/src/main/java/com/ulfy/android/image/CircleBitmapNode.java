package com.ulfy.android.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;

/**
 * 对原有图像进行处理，生成一个新的内切圆图像
 */
public final class CircleBitmapNode extends BitmapProcessNode {
    private int boderColor, borderWidth;        // <0 表示自动计算边框

    public CircleBitmapNode() {
        this(Color.TRANSPARENT, -1);
    }

    public CircleBitmapNode(int boderColor) {
        this(boderColor, -1);
    }

    public CircleBitmapNode(int boderColor, int borderWidth) {
        this.boderColor = boderColor;
        this.borderWidth = borderWidth;
    }

    @Override protected Bitmap onProcessBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        // 计算最小半径
        int radius = bitmap.getWidth() <= bitmap.getHeight() ? bitmap.getWidth() / 2 : bitmap.getHeight() / 2;

        // 绘制目标圆形图像（这时候的图像是一个黑色的圆形）
        Bitmap targetBitmap = Bitmap.createBitmap(radius * 2, radius * 2, Config.ARGB_8888);
        Canvas targetCanvas = new Canvas(targetBitmap);
        Paint targetPaint = new Paint();
        targetPaint.setAntiAlias(true);
        targetCanvas.drawCircle(radius, radius, radius, targetPaint);

        // 设置裁切画笔
        Paint clipPaint = new Paint();
        clipPaint.setAntiAlias(true);
        clipPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

        // 将原始图像画到目标图像上，留下原始图像的内容
        targetCanvas.drawBitmap(bitmap, -(bitmap.getWidth() / 2 - radius), -(bitmap.getHeight() / 2 - radius), clipPaint);

        // 绘制边框
        if (boderColor != Color.TRANSPARENT) {

            if (borderWidth < 0) {
                // 计算边框的宽度，默认为内切圆半径的30分之一，如果计算出来的边框宽度不够一个像素，则补够一个像素
                borderWidth = radius / 30;
                if (borderWidth < 1) {
                    borderWidth = 1;
                }
            }

            Paint borderPaint = new Paint();
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Style.STROKE);
            borderPaint.setStrokeWidth(borderWidth);
            borderPaint.setColor(boderColor);
            targetCanvas.drawCircle(radius, radius, radius - borderWidth / 2, borderPaint);
        }

        return targetBitmap;
    }
}
