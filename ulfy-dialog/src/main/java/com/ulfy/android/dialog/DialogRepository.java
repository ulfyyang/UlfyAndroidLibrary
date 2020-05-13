package com.ulfy.android.dialog;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DialogRepository {
	private static final DialogRepository instance = new DialogRepository();
	private Map<String, IDialog> dialogMap = new HashMap<>();
	private DialogRepository() { }
	public static DialogRepository getInstance() {
		return instance;
	}

	synchronized IDialog getDialogById(String dialogId) {
        return dialogMap.get(dialogId);
    }

	synchronized DialogRepository addDialog(IDialog dialog) {
		// 拥有相同ID的弹出框显示时会现关闭掉上一个弹出框
		if (dialogMap.containsKey(dialog.getDialogId())) {
			IDialog oldDialog = dialogMap.get(dialog.getDialogId());
			// 如果当前的弹出框和之前的弹出框是同一个，则不错任何处理
			if (dialog == oldDialog) {
				return this;
			} else {	// 否则关闭之前的弹出框
				oldDialog.dismiss();
			}
		}
		dialogMap.put(dialog.getDialogId(), dialog);
        return this;
	}

	synchronized DialogRepository removeDialog(IDialog dialog) {
		dialogMap.remove(dialog.getDialogId());
        return this;
	}

	synchronized void releaseDialogOnActivityDestoryed(Context context) {
		// 为了避免在边遍历删除的过程中出现异常，删除时先收集需要删除的对象，然后在删除
		List<IDialog> neededDismissDialogList = new ArrayList<>();
		for (String dialogID : dialogMap.keySet()) {
			IDialog dialog = dialogMap.get(dialogID);
			if (dialog.getDialogContext() == context) {
				neededDismissDialogList.add(dialog);
			}
        }
        for (IDialog dialog : neededDismissDialogList) {
			dialog.dismiss();
		}
	}
}
