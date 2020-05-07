package com.ulfy.android.task;

/**
 * 任务执行器
 */
public interface ITaskExecutor {
	/**
	 * 执行一个任务
	 */
	void post(Task task);
}
