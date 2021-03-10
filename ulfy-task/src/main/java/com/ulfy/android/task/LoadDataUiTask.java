package com.ulfy.android.task;

import android.content.Context;

/**
 * 加载数据类型的 ui 任务，通过传入执行体代码块与响应器即可拼装成一个
 * 完整的任务。任务在执行过程中会在任务的不同阶段通过响应器来响应。
 */
public final class LoadDataUiTask extends UiTask {
	private OnExecute executeBody;		// 任务执行代码块，如果没有则会执行执行成功
	private Transponder transponder;	// 任务执行过程中的应答器，没有则不执行响应

	public LoadDataUiTask(Context context, Transponder transponder) {
		this(context, null, transponder);
	}

	public LoadDataUiTask(Context context, OnExecute executeBody, Transponder transponder) {
		super(context);
		this.executeBody = executeBody;
		this.transponder = transponder;
	}

	public final void setExecuteBody(OnExecute executeBody) {
		this.executeBody = executeBody;
	}

	/**
	 * 具体的执行方法
	 */
	@Override protected final void run(Task task) {
		if (isCancelUiHandler()) {		// 不执行已经取消更新 ui 的任务
			return;
		}
		if (executeBody == null) { // 如果没有指定执行体，则直接执行成功
			notifySuccess(TaskConfig.Config.LOAD_DATA_SUCCESS_TIP);
		} else {
			executeBody.onExecute(this);
		}
	}

	//-- 通知方法

	/**
	 * 通知任务启动了
	 */
	public final void notifyStart(final Object tipData) {
		if (transponder == null) {		// 没有设置响应器则不执行
			return;
		}
		runOnUiThread(() -> transponder.onTranspondMessage(new Message(Message.TYPE_START, tipData)));
	}

	/**
	 * 通知任务成功了
	 */
	public final void notifySuccess(final Object tipData) {
		if (transponder == null) {		// 没有设置响应器则不执行
			return;
		}
		runOnUiThread(() -> {
			transponder.onTranspondMessage(new Message(Message.TYPE_SUCCESS, tipData));
			transponder.onTranspondMessage(new Message(Message.TYPE_FINISH, tipData));
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
		if (transponder == null) {		// 没有设置响应器则不执行
			return;
		}
		runOnUiThread(() -> {
			transponder.onTranspondMessage(new Message(Message.TYPE_FAIL, tipData));
			transponder.onTranspondMessage(new Message(Message.TYPE_FINISH, tipData));
		});
	}

	/**
	 * 通知任务更新了
	 */
	public final void notifyUpdate(final Object data) {
		if (transponder == null) {		// 没有设置响应器则不执行
			return;
		}
		runOnUiThread(() -> transponder.onTranspondMessage(new Message(Message.TYPE_UPDATE, data)));
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
