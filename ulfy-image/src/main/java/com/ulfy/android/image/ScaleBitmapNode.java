package com.ulfy.android.image;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * 位图缩放处理
 */
public final class ScaleBitmapNode extends BitmapProcessNode {
    private int targetWidth, targetHeight;

    public ScaleBitmapNode(int targetWidth, int targetHeight) {
        if (targetWidth < 1) {
            targetWidth = 1;
        }
        if (targetHeight < 1) {
            targetHeight = 1;
        }
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    @Override protected Bitmap onProcessBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);

        Matrix matrix = new Matrix();
        matrix.setScale(targetWidth * 1.0f / bitmap.getWidth(), targetHeight * 1.0f / bitmap.getHeight());
        canvas.drawBitmap(bitmap, matrix, null);

        return targetBitmap;
    }
}
