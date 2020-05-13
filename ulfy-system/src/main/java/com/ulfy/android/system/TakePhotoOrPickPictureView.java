package com.ulfy.android.system;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ulfy.android.dialog.DialogUtils;


/**
 * 选择图片时使用的界面
 */
class TakePhotoOrPickPictureView extends FrameLayout implements OnClickListener {
	private LinearLayout pickPictureLL;				// 选择图片
	private LinearLayout takePhotoLL;				// 拍照
	private int requestCode;						// 请求码
	private CropImageParam cropImageParam;			// 裁切图片的参数
	static final String ULFY_MAIN_TAKE_PHOTO_PICK_PICTURE_ID = "ULFY_MAIN_TAKE_PHOTO_PICK_PICTURE_ID";		// 默认进相机相册选图Dialog的ID

	TakePhotoOrPickPictureView(Context context, int requestCode, CropImageParam cropImageParam) {
		super(context);
		this.requestCode = requestCode;
		this.cropImageParam = cropImageParam;
		LayoutInflater.from(context).inflate(R.layout.ulfy_system_dialog_take_or_pick_picture, this);
		pickPictureLL = findViewById(R.id.pickPictureLL);
		takePhotoLL = findViewById(R.id.takePhotoLL);
		pickPictureLL.setOnClickListener(this);
		takePhotoLL.setOnClickListener(this);
	}

	@Override public void onClick(View v) {
		if (v == pickPictureLL) {
			ActivityUtils.pickPicture(requestCode, cropImageParam);
		} else if (v == takePhotoLL) {
			ActivityUtils.takePhoto(requestCode, cropImageParam);
		}
		DialogUtils.dismissDialog(ULFY_MAIN_TAKE_PHOTO_PICK_PICTURE_ID);
	}
}
