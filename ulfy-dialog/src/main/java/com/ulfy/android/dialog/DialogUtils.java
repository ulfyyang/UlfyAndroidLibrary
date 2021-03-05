package com.ulfy.android.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import java.util.Arrays;
import java.util.List;

/**
 * Dialog 辅助类
 */
public final class DialogUtils {

	/**
	 * 弹出一个弹窗
	 */
	public static IDialog showDialog(Context context, View dialogView) {
		return showDialog(context, DialogConfig.ULFY_MAIN_DIALOG_ID, dialogView);
	}

	/**
	 * 弹出一个弹窗
	 */
	public static IDialog showDialog(Context context, String dialogId, View dialogView) {
		IDialog dialog = new NormalDialog.Builder(context, dialogView).setDialogId(dialogId).build();
		dialog.show();
		return dialog;
	}

	/**
	 * 弹出一个弹窗
	 */
	public static IDialog showBottomDialog(Context context, View dialogView) {
		return showBottomDialog(context, DialogConfig.ULFY_MAIN_DIALOG_ID, dialogView);
	}

	/**
	 * 弹出一个弹窗
	 */
	public static IDialog showBottomDialog(Context context, String dialogId, View dialogView) {
		IDialog dialog = new NormalDialog.Builder(context, dialogView).setDialogId(dialogId)
				.setDialogAnimationId(R.style.window_anim_bottom).build();
		dialog.show();
		return dialog;
	}

	/**
	 * 弹出一个弹窗
	 */
	public static IDialog showTopDialog(Context context, View dialogView) {
		return showTopDialog(context, DialogConfig.ULFY_MAIN_DIALOG_ID, dialogView);
	}

	/**
	 * 弹出一个弹窗
	 */
	public static IDialog showTopDialog(Context context, String dialogId, View dialogView) {
		IDialog dialog = new NormalDialog.Builder(context, dialogView).setDialogId(dialogId)
				.setDialogAnimationId(R.style.window_anim_top).build();
		dialog.show();
		return dialog;
	}

	/**
	 * 弹出一个BottomSheet弹窗
	 */
	public static IDialog showBottomSheetDialog(Context context, View dialogView) {
		return showBottomSheetDialog(context, DialogConfig.ULFY_MAIN_DIALOG_ID, dialogView, false);
	}

	/**
	 * 弹出一个BottomSheet弹窗
	 */
	public static IDialog showBottomSheetDialog(Context context, String dialogId, View dialogView) {
		return showBottomSheetDialog(context, dialogId, dialogView, false);
	}

	/**
	 * 弹出一个BottomSheet弹窗
	 */
	public static IDialog showBottomSheetDialog(Context context, View dialogView, boolean noBackground) {
		return showBottomSheetDialog(context, DialogConfig.ULFY_MAIN_DIALOG_ID, dialogView, noBackground);
	}

	/**
	 * 弹出一个BottomSheet弹窗
	 */
	public static IDialog showBottomSheetDialog(Context context, String dialogId, View dialogView, boolean noBackground) {
		BounceBottomSheetDialog dialog = new BounceBottomSheetDialog(context, dialogId, noBackground);
		dialog.setContentView(dialogView);
		dialog.addSpringBackDisLimit(-1);
		dialog.show();
		return dialog;
	}



	/**
	 * 弹出一个局部弹出框
	 */
	public static IDialog showPopupDialog(Context context, View contentView, View anchorView) {
		IDialog dialog = new PopupDialog(context, DialogConfig.ULFY_MAIN_POPUP_ID, contentView, anchorView).build();
		dialog.show();
		return dialog;
	}



	/**
	 * 弹出一个按钮提示框
	 */
	public static void showAlertOneButtonDialog(Context context, String title, String message) {
		new AlertDialog(context, DialogConfig.ULFY_MAIN_ALERT_ID, title, message, 1, null).build().show();
	}

	/**
	 * 弹出一个按钮提示框
	 * 		可设置点击确定后的回调
	 */
	public static void showAlertOneButtonDialog(Context context, String title, String message, AlertDialog.OnClickAlertOkListener onClickAlertOkListener) {
		new AlertDialog(context, DialogConfig.ULFY_MAIN_ALERT_ID, title, message, 1, onClickAlertOkListener).build().show();
	}

	/**
	 * 弹出两个按钮确认框
	 * 		可设置点击确定后的回调
	 */
	public static void showAlertTwoButtonDialog(Context context, String title, String message, AlertDialog.OnClickAlertOkListener onClickAlertOkListener) {
		new AlertDialog(context, DialogConfig.ULFY_MAIN_ALERT_ID, title, message, 2, onClickAlertOkListener).build().show();
	}



