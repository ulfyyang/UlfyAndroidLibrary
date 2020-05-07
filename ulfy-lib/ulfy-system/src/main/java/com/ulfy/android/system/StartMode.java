package com.ulfy.android.system;


/**
 * Activity 的启动参数
 */
public final class StartMode {
	/** 正常模式启动，使用该模式将会按照常规的方式启动Activity。 */
	public static final int NORMAL_START = 1;
	/** 重新启动模式启动，使用该模式启动将会在启动一个新的Activity之后关闭之前的Activity。 */
	public static final int RE_START = 2;
	/** 清空顶部模式启动，使用该模式将会在启动新的Activity之后关闭原来Activity到顶部的之间的Activity。 */
	public static final int CLEARTOP_START = 3;
	/** 清空所有模式启动，使用该模式将会在启动新的Activity之后将原有的所有Activity关闭。 */
	public static final int CLEARALL_START = 4;
}