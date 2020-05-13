package com.ulfy.android.ui_injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ViewClick标签，为控件注入单机事件，需要自定义一个 Click 方法，方法需要满足以下的格式：
 *
 * <br>
 *     @ViewClick(ids = {R.id.控件id})
 *     void click(View v){}
 * <br>
 *     方法的权限不做要求。
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewClick {

	/**
	 * <p>控件的id数组，如果指定了多个控件的id，则每个控件点击都会执行到这里。</p>
	 * <p>如果只有一个id，可以不用大括号。</p>
     */
	int[] ids();
}
