package com.ulfy.android.mvvm;

/**
 * View需要实现的接口
 */
public interface IView<M extends IViewModel> {
	
	/**
	 * 数据绑定方法
	 */
	void bind(M model);
}
