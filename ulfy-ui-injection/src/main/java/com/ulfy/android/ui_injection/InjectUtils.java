package com.ulfy.android.ui_injection;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * UI 注入辅助类，通过该类完成类中注解的解析与注入。
 * <br>
 *     处理过程不支持类中嵌套类，但是支持继承，在继承树中扫描的顺序是从子类到父类的顺序进行扫描的。
 * <br>
 *     也就是说对每个没有集成关系的类都要进行处理，如果是继承关系的类，则在继承树的任意一个位置调用该方法都能对整个层级进行遍历。
 * <br>
 *     一个组件若在子类中已经被找到，则在父类上发现的同名组件将会被忽略
 */
public final class InjectUtils {

	/**
	 * 解析 @Layout 注解，将 @Layout 注解中指定的布局填充到 目标界面中。
	 * <br>
	 *     目前支持的界面类型有 Activity、ViewGroup
	 */
	public static void processLayoutFile(Object target) {
		int layoutId = findUILayoutFileID(target.getClass());
		if (layoutId != -1) {
			if(target instanceof Activity) {
				((Activity) target).setContentView(layoutId);
			} else if(target instanceof ViewGroup) {
				ViewGroup viewGroup = (ViewGroup) target;
				LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup);
			}
		}
	}

	/**
	 * 解析 @Layout 注解，找到 继承树 中的 @Layout 注解中携带的布局 id 信息
	 * <br>
	 *     在继承树中查找的顺序为从子类到父类的顺序
	 * @return 查找到的布局 id 信息；如果找不到，则返回 -1
	 */
	public static int findUILayoutFileID(Class<?> clazz) {
		if(clazz == Activity.class || clazz == FragmentActivity.class || clazz == android.app.Fragment.class || clazz == android.support.v4.app.Fragment.class || clazz == ViewGroup.class) {
			return -1;
		} else {
			if(clazz.isAnnotationPresent(Layout.class)) {
				return clazz.getAnnotation(Layout.class).id();
			}
		}
		return findUILayoutFileID(clazz.getSuperclass());		// 向父类进行递归
	}
	
	/**
	 * 解析 @ViewById 注解，为 View 声明找到对应的组件，并进行绑定。
	 * <br>
	 *     目前支持任意权限的控件声明，即使该声明是私有的。
	 * <br>
	 *     处理的目标必须是 Activity 或 View
	 */
	public static void processViewById(Object target) {
		bindViewById(target, target, findUIViewByIdList(target.getClass(), new ArrayList<Field>()));
	}

	public static void processViewById(Object search, Object target) {
		bindViewById(search, target, findUIViewByIdList(target.getClass(), new ArrayList<Field>()));
	}
	
	/**
	 * 解析 @ViewById 注解，从继承树中查找成员变量，将找到的成员变量放到一个列表集合中。
	 * <br>
	 *     在继承树中查找的顺序为从子类到父类的顺序
	 * @return 查找到的成员变量列表集合
	 */
	private static List<Field> findUIViewByIdList(Class<?> clazz, List<Field> fieldList) {
		if(clazz == Activity.class || clazz == FragmentActivity.class || clazz == android.app.Fragment.class || clazz == android.support.v4.app.Fragment.class || clazz == View.class) {
			return fieldList;
		}
		for(Field field : clazz.getDeclaredFields()) {
			if(field.isAnnotationPresent(ViewById.class)) {
				boolean alreadyFindSameNameField = false;
				for (Field findedField : fieldList) {
					if (field.getName().equals(findedField.getName())) {
						alreadyFindSameNameField = true;
						break;
					}
				}
				if (!alreadyFindSameNameField) {
					fieldList.add(field);
				}
			}
		}
		return findUIViewByIdList(clazz.getSuperclass(), fieldList);
	}

	/**
	 * 把查询到的 ViewById 列表绑定到目标 UI 中
	 */
	private static void bindViewById(Object search, Object target, List<Field> viewByIdList) {
		for(Field field : viewByIdList) {
            int viewId = field.getAnnotation(ViewById.class).id();
            if (viewId > 0) {
				View view = null;
				if(search instanceof Activity) {
					view = ((Activity) search).findViewById(viewId);
				} else if(search instanceof View) {
					view = ((View) search).findViewById(viewId);
				}
				try {
					field.setAccessible(true);
					field.set(target, view);
				} catch (Exception e) {
					if (!view.getClass().isAssignableFrom(field.getClass())) {		// 如果类型没有对上，则无法设置
						throw new IllegalStateException(view.getClass() + " cannot be cast to " + field.getClass() + "on field " + field.getName());
					}
				}
            }
		}
	}
	
	/**
	 * 解析 @ViewClick 注解，从继承树中查找注册方法，为该 View 添加单机事件。
	 * <br>
	 *     目前支持任意权限的方法声明，即使该声明是私有的。
	 * <br>
	 *     处理的目标必须是 Activity 或 View
	 */
	public static void processViewClick(Object target) {
		bindViewClick(target, target, findUIViewClickList(target.getClass(), new ArrayList<Method>()));
	}

	public static void processViewClick(Object search, Object target) {
		bindViewClick(search, target, findUIViewClickList(target.getClass(), new ArrayList<Method>()));
	}

	/**
	 * 解析 @ViewClick 注解，从继承树中查找成员方法，将找到的成员方法放到一个列表集合中。
	 * <br>
	 *     在继承树中查找的顺序为从子类到父类的顺序
	 * @return 查找到的成员方法列表集合
	 */
	private static List<Method> findUIViewClickList(Class<?> clazz, List<Method> methodList) {
		if(clazz == Activity.class || clazz == FragmentActivity.class || clazz == android.app.Fragment.class || clazz == android.support.v4.app.Fragment.class || clazz == View.class) {
            return methodList;
        }
		for(Method method : clazz.getDeclaredMethods()) {
			if(method.isAnnotationPresent(ViewClick.class)) {
				boolean alreadyFindSameNameMethod = false;
				for (Method findedMethod : methodList) {
					if (method.getName().equals(findedMethod.getName())) {
						alreadyFindSameNameMethod = true;
						break;
					}
				}
				if (!alreadyFindSameNameMethod) {
					methodList.add(method);
				}
			}
		}
		return findUIViewClickList(clazz.getSuperclass(), methodList);
	}

	/**
	 * 把查询到的 ViewClick 列表绑定到目标 UI 中
	 */
	private static void bindViewClick(Object search, Object target, List<Method> viewClickList) {
		for(Method method : viewClickList) {
			ViewClick viewClick = method.getAnnotation(ViewClick.class);
			int[] ids = viewClick.ids();
			boolean longClick = viewClick.longClick();

			for(int id : ids) {
				View view = null;
				if(search instanceof Activity) {
					view = ((Activity) search).findViewById(id);
				} else if(search instanceof View) {
					view = ((View) search).findViewById(id);
				}
				if (view == null) {
					throw new IllegalArgumentException("cannot regist method: " + method.getName() + " on a null view");
				}

				if (!longClick) {
					view.setOnClickListener(new OnViewClickListener(target, method));
				} else {
					view.setOnLongClickListener(new OnViewLongClickListener(target, method));
				}
			}
		}
	}

	// View 的点击事件
	private static class OnViewClickListener implements View.OnClickListener {
		private Object target;
		private Method clickMethod;

		OnViewClickListener(Object target, Method clickMethod) {
			this.target = target;
			this.clickMethod = clickMethod;
		}

		@Override public void onClick(View v) {
			try {
				clickMethod.setAccessible(true);
				clickMethod.invoke(target, v);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	// View 的长按事件
	private static class OnViewLongClickListener implements View.OnLongClickListener {
		private Object target;
		private Method clickMethod;

		OnViewLongClickListener(Object target, Method clickMethod) {
			this.target = target;
			this.clickMethod = clickMethod;
		}

		@Override public boolean onLongClick(View v) {
			try {
				clickMethod.setAccessible(true);
				clickMethod.invoke(target, v);
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
			return true;
		}
	}
}
