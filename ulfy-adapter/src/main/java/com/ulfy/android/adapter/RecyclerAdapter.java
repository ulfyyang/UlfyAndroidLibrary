package com.ulfy.android.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerAdapter<M extends IViewModel> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ViewTypeHolder viewTypeHolder = new ViewTypeHolder();
    private List<M> oldModleList;
    private List<M> modelList;
    private List<View> viewList = new ArrayList<>();
    private Comparator<M> comparator;
    private View headerView, footerView, emptyView, loadingView;
    private OnItemClickListener<M> itemClickListener;
    private OnItemLongClickListener<M> itemLongClickListener;

    public RecyclerAdapter() {
        setHasStableIds(true);
    }

    public RecyclerAdapter(List<M> modelList) {
        setHasStableIds(true);
        setData(modelList);
    }

    public RecyclerAdapter setData(List<M> modelList) {
        if (modelList == null) {
            throw new NullPointerException("model list cannot be null");
        }
        this.modelList = modelList;
        return this;
    }

    public RecyclerAdapter setComparator(Comparator<M> comparator) {
        this.comparator = comparator;
        return this;
    }

    public RecyclerAdapter setHeaderView(View headerView) {
        if (headerView != null) {
            UiUtils.clearParent(headerView);
            headerView.setVisibility(View.VISIBLE);
        }
        this.headerView = headerView;
        return this;
    }

    public View getHeaderView() {
        return headerView;
    }

    public RecyclerAdapter setFooterView(View footerView) {
        if (footerView != null) {
            UiUtils.clearParent(footerView);
            footerView.setVisibility(View.VISIBLE);
        }
        this.footerView = footerView;
        return this;
    }

    public View getFooterView() {
        return footerView;
    }

    public RecyclerAdapter setEmptyView(View emptyView) {
        if (emptyView != null) {
            UiUtils.clearParent(emptyView);
            emptyView.setVisibility(View.VISIBLE);
        }
        this.emptyView = emptyView;
        return this;
    }

    public RecyclerAdapter setLoadingView(View loadingView) {
        if (loadingView != null) {
            UiUtils.clearParent(loadingView);
            loadingView.setVisibility(View.VISIBLE);
        }
        this.loadingView = loadingView;
        return this;
    }

    /**
     * 设置单机事件
     */
    public RecyclerAdapter setOnItemClickListener(OnItemClickListener<M> listener) {
        this.itemClickListener = listener;
        registerItemClickListenerIfNeed();
        return this;
    }

    public RecyclerAdapter setOnItemLongClickListener(OnItemLongClickListener<M> listener) {
        this.itemLongClickListener = listener;
        registerItemClickListenerIfNeed();
        return this;
    }

    /**
     * 根据条件 shouldRegisterItemClickListener() 来执行是否注册单击事件的回调
     *      该方法可以在子类中手动触发
     */
    void registerItemClickListenerIfNeed() {
        View.OnClickListener clickListener = shouldRegisterItemClickListener() ? new OnClickListenerImpl() : null;
        View.OnLongClickListener longClickListener = shouldRegisterItemLongClickListener() ? new OnLongClickListenerImpl() : null;
        for (int i = 0; i < viewList.size(); i++) {
            UiUtils.setViewClickListener(viewList.get(i), clickListener, longClickListener);
        }
    }

    /**
     * 判断是否应该注册单击事件，在本类中，设置了单击监听事件时表示应该注册。
     *      该方法可以在子类中加入判定条件
     */
    protected boolean shouldRegisterItemClickListener() {
        return itemClickListener != null;
    }

    /**
     * 判断是否应该注册长按事件，在本类中，设置了长按事件表示应该注册。
     * 该方法可以在子类中加入判断条件
     */
    protected boolean shouldRegisterItemLongClickListener() {
        return itemLongClickListener != null;
    }

    private class OnClickListenerImpl implements View.OnClickListener {
        public void onClick(View v) {
            int clickIndex = (int) v.getTag(ViewHolder.TAG_CLICK_INDEX);
            if (itemClickListener != null) {
                itemClickListener.onItemClick((ViewGroup) v.getParent(), v, clickIndex, modelList.get(clickIndex));
            }
            dispatchOnItemClick((ViewGroup) v.getParent(), v, clickIndex, modelList.get(clickIndex));
        }
    }

    private class OnLongClickListenerImpl implements View.OnLongClickListener {
        @Override public boolean onLongClick(View v) {
            int clickIndex = (int) v.getTag(ViewHolder.TAG_CLICK_INDEX);
            if (itemLongClickListener != null) {
                itemLongClickListener.onItemLongClick((ViewGroup) v.getParent(), v, clickIndex, modelList.get(clickIndex));
            }
            dispatchOnItemLongClick((ViewGroup) v.getParent(), v, clickIndex, modelList.get(clickIndex));
            return true;
        }
    }

    /**
     * 在单击事件触发的时候执行这个派发方法，让子类可以有自己的对应处理逻辑
     */
    protected void dispatchOnItemClick(ViewGroup parent, View view, int position, M model) { }

    /**
     * 在长按事件触发的时候执行这个派发方法，让子类可以有自己的对应处理逻辑
     */
    protected void dispatchOnItemLongClick(ViewGroup parent, View view, int position, M model) { }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (headerView != null && viewType == headerView.hashCode()) {
            return new HeaderFooterEmptyLoadingViewHolder(headerView);
        } else if (footerView != null && viewType == footerView.hashCode()) {
            return new HeaderFooterEmptyLoadingViewHolder(footerView);
        } else if (emptyView != null && viewType == emptyView.hashCode()) {
            return new HeaderFooterEmptyLoadingViewHolder(emptyView);
        } else if (loadingView != null && viewType == loadingView.hashCode()) {
            return new HeaderFooterEmptyLoadingViewHolder(loadingView);
        } else {
            View view = UiUtils.createViewFromClazz(parent.getContext(), viewTypeHolder.getViewClazzByType(viewType));
            viewList.add(view);
            registerItemClickListenerIfNeed();
            return new ViewHolder(view);
        }
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (headerView != null) {
            position--;
        }
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bind(modelList.get(position), position);
        }
    }

    @Override public int getItemViewType(int position) {
        // 当设置了加载中视图时直接返回加载中视图
        if (loadingView != null) {
            return loadingView.hashCode();
        }
        // 当设置了空视图且没有业务模型的时候返回空视图
        if (emptyView != null && (modelList == null || modelList.size() == 0)) {
            return emptyView.hashCode();
        }
        // 设置了header且是第一个位置，则使用header类型
        if (headerView != null && position == 0) {
            return headerView.hashCode();
        }
        // 设置了footer且是最后一个位置，则使用footer类型
        if (footerView != null && position == getItemCount() - 1) {
            return footerView.hashCode();
        }
        // 如果存在header则业务计算需要去除header所占位置
        if (headerView != null) {
            position--;
        }
        viewTypeHolder.updateViewTypeByPosition(position);
        return viewTypeHolder.getViewType(position);
    }

    @Override public int getItemCount() {
        // 如果设置了加载中视图则直接返回1
        if (loadingView != null) {
            return 1;
        }
        int itemCount = modelList == null ? 0 : modelList.size();
        // 当存在空视图的时候，如果没有具体的业务模型则返回这个空视图，否则返回包含了header和footer的视图
        if (emptyView != null && itemCount == 0) {
            return 1;
        } else {
            if (headerView != null) {
                itemCount++;
            }
            if (footerView != null) {
                itemCount++;
            }
            return itemCount;
        }
    }

    public int getModelListSize() {
        return modelList == null ? 0 : modelList.size();
    }

    @Override public long getItemId(int position) {
        if (loadingView != null) {
            return loadingView.hashCode();
        }
        if (emptyView != null && (modelList == null || modelList.size() == 0)) {
            return emptyView.hashCode();
        }
        if (headerView != null && position == 0) {
            return headerView.hashCode();
        }
        if (footerView != null && position == getItemCount() - 1) {
            return footerView.hashCode();
        }
        return headerView == null ? position : position - 1;
    }

    @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {         // 将 GridLayoutManager 单行 View 铺满
        if(recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = ((GridLayoutManager) recyclerView.getLayoutManager());
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override public int getSpanSize(int position) {
                    return  shouldFullSpanForGridLayout(position) ? gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    protected boolean shouldFullSpanForGridLayout(int position) {
        int itemViewType = getItemViewType(position);
        return (loadingView != null && itemViewType == loadingView.hashCode()) || (headerView != null && itemViewType == headerView.hashCode()) ||
                (footerView != null && itemViewType == footerView.hashCode()) || (emptyView != null && itemViewType == emptyView.hashCode());
    }

    @Override public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {      // 将 StaggeredGridLayoutManager 单行 View 铺满
        if(holder.itemView.getLayoutParams() != null && holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            if (loadingView != null) {
                layoutParams.setFullSpan(holder.getLayoutPosition() == 0);
            } else if (emptyView != null && (modelList == null || modelList.size() == 0)) {
                layoutParams.setFullSpan(holder.getLayoutPosition() == 0);
            } else {
                if (headerView != null) {
                    layoutParams.setFullSpan(holder.getLayoutPosition() == 0);
                }
                if (footerView != null) {
                    layoutParams.setFullSpan(holder.getLayoutPosition() == getItemCount() - 1);
                }
            }
        }
    }

    /**
     * 设置单机事件
     */
    public interface OnItemClickListener<M> {
        void onItemClick(ViewGroup parent, View view, int position, M model);
    }

    /**
     * 设置长按事件
     */
    public interface OnItemLongClickListener<M> {
        void onItemLongClick(ViewGroup parent, View view, int position, M model);
    }

    private class ViewTypeHolder {
        private Map<Integer, Integer> positionViewTypeMap = new HashMap<>();        // 用于记录每个位置对应的ViewType类型
        private Map<Integer, Class> viewTypeClazzMap = new HashMap<>();             // 用于记录每种ViewType对应的View类型

        void updateViewTypeByPosition(int position) {
            Class viewClazz = modelList.get(position).getViewClass();
            int viewType = viewClazz.hashCode();
            positionViewTypeMap.put(position, viewType);
            viewTypeClazzMap.put(viewType, viewClazz);
        }

        int getViewType(int position) {
            return positionViewTypeMap.get(position);
        }

        Class getViewClazzByType(int type) {
            return viewTypeClazzMap.get(type);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        static final int TAG_CLICK_INDEX = 1287368718;

        ViewHolder(View itemView) {
            super(itemView);
            UiUtils.clearParent(itemView);
        }

        public void bind(IViewModel model, int position) {
            ((IView)itemView).bind(model);
            itemView.setTag(TAG_CLICK_INDEX, position);
        }
    }

    private static class HeaderFooterEmptyLoadingViewHolder extends RecyclerView.ViewHolder {
        HeaderFooterEmptyLoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface Comparator<M> {
        public boolean areItemsTheSame(M oldItem, M newItem);
        public boolean areContentsTheSame(M oldItem, M newItem);
    }

    public static <M extends IViewModel> void notifyDataSetChanged(final RecyclerAdapter<M> adapter) {
        if (adapter.comparator == null) {
            adapter.notifyDataSetChanged();
        } else {
            final List<M> oldList = adapter.oldModleList;
            final List<M> newList = adapter.modelList;

            DiffUtil.Callback callback = new DiffUtil.Callback() {
                @Override public int getOldListSize() {
                    return oldList == null ? 0 : oldList.size();
                }
                @Override public int getNewListSize() {
                    return newList == null ? 0 : newList.size();
                }
                @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return adapter.comparator.areItemsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
                }
                @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return adapter.comparator.areContentsTheSame(oldList.get(oldItemPosition), newList.get(newItemPosition));
                }
            };
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
            diffResult.dispatchUpdatesTo(adapter);

            if (adapter.modelList != null) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    new ObjectOutputStream(baos).writeObject(adapter.modelList);
                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    adapter.oldModleList = (List<M>) new ObjectInputStream(bais).readObject();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
