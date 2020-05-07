package com.ulfy.android.bus;

/**
 * 方法运行模式
 */
public final class MethodMode {
	/** 主线程 */
	public static final int MAIN = 0;
	/** 和发布者在相同线程 */
	public static final int SAME_THREAD = 1;
	/** 后台线程：如果当前线程是主线程，则在新线程中执行；如果当前线程是后台线程，则在当前线程中执行 */
	public static final int BACKGROUND = 2;
	/** 始终在新开线程中执行 */
	public static final int ASYNC = 3;
}
