package com.ulfy.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ulfy.android.mvvm.IViewModel;

import java.util.List;

/**
 * 简单适配器，该适配器用于简单地列表数据展示
 */
public final class ListAdapter<M extends IViewModel> extends UlfyBaseAdapter {

	public interface OnItemClickListener<M extends IViewModel> {
		/**
		 * @param parent 父容器
		 * @param view 点击的视图
		 * @param position 点击的位置
		 * @param model 数据模型
		 */
		void onItemClick(AdapterView<?> parent, View view, int position, M model);
	}

	public interface OnItemLongClickListener<M extends IViewModel> {
		/**
		 * @param parent 父容器
		 * @param view 点击的视图
		 * @param position 点击的位置
		 * @param model 数据模型
		 */
		boolean onItemClick(AdapterView<?> parent, View view, int position, M model);
	}

	private List<M> modelList;
	private OnItemClickListener<M> onItemClickListener;    		// 单击事件
	private OnItemLongClickListener<M> onItemLongClickListener;	// 长按事件

	public ListAdapter() {}

	public ListAdapter(List<M> modelList) {
		setData(modelList);
	}

	public ListAdapter setData(List<M> modelList) {
		if (modelList == null) {
			throw new NullPointerException("model list cannot be null");
		}
		this.modelList = modelList;
		return this;
	}

	public ListAdapter removeItem(int position) {
		modelList.remove(position);
		notifyDataSetChanged();
		return this;
	}

	public ListAdapter removeItem(M model) {
		modelList.remove(model);
		notifyDataSetChanged();
		return this;
	}

	public void setOnItemClickListener(final AbsListView listView, OnItemClickListener<M> onItemClickListener) {
		if (listView == null) {
			throw new NullPointerException("list view can not be null");
		}
		this.onItemClickListener = onItemClickListener;
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (ListAdapter.this.onItemClickListener == null) {
					return;
				}
				if (isHaveEmptyView() && (modelList == null || modelList.size() == 0)) {
					return;
				}
				if (listView instanceof ListView) {
					position = position - ((ListView)listView).getHeaderViewsCount();
				}
				ListAdapter.this.onItemClickListener.onItemClick(parent, view, position, modelList.get(position));
			}
		});
	}

	public void setOnItemLongClickListener(final AbsListView listView, OnItemLongClickListener<M> onItemLongClickListener) {
		if (listView == null) {
			throw new NullPointerException("list view can not be null");
		}
		this.onItemLongClickListener = onItemLongClickListener;
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (ListAdapter.this.onItemLongClickListener == null) {
					return false;
				}
				if (isHaveEmptyView() && (modelList == null || modelList.size() == 0)) {
					return false;
				}
				if (listView instanceof ListView) {
					position = position - ((ListView)listView).getHeaderViewsCount();
				}
				return ListAdapter.this.onItemLongClickListener.onItemClick(parent, view, position, modelList.get(position));
			}
		});
	}

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		if (isHaveEmptyView() && (modelList == null || modelList.size() == 0)) {
			return getEmptyView();
		} else {
			IViewModel model = modelList.get(position);
			convertView = UiUtils.createView(parent.getContext(), convertView, model);
			return convertView;
		}
	}

	@Override public int getCount() {
		if (modelList == null || modelList.size() == 0) {
			return isHaveEmptyView() ? 1 : 0;
		} else {
			return  modelList.size();
		}
	}

	@Override public Object getItem(int position) {
		return modelList.get(position);
	}

	@Override public long getItemId(int position) {
		return position;
	}
}
