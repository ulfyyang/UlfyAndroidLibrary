package com.ulfy.android.image;

import android.graphics.Bitmap;

/**
 * 位图处理点
 */
public abstract class BitmapProcessNode {
	private BitmapProcessNode nextNode;

	BitmapProcessNode connect(BitmapProcessNode nextNode) {
		this.nextNode = nextNode;
		return nextNode;
	}

	public final Bitmap processBitmap(Bitmap bitmap) {
		if(bitmap != null) {
			bitmap = onProcessBitmap(bitmap);
		}
		return nextNode == null ? bitmap : nextNode.processBitmap(bitmap);
	}

	protected abstract Bitmap onProcessBitmap(Bitmap bitmap);
}