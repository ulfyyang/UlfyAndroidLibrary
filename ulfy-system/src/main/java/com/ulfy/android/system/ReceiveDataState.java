package com.ulfy.android.system;

/**
 * 记录和接收数据相关的状态
 */
public final class ReceiveDataState {
	/** 初始状态 */
	public static final int INIT_STATE = 0;
	/** 接收数据 */
	public static final int RECEIVE_DATA = 1;
	/** 选择图库图片 */
	public static final int PICK_PICTURE = 2;
	/** 选择拍照图片 */
	public static final int TAKE_PICTURE = 3;
	/** 拍摄视频 */
	public static final int TAKE_VIDEO = 4;
	/** 对图片进行裁切 */
	public static final int CROP_PICTURE = 5;
	/** 选择媒体 **/
	public static final int PICK_MEDIA = 6;

	/*
	由于调用原生问题选择视频兼容性非常差，因此舍弃调用系统方法选择视频的功能
	为了更低的耦合性，这里放弃了将多媒体选择器集成到ActivityManager中的想法
	 */

	/** 记录状态的变量 */
	public int state = INIT_STATE;
}
