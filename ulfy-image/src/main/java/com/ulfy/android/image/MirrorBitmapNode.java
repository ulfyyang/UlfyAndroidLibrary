package com.ulfy.android.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;

/**
 * 生成图像镜像，即倒影
 */
public final class MirrorBitmapNode extends BitmapProcessNode {
	private int distance;		// 倒影和图像之间的距离

	public MirrorBitmapNode() {
		this(5);
	}

	public MirrorBitmapNode(int distance) {
		if (distance < 0) {
			distance = 0;
		}
		this.distance = distance;
	}

	@Override protected Bitmap onProcessBitmap(Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}

		// 获得图片的长宽
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1); // 实现图片的反转
		// 创建反转后的图片Bitmap对象，图片高是原图的一半
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix, false);
		// 创建标准的Bitmap对象，宽和原图一致，高是原图的1.5倍
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bitmap, 0, 0, null); // 创建画布对象，将原图画于画布，起点是原点位置
		Paint paint = new Paint();
		paint.setColor(Color.TRANSPARENT);
		canvas.drawRect(0, height, width, height + distance, paint);
		// 将反转后的图片画到画布中
		canvas.drawBitmap(reflectionImage, 0, height + distance, null); 

		paint = new Paint();
		// 创建线性渐变LinearGradient对象
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0, bitmapWithReflection.getHeight() + distance, 0x70ffffff, 0x00ffffff, TileMode.MIRROR);
		// 绘制
		paint.setShader(shader); 
		// 倒影遮罩效果
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		// 画布画出反转图片大小区域，然后把渐变效果加到其中，就出现了图片的倒影效果
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + distance, paint);

		return bitmapWithReflection;
	}
}
