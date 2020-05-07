package com.ulfy.android.task;

/**
 * 任务执行器，从该类中能得到相应特性的任务执行器
 */
public abstract class TaskExecutor {
	private static final ITaskExecutor defaultConcurrentTaskExecutor = new ConcurrentTaskExecutor();
	private static final ITaskExecutor defaultSerialTaskExecutor = new SerialTaskExecutor();
	private static final ITaskExecutor defaultSingleTaskExecutor = new SingleTaskExecutor();
	private static final ITaskExecutor defaultFinalUiTaskExecutor = new FinalUiTaskExecutor();

	/**
	 * 获得一个框架默认提供的执行器
	 */
	public static ITaskExecutor defaultExecutor() {
		return defaultConcurrentTaskExecutor();
	}

	/**
	 * 获得一个默认的并行执行的执行器
	 */
	public static ITaskExecutor defaultConcurrentTaskExecutor() {
		return defaultConcurrentTaskExecutor;
	}

	/**
	 * 获得一个默认的顺序执行的执行器
	 */
	public static ITaskExecutor defaultSerialTaskExecutor() {
		return defaultSerialTaskExecutor;
	}

	/**
	 * 获得一个默认的单任务执行器
	 */
	public static ITaskExecutor defaultSingleTaskExecutor() {
		return defaultSingleTaskExecutor;
	}

	/**
	 * 获得一个默认的最终界面执行器
	 */
	public static ITaskExecutor defaultFinalUiTaskExecutor() {
		return defaultFinalUiTaskExecutor;
	}

	/**
	 * 获得一个全新的并行执行的执行器
	 */
	public static ITaskExecutor newConcurrentTaskExecutor() {
		return new ConcurrentTaskExecutor();
	}

	/**
	 * 获得一个全新的顺序执行的执行器
	 */
	public static ITaskExecutor newSerialTaskExecutor() {
		return new SerialTaskExecutor();
	}

	/**
	 * 获得一个全新的单任务执行器
	 */
	public static ITaskExecutor newSingleTaskExecutor() {
		return new SingleTaskExecutor();
	}

	/**
	 * 获得一个全新的最终界面执行器
	 */
	public static ITaskExecutor newFinalUiTaskExecutor() {
		return new FinalUiTaskExecutor();
	}
}
