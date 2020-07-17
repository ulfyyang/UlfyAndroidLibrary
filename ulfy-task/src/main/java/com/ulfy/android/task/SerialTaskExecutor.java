package com.ulfy.android.task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 顺序执行任务的执行器
 */
final class SerialTaskExecutor implements ITaskExecutor {
	private Executor executor = Executors.newSingleThreadExecutor();

	@Override public void post(Task task) {
		if (task != null) {
			executor.execute(task);
		}
	}
}
