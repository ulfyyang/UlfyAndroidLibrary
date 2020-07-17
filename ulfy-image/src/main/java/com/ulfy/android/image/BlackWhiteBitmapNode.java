package com.ulfy.android.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * 处理位图为黑白效果的位图
 */
public final class BlackWhiteBitmapNode extends BitmapProcessNode {

	@Override protected Bitmap onProcessBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

		int width = bitmap.getWidth();
        int height = bitmap.getHeight();
 
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
 
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
 
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
 
                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }

        Bitmap newBmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        return newBmp;
	}

}
