package com.ulfy.android.task;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Objects;

/**
 * 可以执行 ui 处理的任务，为子类提供更新 ui 的能力，并受到关联 Activity 生命周期的限制。
 * @see TaskRepository {@link #runOnUiThread(Runnable)}
 */
public abstract class UiTask extends Task {
	private static final String TAG = UiTask.class.getName();
	private Handler uiHandler = new UiHandler();	// 为任务提供更新 ui 的能力
	private Context context;						// 任务关联的 Activity 上下文，用于跟随上下文的生命周期
	private int identityId = hashCode();			// 为每一个任务定义唯一标识，避免任务回调混乱
	private boolean cancelUiHandler;				// 是否取消更新 ui，如果取消则更新 ui 的操作不会执行

	/**
	 * 构造方法，参数必须是一个 Activity 的上下文。和 ui 相关的任务都涉及到
	 * Activity 的生命周期关联（例如：Activity 被销毁后无法在更新 ui），因
	 * 此上下文必须是一个 Activity 的实例。
	 */
	public UiTask(Context context) {
		Objects.requireNonNull(context, "context can not be null");
		if (context instanceof Activity) {
			this.context = context;
		} else {
			throw new IllegalArgumentException("a ui task must associate with a activity context");
		}
	}

	/**
	 * 获取和任务关联的上线文
	 */
	protected final Context getContext() {
		return context;
	}

	/**
	 * 任务执行开始时注册到任务仓库中，任务执行结束前取消注册到仓库中的任务
	 * 任务定制不可以复写该方法，需要通过复写 {@link Task#run(Task)} 方
	 * 法来对任务进行定制
	 */
	@Override public final void run() {
		TaskRepository.getInstance().addUiTask(context, this);
		super.run();
		TaskRepository.getInstance().removeUiTask(context, this);
	}

	/**
	 * 该方法为子类提供更新 ui 的能力
	 */
	protected synchronized final void runOnUiThread(Runnable runnable) {
		if (!cancelUiHandler && runnable != null) {
			uiHandler.sendMessage(uiHandler.obtainMessage(identityId, runnable));
		}
	}

	private class UiHandler extends Handler {
		UiHandler() { super(Looper.getMainLooper()); }
		@Override public void handleMessage(Message msg) {
			if (msg.what == identityId && msg.obj instanceof Runnable && !cancelUiHandler) {
				((Runnable) msg.obj).run();
			}
		}
	}

	/**
	 * 设置取消 ui 操作
	 */
	public synchronized void setCancelUiHandler(boolean cancelUiHandler) {
		this.cancelUiHandler = cancelUiHandler;
		if (cancelUiHandler) {
			uiHandler.removeMessages(identityId);
		}
		Log.v(TAG, String.format("the ability of task associated with %s of update ui has been %s!",
				context.getClass().getName(), cancelUiHandler ? "canceled" : "granted"));
	}

	/**
	 * 判断是否取消 ui 操作
	 */
	public synchronized boolean isCancelUiHandler() {
		return cancelUiHandler;
	}
}
