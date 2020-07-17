package com.ulfy.android.task;

/**
 * 任务执行器，执行 {@link Task} 为基础的任务其子类任务。
 */
public interface ITaskExecutor {
	/**
	 * 执行一个任务
	 * @see Task
	 */
	void post(Task task);
}
