package com.ulfy.android.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecyclerAdapter<M extends IViewModel> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ViewTypeHolder viewTypeHolder = new ViewTypeHolder();
    private List<M> modelList;
    private List<View> viewList = new ArrayList<>();
    private View headerView, footerView, emptyView, loadingView;
    private OnItemClickListener itemClickListener;

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

    public RecyclerAdapter setHeaderView(View headerView) {
        UiUtils.clearParent(headerView);
        this.headerView = headerView;
        return this;
    }

    public RecyclerAdapter setFooterView(View footerView) {
        UiUtils.clearParent(footerView);
        this.footerView = footerView;
        return this;
    }

    public RecyclerAdapter setEmptyView(View emptyView) {
        UiUtils.clearParent(emptyView);
        this.emptyView = emptyView;
        return this;
    }

    public RecyclerAdapter setLoadingView(View loadingView) {
        UiUtils.clearParent(loadingView);
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

    private void registerItemClickListenerIfNeed() {
        for (int i = 0; i < viewList.size(); i++) {
            if (itemClickListener == null) {
                UiUtils.setViewClickListener(viewList.get(i), null);
            } else {
                UiUtils.setViewClickListener(viewList.get(i), new OnClickListenerImpl());
            }
        }
    }

    private class OnClickListenerImpl implements View.OnClickListener {
        public void onClick(View v) {
            int clickIndex = (int) v.getTag(ViewHolder.TAG_CLICK_INDEX);
            itemClickListener.onItemClick((ViewGroup) v.getParent(), v, clickIndex, modelList.get(clickIndex));
        }
    }

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

    @Override public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        // 将GridLayoutManager中的header和footer铺满
        if(layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = ((GridLayoutManager) layoutManager);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override public int getSpanSize(int position) {
                    int itemViewType = getItemViewType(position);
                    return ((loadingView != null && itemViewType == loadingView.hashCode()) ||
                            (headerView != null && itemViewType == headerView.hashCode()) ||
                            (footerView != null && itemViewType == footerView.hashCode()) ||
                            (emptyView != null && itemViewType == emptyView.hashCode())) ?
                            gridLayoutManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        // 将StaggeredGridLayoutManager中的header和footer铺满
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if(layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            if (loadingView != null) {
                (((StaggeredGridLayoutManager.LayoutParams) layoutParams)).setFullSpan(holder.getLayoutPosition() == 0);
            } else if (emptyView != null && (modelList == null || modelList.size() == 0)) {
                (((StaggeredGridLayoutManager.LayoutParams) layoutParams)).setFullSpan(holder.getLayoutPosition() == 0);
            } else {
                if (headerView != null) {
                    (((StaggeredGridLayoutManager.LayoutParams) layoutParams)).setFullSpan(holder.getLayoutPosition() == 0);
                }
                if (footerView != null) {
                    (((StaggeredGridLayoutManager.LayoutParams) layoutParams)).setFullSpan(holder.getLayoutPosition() == getItemCount() - 1);
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
}
