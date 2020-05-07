package com.ulfy.android.ui_injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Layout标签，该标签用于标注UI组件的布局文件标注。
 * <br>
 * 目前该标签只能用到 Activity 或者 View 的类声明上，不支持如 Fragment 等其它的 UI 控件。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Layout {

	/**
	 * 指定布局ID，把 R.layout.布局id 放到这里即可
	 */
	int id();
}