package com.ulfy.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ulfy.android.task_extension.UiTimer;

/**
 * 自动向上滚动的工具
 * 使用时必须指定和item的高度相同的具体值
 */
public class AutoScrollUpLayout extends ListView {
	public static final int DIRECTION_UP = 0;		// 向上滚动
	public static final int DIRECTION_DOWN = 1;		// 向下滚动
	public static final int MODE_JUMP = 0;			// 跳动滚动
	public static final int MODE_SMOOTH = 1;		// 平滑滚动
	public static final int DISTANCE_AUTO = 0;		// 自动匹配（匹配容器高度）

	private int scrollDirection;					// 滚动方向
	private int scrollMode;							// 滚动模式

	private int scrollDistance;						// 跳动模式下每次滚动的距离，默认为容器的高度。当只显示一行数据时无需指定，但是要是多行数据时需要指定为一行数据的高度。
	private int scrollTime;							// 跳动模式下滚动动画需要的时间（默认1000毫秒）
	private int scrollDelay;						// 跳动模式下每次滚动之间的时间间隔（默认2000毫秒）
	private int position;							// 跳动模式下用来记录跳动位置的标志

	private int scrollSpeed;						// 滚动模式下每次滚动移动的像素，值越大滚动的越快（默认为1像素）

	private ProxyAdapter proxyAdapter = new ProxyAdapter();
	private ListAdapter baseAdapter;
	private UiTimer uiTimer = new UiTimer();

	public AutoScrollUpLayout(Context context) {
		super(context);
		this.init(context, null);
	}

	public AutoScrollUpLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		// 对本身的设置
		this.setDivider(null);
		this.setFastScrollEnabled(false);
		this.setDividerHeight(0);
		// 接收布局文件中设置的参数
		if (attrs == null) {
			scrollDirection = DIRECTION_UP;
			scrollMode = MODE_JUMP;
			scrollDistance = DISTANCE_AUTO;
			scrollTime = 1000;
			scrollDelay = 2000;
			scrollSpeed = 1;
		} else {
			TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoScrollUpLayout);
			scrollDirection = typedArray.getInteger(R.styleable.AutoScrollUpLayout_scroll_direction, DIRECTION_UP);
			scrollMode = typedArray.getInt(R.styleable.AutoScrollUpLayout_scroll_mode, MODE_JUMP);
			scrollDistance = typedArray.getLayoutDimension(R.styleable.AutoScrollUpLayout_scroll_distance, DISTANCE_AUTO);
			scrollTime = typedArray.getInteger(R.styleable.AutoScrollUpLayout_scroll_time, 1000);
			scrollDelay = typedArray.getInteger(R.styleable.AutoScrollUpLayout_scroll_delay, 2000);
			scrollSpeed = typedArray.getInt(R.styleable.AutoScrollUpLayout_scroll_speed, 1);
			typedArray.recycle();
		}
		// 初始化跳动模式下的滚动初始位置
		if (scrollMode == MODE_JUMP) {
			position = scrollDirection == DIRECTION_UP ? -1 : 1;
		}
		// 对定时器的设置
		if (scrollMode == MODE_JUMP) {
			uiTimer.setDelay(scrollDelay);
			uiTimer.setUiTimerExecuteBody(new JumpExecuteBody());
		} else if (scrollMode == MODE_SMOOTH) {
			uiTimer.setDelay(16);	// 一秒六十帧
			uiTimer.setUiTimerExecuteBody(new SmoothExecuteBody());
		}
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 如果是自动适应，则取容器的高度
		if (scrollDistance == DISTANCE_AUTO) {
			scrollDistance = MeasureSpec.getSize(heightMeasureSpec);
		}
	}

	@Override public boolean onTouchEvent(MotionEvent ev) {
		// 屏蔽手指滑动事件
		return ev.getAction() != MotionEvent.ACTION_MOVE && super.onTouchEvent(ev);
	}

	@Override public void setOnItemClickListener(final AdapterView.OnItemClickListener listener) {
		if (listener == null) {
			super.setOnItemClickListener(null);
		} else {
			super.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					listener.onItemClick(parent, view, position % baseAdapter.getCount(), id);
				}
			});
		}
	}

	public void start() {
		uiTimer.schedule();
	}

	public void stop() {
		uiTimer.cancel();
	}

	///////////////////////////////////////////////////////////////////////////
	// 不同滚动效果的实现
	///////////////////////////////////////////////////////////////////////////

	private class JumpExecuteBody implements UiTimer.UiTimerExecuteBody {
		@Override public void onExecute(UiTimer timer, UiTimer.TimerDriver timerDriver) {
			if (scrollDirection == DIRECTION_UP) {
				position++;
			} else {
				position--;
			}
			if (position != 0) {		// 排除初始位置
				smoothScrollBy(scrollDirection == DIRECTION_UP ? scrollDistance : - scrollDistance, scrollTime);
				postDelayed(new Runnable() {			// 动画结束后如果有位置错位则矫正
					@Override public void run() {
						setSelection(position);
					}
				}, scrollTime);
			}
		}
	}

	private class SmoothExecuteBody implements UiTimer.UiTimerExecuteBody {
		@Override public void onExecute(UiTimer timer, UiTimer.TimerDriver timerDriver) {
			smoothScrollBy(scrollDirection == DIRECTION_UP ? scrollSpeed : -scrollSpeed, 0);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// 通过设置一个代理适配器纠正下标位置
	///////////////////////////////////////////////////////////////////////////

	@Override public void setAdapter(ListAdapter adapter) {
		baseAdapter = adapter;
		proxyAdapter.setAdapter(adapter);
		super.setAdapter(proxyAdapter);
		setSelection(scrollDirection == DIRECTION_UP ? 0 : Integer.MAX_VALUE);
	}

	private class ProxyAdapter extends BaseAdapter {
		private ListAdapter adapter;

		public void setAdapter(ListAdapter adapter) {
			this.adapter = adapter;
		}

		@Override public int getCount() {
			return adapter != null && adapter.getCount() > 0 ? Integer.MAX_VALUE : 0;
		}

		@Override public Object getItem(int position) {
			return adapter.getItem(position % adapter.getCount());
		}

		@Override public long getItemId(int position) {
			return adapter.getItemId(position % adapter.getCount());
		}

		@Override public View getView(int position, View convertView, ViewGroup parent) {
			return adapter.getView(position % adapter.getCount(), convertView, parent);
		}
	}
}
