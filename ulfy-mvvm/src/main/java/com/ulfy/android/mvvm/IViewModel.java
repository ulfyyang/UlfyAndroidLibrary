package com.ulfy.android.mvvm;

import java.util.List;

/**
 * 数据模型需要实现的接口
 * 		如果该数据模型是一个组，则泛型 C 对应着其中子模型列表的类型
 */
public interface IViewModel<C extends IViewModel> {

	/**
	 * 获取组内子模型列表
	 */
	default List<C> getChildViewModelList() {
		return null;
	}

	/**
	 * 数据模型使用的View
	 */
	Class<? extends IView> getViewClass();

}
