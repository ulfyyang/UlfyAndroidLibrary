package com.ulfy.android.task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 提供并发功能的任务执行器
 */
final class ConcurrentTaskExecutor implements ITaskExecutor {
	private Executor executor = Executors.newCachedThreadPool();

	@Override public void post(Task task) {
		if (task != null) {
			executor.execute(task);
		}
	}
}
