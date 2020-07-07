package com.ulfy.android.system;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.dialog.NormalDialog;
import com.ulfy.android.system.event.OnCropPictureEvent;
import com.ulfy.android.system.event.OnPickMediaEvent;
import com.ulfy.android.system.event.OnPickPictureEvent;
import com.ulfy.android.system.event.OnReceiveDataEvent;
import com.ulfy.android.system.event.OnTakePhotoEvent;
import com.ulfy.android.system.event.OnTakePhotoOrPickPictureEvent;
import com.ulfy.android.system.media_picker.MediaEntity;
import com.ulfy.android.system.media_picker.MediaPickerActivity;
import com.ulfy.android.ui_injection.InjectUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Activity辅助类，该类中包含一些关于Activity的常用操作。
 * 目前有关 Activity 的操作均不支持 带有其它启动模式的 Activity 启动方式，目前只支持 standard 方式启动的 Activity
 * 如果使用指定了其它的启动模式，则调用相关的 Activity 操作方法会出现意外的错误情况
 */
public final class ActivityUtils {
	private static File OUTPUT_FILE = null;         		// 输出图片
	private static CropImageParam cropImageParam;   		// 裁切图片的参数

	///////////////////////////////////////////////////////////////////////////
	// 生命周期托管
	///////////////////////////////////////////////////////////////////////////

	public static void onCreated(Activity activity) {
		InjectUtils.processLayoutFile(activity);
		InjectUtils.processViewById(activity);
		InjectUtils.processViewClick(activity);
		ActivityRepository.getInstance().notifyActivityCreated(activity);
		BusUtils.register(activity, activity);
		BusUtils.register(activity);
	}

	public static void onStart(final Activity activity) {
		ActivityInfo activityInfo = ActivityRepository.getInstance().findInfoByActivity(activity);
		if (activityInfo != null && activityInfo.enableShake) {
			activityInfo.registerShake();
		}
	}

	public static void onStop(Activity activity) {
		ActivityInfo activityInfo = ActivityRepository.getInstance().findInfoByActivity(activity);
		if (activityInfo != null && activityInfo.enableShake) {
			activityInfo.unRegisterShake();
		}
	}

	public static void onDestroy(Activity activity) {
		BusUtils.unregister(activity, activity);
		BusUtils.unregister(activity);
		ActivityRepository.getInstance().notifyActivityDestroyed(activity);
	}

	public static void onActivityResultForReceiveData(Activity activity, int requestCode, int resultCode, Intent data) {
		ReceiveDataState receiveDataState = ActivityRepository.getInstance().findInfoByActivity(activity).receiveDataState;
		if (receiveDataState == null || resultCode != Activity.RESULT_OK) {
			initActivityState(receiveDataState);
		} else if (receiveDataState.state == ReceiveDataState.RECEIVE_DATA) {
			processReceiveDataResult(receiveDataState, activity, requestCode, data);
		} else if (receiveDataState.state == ReceiveDataState.PICK_PICTURE) {
			processReceivePictureThenCropIfCan(receiveDataState, activity, requestCode, data);
		} else if (receiveDataState.state == ReceiveDataState.TAKE_PICTURE) {
			processTakePictureThenCropIfCan(receiveDataState, activity, requestCode, data);
		} else if (receiveDataState.state == ReceiveDataState.CROP_PICTURE) {
			processCropPicture(receiveDataState, activity, requestCode);
		} else if (receiveDataState.state == ReceiveDataState.PICK_MEDIA) {
			processPickMedia(receiveDataState, activity, requestCode, data);
		}
	}

	private static void processReceiveDataResult(ReceiveDataState receiveDataState, Activity activity, int requestCode, Intent data) {
		BusUtils.post(activity, new OnReceiveDataEvent(requestCode, data == null ? null : data.getExtras()));
		initActivityState(receiveDataState);
	}

