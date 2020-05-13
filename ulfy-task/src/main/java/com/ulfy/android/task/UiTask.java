package com.ulfy.android.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 可以执行UI处理的任务
 */
public abstract class UiTask extends Task {
	private Context context;
	private Handler uiHandler = new UiHandler();
	private int identityId = hashCode();					// 为每一个任务定义唯一标识，避免任务回调混乱
	private boolean cancelUiHandler;

	public UiTask(Context context) {
		this.context = context;
	}

	@Override public void run() {
		TaskRepository.getInstance().addUiTask(context, this);
		super.run();
		TaskRepository.getInstance().removeUiTask(context, this);
	}

	protected final synchronized void runOnUiThread(Runnable runnable) {
		if (cancelUiHandler) {
			return;
		}
		uiHandler.sendMessage(uiHandler.obtainMessage(identityId, runnable));
	}

	public synchronized void setCancelUiHandler(boolean cancelUiHandler) {
		this.cancelUiHandler = cancelUiHandler;
		if (cancelUiHandler) {
			uiHandler.removeMessages(identityId);
		}
	}

	public synchronized boolean isCancelUiHandler() {
		return cancelUiHandler;
	}

	private class UiHandler extends Handler {

		UiHandler() {
			super(Looper.getMainLooper());
		}

		public void handleMessage(Message msg) {
			if (msg.what == identityId && msg.obj instanceof Runnable && !cancelUiHandler) {
				((Runnable)msg.obj).run();
			}
		}
	}
}
