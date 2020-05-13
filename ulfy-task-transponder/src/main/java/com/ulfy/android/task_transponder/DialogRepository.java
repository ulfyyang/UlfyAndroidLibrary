package com.ulfy.android.task_transponder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class DialogRepository {
	private static final DialogRepository instance = new DialogRepository();
	private List<CustomDialog> dialogList = new ArrayList<>();
	private AlertDialog notNetConnectionDialog;

	private DialogRepository() { }

	static DialogRepository getInstance() {
		return instance;
	}

	synchronized DialogRepository addDialog(CustomDialog dialog) {
		dialogList.add(dialog);
        return this;
	}

	synchronized DialogRepository removeDialog(CustomDialog dialog) {
		dialogList.remove(dialog);
        return this;
	}

	synchronized AlertDialog getNotNetConnectionDialog() {
		if (notNetConnectionDialog == null && ActivityRepository.getInstance().getTopActivity() != null) {
			notNetConnectionDialog = new AlertDialog.Builder(ActivityRepository.getInstance().getTopActivity())
					.setTitle("网络错误").setMessage("抱歉，网络连接失败，是否进行网络设置？")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							ActivityRepository.getInstance().getTopActivity().startActivity(new Intent(Settings.ACTION_SETTINGS));
						}
					}).create();
			notNetConnectionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override public void onDismiss(DialogInterface dialog) {
					notNetConnectionDialog = null;
				}
			});
		}
		return notNetConnectionDialog;
	}

	synchronized void releaseDialogOnActivityDestory(Context context) {
		// 取消常规弹窗
		Iterator<CustomDialog> iterator = dialogList.iterator();
		while (iterator.hasNext()) {
			CustomDialog dialog = iterator.next();
			if (dialog.getContext() == context) {
				iterator.remove();
				dialog.dismiss();
			}
		}
		// 取消网络监测弹窗
		if (notNetConnectionDialog != null && notNetConnectionDialog.getContext() == context) {
			notNetConnectionDialog.dismiss();
		}
	}
}