	private static void processReceivePictureThenCropIfCan(ReceiveDataState receiveDataState, Activity activity, int requestCode, Intent data) {
		File file = data == null ? null : AppUtils.getFileFromUri(data.getData());
		if (cropImageParam == null) {
			BusUtils.post(activity, new OnPickPictureEvent(requestCode, file));
			BusUtils.post(activity, new OnTakePhotoOrPickPictureEvent(requestCode, file));
			initActivityState(receiveDataState);
		} else {
			cropImage(requestCode, file, cropImageParam);
		}
	}

	private static void processTakePictureThenCropIfCan(ReceiveDataState receiveDataState, Activity target, int requestCode, Intent data) {
		if (cropImageParam == null) {
			BusUtils.post(target, new OnTakePhotoEvent(requestCode, OUTPUT_FILE));
			BusUtils.post(target, new OnTakePhotoOrPickPictureEvent(requestCode, OUTPUT_FILE));
			initActivityState(receiveDataState);
		} else {
			cropImage(requestCode, OUTPUT_FILE, cropImageParam);
		}
	}

	private static void processCropPicture(ReceiveDataState receiveDataState, Activity target, int requestCode) {
		BusUtils.post(target, new OnCropPictureEvent(requestCode, OUTPUT_FILE));
		initActivityState(receiveDataState);
	}

	private static void processPickMedia(ReceiveDataState receiveDataState, Activity target, int request, Intent data) {
		int search = data == null ? 0 : data.getExtras().getInt("search");
		int max = data == null ? 0 : data.getExtras().getInt("max");
		List<MediaEntity> entities = data == null ? null : (List<MediaEntity>) data.getExtras().getSerializable("entities");
		BusUtils.post(target, new OnPickMediaEvent(request, search, max, entities));
		initActivityState(receiveDataState);
	}

	private static void initActivityState(ReceiveDataState receiveDataState) {
		receiveDataState.state = ReceiveDataState.INIT_STATE;
		cropImageParam = null;
	}

	///////////////////////////////////////////////////////////////////////////
	// 启动Activity相关方法
	///////////////////////////////////////////////////////////////////////////

	public static void startActivity(Class<? extends Activity> activityClazz) {
		startActivity(activityClazz, -1, null, StartMode.NORMAL_START);
	}

	public static void startActivity(Class<? extends Activity> activityClazz, int startMode) {
		startActivity(activityClazz, -1, null, startMode);
	}

	public static void startActivity(Class<? extends Activity> activityClazz, String key, Serializable value) {
		Bundle sendData = new Bundle();
		sendData.putSerializable(key, value);
		startActivity(activityClazz, sendData);
	}

	public static void startActivity(Class<? extends Activity> activityClazz, Bundle sendData) {
		startActivity(activityClazz, -1, sendData, StartMode.NORMAL_START);
	}

	public static void startActivity(Class<? extends Activity> activityClazz, int requestCode, Bundle sendData) {
		startActivity(activityClazz, requestCode, sendData, StartMode.NORMAL_START);
	}

