package com.ulfy.android.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

/**
 * Ui工具类
 */
public final class UiUtils {
	private static final Handler uiHandler = new Handler(Looper.getMainLooper());		// 用于更新UI的Handler

	/**
	 * 屏幕宽度
	 */
	public static int screenWidth() {
		return UtilsConfig.context.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * 屏幕高度
	 */
	public static int screenHeight() {
		return UtilsConfig.context.getResources().getDisplayMetrics().heightPixels;
	}

	/**
	 * dp转px
	 */
	public static float dp2px(float dp) {
		return dp * Resources.getSystem().getDisplayMetrics().density;
	}

	/**
	 * px转dp
	 */
	public static float px2dp(float px) {
		return px / Resources.getSystem().getDisplayMetrics().density;
	}

	/**
	 * sp转px
	 */
	public static float sp2px(float sp) {
		return sp * Resources.getSystem().getDisplayMetrics().scaledDensity;
	}

	/**
	 * px转sp
	 */
	public static float px2sp(float px) {
		return px / Resources.getSystem().getDisplayMetrics().scaledDensity;
	}

	/**
	 * 根据指定的字号大小测量文字的长度
	 */
	public static float measureText(String content, float sp) {
		Paint paint = new Paint();
		paint.setTextSize(sp2px(sp));
		return paint.measureText(content);
	}

	/**
	 * 显示吐司：支持View和常规对象
	 * 		如果是View则以View原本的样式显示
	 * 		如果是常规对象则以toString的样式显示
	 */
	public static void show(Object message) {
		show(message, UtilsConfig.Config.toastGravity);		// 使用默认权重（查看安卓源码的默认设置）
	}

	/**
	 * 显示吐司：支持View和常规对象
	 * 		如果是View则以View原本的样式显示
	 * 		如果是常规对象则以toString的样式显示
	 */
	public static void show(final Object message, final int gravity) {
		if (message == null) {
			return;
		}
		if (isMainThread()) {
			Toast toast;
			if (message instanceof View) {
				View contentView = (View) message;
				clearParent(contentView);
				contentView.setVisibility(View.VISIBLE);
				toast = new Toast(UtilsConfig.context);
				toast.setView(contentView);
				toast.setDuration(Toast.LENGTH_SHORT);
				// 自定义视图取消偏移量
				toast.setGravity(gravity, 0, 0);
			} else {
				// 修改多行文本可以居中显示（根据源码修改）
				toast = Toast.makeText(UtilsConfig.context, message.toString(), Toast.LENGTH_SHORT);
				// 查找显示内容的TextView并设置可多行居中
				ViewGroup contentVG = (ViewGroup) toast.getView();
				if (contentVG.getChildCount() > 0) {
					TextView messageTV = (TextView) contentVG.getChildAt(0);
					messageTV.setGravity(Gravity.CENTER);
				}
				// 文本如果不是默认的设置则取消偏移量
				if (gravity == (Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM)) {
					toast.setGravity(gravity, toast.getXOffset(), toast.getYOffset());
				} else {
					toast.setGravity(gravity, 0, 0);
				}
			}
			toast.show();
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					show(message, gravity);
				}
			});
		}
	}

	/**
	 * 判断输入框中的内容是否为空
	 */
	public static boolean isEmpty(TextView textView) {
		String text = textView.getText().toString().trim();
		return text == null || text.length() == 0;
	}

	public static String toString(TextView textView) {
		if (isEmpty(textView)) {
			return "";
		} else {
			return textView.getText().toString();
		}
	}

	public static void setText(TextView et, CharSequence text) {
		et.setText(text);
		if (et instanceof EditText) {
			((EditText)et).setSelection(text.length());
		}
	}

	/**
	 * 根据视图模型创建对应的View
	 */
	public static View createView(Context context, IViewModel model) {
		return createView(context, null, model);
	}

	/**
	 * 根据视图模型创建对应的View
	 */
	public static View createView(Context context, View view, IViewModel model) {
		view = isViewCanReuse(view, model) ? view : createViewFromModel(context, model);
		if (view != null && view instanceof IView) {
			((IView)view).bind(model);
		}
		return view;
	}

	/**
	 * 根据视图的类型创建该视图的对象
	 */
	public static View createView(Context context, Class<? extends View> clazz) {
		return createViewFromClazz(context, clazz);
	}

	/**
	 * 根据视图的类型创建该视图的对象
	 */
	public static View createView(Context context, View view, Class<? extends View> clazz) {
		return isViewCanReuse(view, clazz) ? view : createViewFromClazz(context, clazz);
	}

	/**
	 * 根据model携带的反射类型创建view
	 */
	@SuppressWarnings("unchecked")
	private static View createViewFromModel(Context context, IViewModel model) {
		if (model == null || model.getViewClass() == null) {
			return null;
		}
		return createViewFromClazz(context, (Class<? extends View>) model.getViewClass());
	}

	/**
	 * 根据view的反射类型创建view
	 */
	private static View createViewFromClazz(Context context, Class<? extends View> clazz) {
		if (clazz == null) {
			return null;
		}
		try {
			return clazz.getConstructor(Context.class).newInstance(context);
		} catch (Exception e) {
			throw new IllegalArgumentException("create view failed", e);
		}
	}

	/**
	 * 判断一个view是否可以重复使用
	 */
	public static boolean isViewCanReuse(View view, IViewModel model) {
		return view != null && model != null && view.getClass() == model.getViewClass();
	}

	/**
	 * 判断一个view是否可以重复使用
	 */
	public static boolean isViewCanReuse(View view, Class<? extends View> viewClazz) {
		return view != null && viewClazz != null && view.getClass() == viewClazz;
	}

	/**
	 * 把试图作为容器中的唯一显示内容显示
	 */
	public static View displayViewOnViewGroup(IViewModel vm, ViewGroup vg) {
		return displayViewOnViewGroup(null, vm, vg);
	}

	/**
	 * 根据数据模型生成视图，并且自动绑定相关的数据显示到指定容器中
	 * 该方法会清除掉容器中原有的组件使用该组件替换
	 * 如果原始的view和需要创建的view相同，则会复用原始的view而不会重新创建，如果原始的view已经在vg中了，
	 * 则原始的view将不会变化
	 */
	public static View displayViewOnViewGroup(View originalView, IViewModel vm, ViewGroup vg) {
		if (vm.getViewClass() == null) {
			throw new IllegalArgumentException("无法创建View，vm中必须存储相应View的反射类型");
		}
		View view = null;
		// 复用或创建
		if (originalView != null && originalView.getClass() == vm.getViewClass()) {
			view = originalView;
		} else {
			view = createView(originalView.getContext(), (Class<? extends View>) vm.getViewClass());
		}
		// 数据模型绑定
		if (view instanceof IView) {
			((IView)view).bind(vm);
		} else {
			throw new IllegalArgumentException("view的反射类型必须实现IView接口");
		}
		// 不再容器中进行容器置换
		if (vg.indexOfChild(view) == -1) {
			vg.removeAllViews();
			vg.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		return view;
	}

	/**
	 * 将一个View填充到容器中，该容器中只会有一个View。如果该View已经在容器中了，则不会有任何操作
	 */
	public static void displayViewOnViewGroup(View view, ViewGroup container, boolean isMatch) {
		LayoutParams lp;
		if(isMatch) {
			lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		} else {
			lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		displayViewOnViewGroup(view, container, lp);
	}

	/**
	 * 将一个View填充到容器中，该容器中只会有一个View。如果该View已经在容器中了，则不会有任何操作
	 */
	public static void displayViewOnViewGroup(View view, ViewGroup container, LayoutParams lp) {
		if(view == null) {
			throw new NullPointerException("view cannot be null");
		}
		if (container == null) {
			throw new NullPointerException("container cannot be null");
		}
		if (lp == null) {
			throw new NullPointerException("layout param cannot be null");
		}
		// 如果在容器中存在并且只有一个，则不进行操作
		if (container.indexOfChild(view) == -1 || container.getChildCount() != 1) {
			container.removeAllViews();
			container.addView(view, lp);
		}
	}

	/**
	 * 使用一个源View替换目标View，目标View必须在一个ViewGroup中
	 */
	public static void replaceView(View sourceView, View targetView) {
		if(targetView.getParent() == null) {
			throw new IllegalArgumentException("无法替换目标View，目标View必须放到一个ViewGroup中");
		}
		ViewGroup contentVG = (ViewGroup) targetView.getParent();
		LayoutParams targetLP = targetView.getLayoutParams();
		int targetPosition = contentVG.indexOfChild(targetView);
		contentVG.removeViewAt(targetPosition);
		contentVG.addView(sourceView, targetPosition, targetLP);
	}

	/**
	 * 清空view的父容器，使得view被添加到其它的容器中不会发生错误
	 */
	public static void clearParent(View view) {
		if(view == null) {
			return;
		}
		ViewParent parent = view.getParent();
		if(parent == null || !(parent instanceof ViewGroup)) {
			return;
		}
		// 有些机型 把子 view 移除后还保存这布局参数，这样会导致这个 view 在其它的容器中出现布局匹配错误
		((ViewGroup)parent).removeView(view);
	}

	/*
	ListView 相关辅助方法
	 */

	public static void addViewToListViewHeader(View header, ListView listView) {
		clearParent(header);
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		header.setLayoutParams(layoutParams);
		listView.addHeaderView(header, null, false);
	}

	public static void addViewToListViewFooter(View footer, ListView listView) {
		clearParent(footer);
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		footer.setLayoutParams(layoutParams);
		listView.addFooterView(footer, null, false);
	}

	/**
	 * 当前线程是否是主线程
	 */
	public static boolean isMainThread() {
		// Thread.currentThread() == Looper.getMainLooper().getThread()
		return Looper.myLooper() == Looper.getMainLooper();
	}

	/**
	 * 在ui线程中执行代码段
	 */
	public static void runOnUiThread(Runnable runnable) {
		uiHandler.post(runnable);
	}

	/**
	 * 第一项是否可见
	 */
	public static boolean isFirstItemVisible(ListView listView) {
		return isFirstItemVisible(listView, false);
	}

	/**
	 * 第一项是否可见
	 */
	public static boolean isFirstItemVisible(ListView listView, boolean callSeeAll) {
		// 这个 Adapter 对设置的 Adapter 进行了包装，其中包括了 Header 和 Footer
		ListAdapter adapter = listView.getAdapter();
		// 没有数据则最后一条默认显示
		if (adapter == null || adapter.getCount() <= 0) {
			return true;
		}
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		// 到达第一项
		if (firstVisiblePosition == 0) {
			if (callSeeAll) {
				View firstChildItemView = listView.getChildAt(0);
				if (firstChildItemView != null) {
					return firstChildItemView.getTop() >= listView.getPaddingTop();
				} else {
					return false;
				}
			} else {
				return true;
			}
		}
		// 没有到达第一项
		else {
			return false;
		}
	}

	/**
	 * <p>最后一项是否可见</p>
	 * <p>该方法一定要在 任何可能修改 adapter 的操作之前调用才能得到正确的结果</p>
	 */
	public static boolean isLastItemVisible(ListView listView) {
		return isLastItemVisible(listView, false);
	}

	/**
	 * <p>最后一项是否可见</p>
	 * <p>该方法一定要在 任何可能修改 adapter 的操作之前调用才能得到正确的结果</p>
	 */
	public static boolean isLastItemVisible(ListView listView, boolean callSeeAll) {
		// 这个 Adapter 对设置的 Adapter 进行了包装，其中包括了 Header 和 Footer
		ListAdapter adapter = listView.getAdapter();
		// 没有数据则最后一条默认显示
		if (adapter == null || adapter.getCount() <= 0) {
			return true;
		}
		/*
		如果有些修改会造成 adapter 的变化，如：修改 adapter 获取长度的 List 数据列表；这些变化会导致 adapter.getCount() 获得的值发生变化进而影响到该方法的判断
		因此，该方法一定要在 任何可能修改 adapter 的操作之前调用才能得到正确的结果
		  */
		int lastVisiblePosition = listView.getLastVisiblePosition();
		int allCount = adapter.getCount();
		// 到达最后一项
		if (lastVisiblePosition >= 0 && lastVisiblePosition == allCount - 1) {
			if (callSeeAll) {
				View lastChildItemView = listView.getChildAt(listView.getChildCount() - 1);
				if (lastChildItemView != null) {
					return lastChildItemView.getBottom() + listView.getPaddingBottom() <= listView.getBottom();
				} else {
					return false;
				}
			} else {
				return true;
			}
		}
		// 没有到达最后一项
		else {
			return false;
		}
	}

	/*
	View相关的方法
	 */

	/**
	 * 判断是否触摸了该 view
	 */
	public static boolean isTouchView(MotionEvent event, View view) {
		int[] location = {0, 0};
		view.getLocationInWindow(location);
		int left = location[0], top = location[1], bottom = top + view.getHeight(), right = left + view.getWidth();
		// 只要找到一个点击的 view，则不隐藏软键盘
		if (event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom) {
			return true;
		} else {
			return false;
		}
	}
}
