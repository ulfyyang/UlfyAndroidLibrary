package com.ulfy.android.mvvm;

/**
 * 数据模型需要实现的接口
 */
public interface IViewModel {
	
	/**
	 * 数据模型使用的View
	 */
	Class<? extends IView> getViewClass();
}