	public static void startActivity(Class<? extends Activity> activityClazz, int requestCode, Bundle sendData, int startMode) {
		ActivityRepository repository = ActivityRepository.getInstance();
		// 记录当前 Activity 和当前顶部 Activity 的位置
		int activityPosition = repository.findActivityPosition(activityClazz);
		int topPosition = repository.size() - 1;
		// 记录当前的 Activity
		ActivityInfo activityInfo = repository.getTopActivityInfo();
		if (activityInfo == null) {
			return;
		}
		Activity topActivity = activityInfo.activity;
		ReceiveDataState state = activityInfo.receiveDataState;
		// 有可能用户在跳转之前切到后台运行，或其它情况导致的内存被回收，这里做个安全判断
		if (topActivity == null) {
			return;
		}
		// 构造意图对象
		Intent intent = new Intent(topActivity, activityClazz);
		if (sendData != null) {
			intent.putExtras(sendData);
		}
		// 根据请求码启动 Activity
		if (requestCode <= 0) {
			topActivity.startActivity(intent);
		} else {
			state.state = ReceiveDataState.RECEIVE_DATA;
			topActivity.startActivityForResult(intent, requestCode);
		}
		// 根据启动模式处理 Activity 仓储
		switch (startMode) {
			case StartMode.RE_START:
				if (activityPosition != -1) {
					repository.removeActivity(activityPosition);
				}
				break;
			case StartMode.CLEARTOP_START:
				if (activityPosition != -1) {
					repository.removeActivity(activityPosition, topPosition);
				}
				break;
			case StartMode.CLEARALL_START:
				repository.removeActivity(0, topPosition);
				break;
			// 正常模式和无法识别的模式不需要处理
			case StartMode.NORMAL_START:
			default:
				break;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// 关闭Activity方法
	///////////////////////////////////////////////////////////////////////////

	public static void closeTopActivity() {
		ActivityRepository.getInstance().removeTopActivity();
	}

	public static void closeAllActivity() {
		ActivityRepository.getInstance().removeAllActivity();
	}

	public static void closeActivity(Class<? extends Activity> activityClazz) {
		closeActivity(activityClazz, CloseMode.CLOSE_NORMAL);
	}

	public static void closeActivity(Class<? extends Activity> activityClazz, int closeMode) {
		ActivityRepository repository = ActivityRepository.getInstance();
		// 记录当前 Activity 和当前顶部 Activity 的位置
		int activityPosition = repository.findActivityPosition(activityClazz);
		int topPosition = repository.size() - 1;
		// 如果仓储中不存在指定的 Activity 则不进行其它的删除操作
		if (activityPosition == -1) {
			return;
		}
		// 根据关闭模式处理 Activity 仓储
		switch (closeMode) {
			case CloseMode.CLOSE_TOP:
				repository.removeActivity(activityPosition, topPosition);
				break;
			// 正常模式和无法识别模式正常关闭
			case CloseMode.CLOSE_NORMAL:
			default:
				repository.removeActivity(activityPosition);
				break;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// 其它Activity相关操作
	///////////////////////////////////////////////////////////////////////////

	public static boolean exsitActivity(Class<? extends Activity> clazz) {
		return ActivityRepository.getInstance().findActivityPosition(clazz) != -1;
	}

	public static Activity getTopActivity() {
		return ActivityRepository.getInstance().getTopActivityInfo().activity;
	}

	public static void backToActivity(Class<? extends Activity> clazz) {
		ActivityRepository repository = ActivityRepository.getInstance();
		// 记录当前 Activity 和当前顶部 Activity 的位置
		int activityPosition = repository.findActivityPosition(clazz);
		int topPosition = repository.size() - 1;
		// 如果仓储中不存在指定的 Activity 则不进行其它的删除操作
		if (activityPosition == -1) {
			return;
		}
		repository.removeActivity(activityPosition + 1, topPosition);
	}

	public static Bundle getData() {
		Intent intent = getTopActivity().getIntent();
		return intent == null ? null : intent.getExtras();
	}

	public static Serializable getSerializableData(String key) {
		Intent intent = getTopActivity().getIntent();
		return intent == null ? null : intent.getSerializableExtra(key);
	}

	public static void returnData(String key, Serializable value) {
		Bundle data = new Bundle();
		data.putSerializable(key, value);
		returnData(data);
	}

	public static void returnData(Bundle data) {
		Intent intent = new Intent();
		intent.putExtras(data);
		Activity activity = getTopActivity();
		activity.setResult(Activity.RESULT_OK, intent);
		closeTopActivity();
	}

	///////////////////////////////////////////////////////////////////////////
	// 系统交互
	///////////////////////////////////////////////////////////////////////////

    /**
     * 弹出选择图片或拍照窗口
     */
    public static void showTakePhotoOrPickPictureDialog(int requestCode) {
        showTakePhotoOrPickPictureDialog(requestCode, null);
    }

    /**
     * 弹出一个选择图片的弹窗
     */
    public static void showTakePhotoOrPickPictureDialog(int requestCode, CropImageParam cropImageParam) {
        TakePhotoOrPickPictureView contentView = new TakePhotoOrPickPictureView(ActivityUtils.getTopActivity(), requestCode, cropImageParam);
        new NormalDialog.Builder(ActivityUtils.getTopActivity(), contentView)
                .setDialogId(TakePhotoOrPickPictureView.ULFY_MAIN_TAKE_PHOTO_PICK_PICTURE_ID)
                .build().show();
    }

	/**
	 * 选择图片
	 */
	public static void pickPicture(int requestCode) {
		pickPicture(requestCode, null);
	}

	/**
	 * 选择图片并裁切
	 */
	public static void pickPicture(final int requestCode, final CropImageParam param) {
		AppUtils.requestPermission(new AppUtils.OnRequestPermissionListener() {
			public void onSuccess() {
				if (requestCode <= 0) {
					throw new IllegalArgumentException("cannot pick picture, request code must larger than zero");
				}
				// 获取顶部Activity的基本信息
				ActivityInfo activityInfo = ActivityRepository.getInstance().getTopActivityInfo();
				Activity topActivity = activityInfo.activity;
				ReceiveDataState state = activityInfo.receiveDataState;
				// 构造意图
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_PICK);
				intent.setType("image/*");
				// 更全面的选择器
//				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//				intent.addCategory(Intent.CATEGORY_OPENABLE);
//				intent.setType("image/*");
				// 发送意图
				try {
					state.state = ReceiveDataState.PICK_PICTURE;
					topActivity.startActivityForResult(intent, requestCode);
					cropImageParam = param;
				} catch (ActivityNotFoundException e) {
					Toast.makeText(SystemConfig.context, "device not support pick picture", Toast.LENGTH_LONG).show();
				}
			}
			public void onFail() {
				Toast.makeText(SystemConfig.context, "获取读取相册授权失败", Toast.LENGTH_LONG).show();
			}
		}, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	/**
	 * 拍照
	 */
	public static void takePhoto(int requestCode) {
		takePhoto(requestCode, null);
	}

	/**
	 * 拍照并裁
	 */
	public static void takePhoto(final int requestCode, final CropImageParam param) {
		AppUtils.requestPermission(new AppUtils.OnRequestPermissionListener() {
			public void onSuccess() {
				if (requestCode <= 0) {
					throw new IllegalArgumentException("cannot tack photo, request code must larger than zero");
				}
				// 获取顶部Activity的基本信息
				ActivityInfo activityInfo = ActivityRepository.getInstance().getTopActivityInfo();
				Activity topActivity = activityInfo.activity;
				ReceiveDataState state = activityInfo.receiveDataState;
				// 构造意图
				Intent intent = new Intent();
				intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				OUTPUT_FILE = new File(SystemConfig.getTakePhotoPictureCacheDir(), UUID.randomUUID().toString() + ".jpeg");
				Uri outputUri = AppUtils.getUriFromFile(OUTPUT_FILE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
				grantUriPermission(intent, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				// 发送意图
				try {
					state.state = ReceiveDataState.TAKE_PICTURE;
					topActivity.startActivityForResult(intent, requestCode);
					cropImageParam = param;
				} catch (ActivityNotFoundException e) {
					Toast.makeText(SystemConfig.context, "device not support tack photo", Toast.LENGTH_LONG).show();
				}
			}
			public void onFail() {
				Toast.makeText(SystemConfig.context, "获取相机授权失败", Toast.LENGTH_LONG).show();
			}
		}, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	/**
	 * 裁切图片
	 */
	public static void cropImage(final int requestCode, final File cropFile, final CropImageParam param) {
		AppUtils.requestPermission(new AppUtils.OnRequestPermissionListener() {
			public void onSuccess() {
				// 获取顶部Activity的基本信息
				ActivityInfo activityInfo = ActivityRepository.getInstance().getTopActivityInfo();
				Activity topActivity = activityInfo.activity;
				ReceiveDataState state = activityInfo.receiveDataState;
				// 构造意图
				Uri cropUri = AppUtils.getUriFromFile(cropFile);		// 定义需要裁切的文件
				Intent intent = new Intent("com.android.camera.action.CROP");
				grantUriPermission(intent, cropUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setDataAndType(cropUri, "image/*");
				intent.putExtra("crop", "true");
				if (param.previewX > 0) {
					intent.putExtra("aspectX", param.previewX);
				}
				if (param.previewY > 0) {
					intent.putExtra("aspectY", param.previewY);
				}
				if (param.outputX > 0) {
					intent.putExtra("outputX", param.outputX);
				}
				if (param.outputY > 0) {
					intent.putExtra("outputY", param.outputY);
				}
				OUTPUT_FILE = new File(SystemConfig.getTakePhotoPictureCacheDir(), UUID.randomUUID().toString() + ".jpeg");		// 设置裁切后的图片地址
				Uri outputUri = AppUtils.getUriFromFile(OUTPUT_FILE);
				grantUriPermission(intent, outputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
				intent.putExtra("scale", true);
				intent.putExtra("return-data", false);
				intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
				intent.putExtra("noFaceDetection", true);
				intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				// 发送意图
				try {
					state.state = ReceiveDataState.CROP_PICTURE;
					topActivity.startActivityForResult(intent, requestCode);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(SystemConfig.context, "device not support crop picture", Toast.LENGTH_LONG).show();
				}
			}
			public void onFail() {
				Toast.makeText(SystemConfig.context, "获取裁切图片授权失败", Toast.LENGTH_LONG).show();
			}
		}, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	/**
	 * 选择多媒体
	 * @param search    定义在MultiMediaRepository中，可选择视频、图片
	 * @param max        最大可选择数量，0表示不限制
	 * @param entities  已经选出来的媒体，传入则在选择页默认选中，不选择传null
	 */
	public static void pickMedia(final int requestCode, final int search, final int max, final List<MediaEntity> entities) {
		AppUtils.requestPermission(new AppUtils.OnRequestPermissionListener() {
			@Override public void onSuccess() {
				// 获取顶部Activity的基本信息
				ActivityInfo activityInfo = ActivityRepository.getInstance().getTopActivityInfo();
				Activity topActivity = activityInfo.activity;
				ReceiveDataState state = activityInfo.receiveDataState;
				// 构造意图
				Intent intent = new Intent(topActivity, MediaPickerActivity.class);
				Bundle data = new Bundle();
				data.putInt("search", search);
				data.putInt("max", max);
				data.putSerializable("entities", (Serializable) entities);
				intent.putExtras(data);
				// 启动选择
				state.state = ReceiveDataState.PICK_MEDIA;
				topActivity.startActivityForResult(intent, requestCode);
			}
		}, Manifest.permission.WRITE_EXTERNAL_STORAGE);
	}

	/**
	 * 启用摇一摇，启用之后需要注册OnDeviceShakedEvent事件
	 */
	public static void enableShake(Context context, boolean enableShake) {
		if (context instanceof Activity) {
			ActivityInfo activityInfo = ActivityRepository.getInstance().findInfoByActivity((Activity) context);
			if (activityInfo != null) {
				activityInfo.enableShake = enableShake;
				if (enableShake) {
					activityInfo.registerShake();
				} else {
					activityInfo.unRegisterShake();
				}
			}
		} else {
			Toast.makeText(context, "context must be a instance of Activity", Toast.LENGTH_LONG).show();
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// BUG修复
	///////////////////////////////////////////////////////////////////////////

	/**
	 * 解决在沉浸状态栏下输入框无法被顶起的问题
	 *      1. 布局内需要包含大小可调的布局如ListView、ScrollView等
	 *      2. 最大高度(使用权重weight)至少得大于软键盘和输入框的高度之和
	 *      3. 在Activity的onCreate方法中调用，在setContentView之后调用
	 */
	public static void assistActivity(Activity activity) {
		AndroidBug5497Workaround.assistActivity(activity);
	}

	///////////////////////////////////////////////////////////////////////////
	// 支持方法
	///////////////////////////////////////////////////////////////////////////

	private static void grantUriPermission(Intent intent, Uri uri, int permission) {
		List<ResolveInfo> resInfoList = SystemConfig.context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo resolveInfo : resInfoList) {
			SystemConfig.context.grantUriPermission(resolveInfo.activityInfo.packageName, uri, permission);
		}
	}
}