	/**
	 * 弹出进度处理弹窗
	 * 		重复调用不会重复弹出窗口，只会更新数据
	 */
	public static void showProgressDialog(Context context, String title, int total, int current) {
		showProgressDialog(context, DialogConfig.ULFY_MAIN_PROGRASS_ID, title, total, current);
	}

	/**
	 * 弹出进度处理弹窗
	 * 		重复调用不会重复弹出窗口，只会更新数据
	 */
	public static void showProgressDialog(Context context, String dialogId, String title, int total, int current) {
		NormalDialog normalDialog = (NormalDialog) DialogRepository.getInstance().getDialogById(dialogId);
		if (normalDialog == null) {
			normalDialog = new NormalDialog.Builder(context, (View) DialogConfig.Config.progressDialogConfig.getProgressView(context))
					.setDialogId(DialogConfig.ULFY_MAIN_PROGRASS_ID).setFullDialog(true)
					.setTouchOutsideDismiss(false).setCancelable(false).build();
			normalDialog.show();
		}
		IProgressView progressView = (IProgressView) normalDialog.getDialogView();
		progressView.setTitle(title);
		progressView.updatePrograss(total, current);
	}



	/**
	 * 禁止屏幕触摸
	 */
	public static void disableTouch(Context context) {
		disableTouch(context, Color.TRANSPARENT);
	}

	/**
	 * 禁止屏幕触摸
	 */
	public static void disableTouch(Context context, int color) {
		View view = new View(context);
		view.setBackgroundColor(color);
		new NormalDialog.Builder(context, view).setDialogId(DialogConfig.ULFY_DISABLE_TOUCH_DIALOG_ID)
				.setFullDialog(true).setTouchOutsideDismiss(false).setNoBackground(true)
				.setCancelable(false).build().show();
	}

	/**
	 * 允许屏幕触摸
	 */
	public static void enableTouch(Context context) {
		dismissDialog(DialogConfig.ULFY_DISABLE_TOUCH_DIALOG_ID);
	}



	/**
	 * 弹出一个弹窗
	 * 		用于在一组字符串中快速选择出一个
	 */
	public static void showQuickPick(Context context, String title, IQuickPickView.OnItemClickListener onItemClickListener, CharSequence... list) {
		showQuickPick(context, title, onItemClickListener, Arrays.asList(list));
	}

	/**
	 * 弹出一个弹窗
	 * 		用于在一组字符串中快速选择出一个
	 */
	public static void showQuickPick(Context context, String title, IQuickPickView.OnItemClickListener onItemClickListener, List<CharSequence> list) {
		IQuickPickView quickPickView = Config.quickPickConfig.getQuickPickView(context);
		quickPickView.setTitle(title);
		quickPickView.setData(list);
		quickPickView.setOnItemClickListener(onItemClickListener);
		NormalDialog.Builder builder = new NormalDialog.Builder(context, (View) quickPickView);
		quickPickView.setDialogId(builder.getDialogId());
		builder.setDialogAnimationId(R.style.window_anim_bottom).build().show();
	}



	/**
	 * 关闭默认的弹出框
	 */
	public static void dismissDialog() {
		dismissDialog(DialogConfig.ULFY_MAIN_DIALOG_ID);
	}

	/**
	 * 关闭默认的局部弹出框
	 */
	public static void dismissPopupDialog() {
		dismissDialog(DialogConfig.ULFY_MAIN_POPUP_ID);
	}

	/**
	 * 关闭进度处理弹窗
	 */
	public static void dismissProgressDialog() {
		dismissDialog(DialogConfig.ULFY_MAIN_PROGRASS_ID);
	}

	/**
	 * 关闭指定的弹出框
	 */
	public static void dismissDialog(String dialogId) {
		IDialog dialog = DialogRepository.getInstance().getDialogById(dialogId);
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	/**
	 * 主弹窗是否已经显示了
	 */
	public static boolean isShow() {
		return isShow(DialogConfig.ULFY_MAIN_DIALOG_ID);
	}

	/**
	 * 指定的弹窗是否显示了
	 */
	public static boolean isShow(String dialogId) {
		return DialogRepository.getInstance().getDialogById(dialogId) != null;
	}

	/**
	 * 弹出框模块配置
	 */
	public static class Config {
		public static QuickPickConfig quickPickConfig;									// 快速选择弹窗配置

		static {
			quickPickConfig = new DefaultQuickPickConfig();
		}

		public interface QuickPickConfig {
			public IQuickPickView getQuickPickView(Context context);
		}

		public static class DefaultQuickPickConfig implements QuickPickConfig {
			@Override public IQuickPickView getQuickPickView(Context context) {
				return new QuickPickView(context);
			}
		}
	}
}
