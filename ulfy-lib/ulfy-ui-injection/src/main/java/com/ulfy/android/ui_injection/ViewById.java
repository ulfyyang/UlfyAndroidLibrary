package com.ulfy.android.ui_injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ViewById标签，在需要注入的 View 声明上加上该注解，并填写该注解对应的 R.id.组件id 即可。
 * <br>
 * 该注解可以放到任意权限的 控件声明上，即使这个声明是私有的。
 */
@Target(value = {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewById {

    /**
     * 指定控件的id，把 R.id.组件id 放到这里即可
     */
    int id();
}
