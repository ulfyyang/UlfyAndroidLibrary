package com.ulfy.android.task;

import android.content.Context;

/**
 * 加载数据类型的 Ui 任务
 */
public final class LoadDataUiTask extends UiTask {
	private OnExecute executeBody;
	private Transponder transponder;

	public LoadDataUiTask(Context context, Transponder transponder) {
		this(context, null, transponder);
	}

	public LoadDataUiTask(Context context, OnExecute executeBody, Transponder transponder) {
		super(context);
		this.executeBody = executeBody;
		this.transponder = transponder;
	}

	/**
	 * 具体的执行方法
	 */
	@Override protected final void run(Task task) {
		if (!isCancelUiHandler()) {
			if (executeBody == null) {
				notifySuccess(TaskConfig.Config.LOAD_DATA_SUCCESS_TIP);
			} else {
				executeBody.onExecute(this);
			}
		}
	}

	//-- 通知方法

	/**
	 * 通知任务启动了
	 */
	public final void notifyStart(final Object tipData) {
		runOnUiThread(new Runnable() {
			public void run() {
				transponder.onTranspondMessage(new Message(Message.TYPE_START, tipData));
			}
		});
	}

	/**
	 * 通知任务成功了
	 */
	public final void notifySuccess(final Object tipData) {
		runOnUiThread(new Runnable() {
			public void run() {
				transponder.onTranspondMessage(new Message(Message.TYPE_SUCCESS, tipData));
				transponder.onTranspondMessage(new Message(Message.TYPE_FINISH, tipData));
			}
		});
	}

	/**
	 * 通知任务失败了
	 */
	public final void notifyFail(Exception e) {
		notifyFail(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
	}

	/**
	 * 通知任务失败了
	 */
	public final void notifyFail(final Object tipData) {
		runOnUiThread(new Runnable() {
			public void run() {
				transponder.onTranspondMessage(new Message(Message.TYPE_FAIL, tipData));
				transponder.onTranspondMessage(new Message(Message.TYPE_FINISH, tipData));
			}
		});
	}

	/**
	 * 通知任务更新了
	 */
	public final void notifyUpdate(final Object data) {
		runOnUiThread(new Runnable() {
			public void run() {
				transponder.onTranspondMessage(new Message(Message.TYPE_UPDATE, data));
			}
		});
	}

	public void setExecuteBody(OnExecute executeBody) {
		this.executeBody = executeBody;
	}

	/**
	 * 任务执行体
	 */
	public interface OnExecute {
		/**
		 * 执行体
		 */
		void onExecute(LoadDataUiTask task);
	}
}
