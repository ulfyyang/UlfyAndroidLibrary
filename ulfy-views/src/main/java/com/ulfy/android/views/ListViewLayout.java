package com.ulfy.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ulfy.android.views.R;

import java.util.ArrayList;
import java.util.List;

public class ListViewLayout extends LinearLayout {
    public static final int DIVIDER_HEIGHT_FILL_AVALIABLE = -1; // 填充可用空间
    private BaseAdapter adapter;                                 // 适配器
    private DataSetObserver dataSetObserver;                    // 监听源数据变化
    private OnItemClickListener itemClickListener;              // 单击事件监听
    private List<FrameLayout> viewContainerList = new ArrayList<>();   // 包裹View的容器
    private List<LayoutParams> viewContainerLayoutParamsCopyList = new ArrayList<>();   // 原始布局参数拷贝，恢复时使用
    private List<View> viewList = new ArrayList<>();             // 显示业务的View
    private DividerGenerator dividerGenerator = new DefaultDividerGenerator();
    private Drawable dividerDrawable;                           // 分割线背景
    private int dividerHeight;                                  // 分割线高度
    private boolean isAverage;                                  // 布局是否平均分配
    private int placeHolderCount;                               // 占位符的数量
    private boolean layoutAutoScale;                            // 是否自动缩放子View（当容器大小不够时）

    public ListViewLayout(Context context) {
        super(context);
    }

    public ListViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListViewLayout);
        dividerDrawable = typedArray.getDrawable(R.styleable.ListViewLayout_layoutDivider);
        dividerHeight = typedArray.getLayoutDimension(R.styleable.ListViewLayout_layoutDividerHeight, 0);
        isAverage = typedArray.getBoolean(R.styleable.ListViewLayout_layoutAverage, false);
        placeHolderCount = typedArray.getInt(R.styleable.ListViewLayout_layoutPlaceHolderCount, 0);
        layoutAutoScale = typedArray.getBoolean(R.styleable.ListViewLayout_layoutAutoScale, false);
        // 如果设置了占位符，则自动设置为平均分配
        if (placeHolderCount > 0) {
            isAverage = true;
        }
        typedArray.recycle();
    }

    /**
     * 设置适配器
     */
    public ListViewLayout setAdapter(BaseAdapter adapter) {
        if (adapter == null) {
            throw new NullPointerException("Adapter may not be null");
        }
        if (this.adapter != null && this.dataSetObserver != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = adapter;
        this.dataSetObserver = new DataSetObserver() {
            public void onChanged() {
                refreshLayout();
            }
        };
        this.adapter.registerDataSetObserver(this.dataSetObserver);
        this.refreshLayout();
        return this;
    }

    /**
     * 设置占位数量
     */
    public ListViewLayout setPlaceHolderCount(int placeHolderCount) {
        if (placeHolderCount < 0) {
            placeHolderCount = 0;
        }
        if (placeHolderCount > 0) {
            isAverage = true;
        }
        this.placeHolderCount = placeHolderCount;
        return this;
    }

    /**
     * 设置单机事件
     */
    public ListViewLayout setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
        registerItemClickListenerIfNeed();
        return this;
    }

    /**
     * 设置分隔符生成器
     */
    public ListViewLayout setDividerGenerator(DividerGenerator dividerGenerator) {
        this.dividerGenerator = dividerGenerator;
        return this;
    }

    /**
     * 设置单机事件
     */
    public interface OnItemClickListener {
        void onItemClick(ListViewLayout parent, View view, int position, Object item, long itemId);
    }

    /**
     * 分隔符生成器
     */
    public interface DividerGenerator {
        View generateDivider(ListViewLayout listViewLayout, Drawable dividerDrawable);
    }

    private class DefaultDividerGenerator implements DividerGenerator {
        public View generateDivider(ListViewLayout listViewLayout, Drawable dividerDrawable) {
            if (dividerHeight == 0) {
                return null;
            } else {
                View view = new View(getContext());
                view.setBackground(dividerDrawable);
                return view;
            }
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (layoutAutoScale && getChildCount() > 0) {
            // 有可能会执行多次测量，如果因为上一次的测量设置了平均分配且大小为零，则本次测量子View有权重的大小都为零
            // 因此在测量之前需要将子View重置
            isAverage = false;
            resetContainerSizeForAverage();
            // 计算出子所有子View需要的大小
            int needSize = 0;
            for (int i = 0; i < getChildCount(); i++) {
                measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
                needSize += getOrientation() == LinearLayout.HORIZONTAL ? getChildAt(i).getMeasuredWidth() : getChildAt(i).getMeasuredHeight();
            }
            // 计算自身的大小
            int currentSize = 0;
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                currentSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
            } else {
                currentSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
            }
            // 如果容器不能满足需求的大小，让子View平均分配空间
            isAverage = needSize > currentSize;
            resetContainerSizeForAverage();
            // 当空间分配好以后，再次重新测量纠正子View的大小和位置
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 服务方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 刷新页面数据
     */
    private void refreshLayout() {
        clearAllViews();
        generateViewList();
        inflateViewListToContainier();
        resetContainerSizeForAverage();
        inflateContainerToListViewLayout();
        registerItemClickListenerIfNeed();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 服务刷新页面方法 start
    ///////////////////////////////////////////////////////////////////////////

    private void clearAllViews() {
        this.removeAllViews();
        for (View container : viewContainerList) {
            UiUtils.clearParent(container);
        }
        for (View view : viewList) {
            UiUtils.clearParent(view);
        }
    }

    private void generateViewList() {
        int count = adapter.getCount();
        List<View> tempViewList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            tempViewList.add(adapter.getView(i, i < viewList.size() ? viewList.get(i) : null, this));
        }
        viewList = tempViewList;
    }

    private void inflateViewListToContainier() {
        int needSize;
        if (placeHolderCount != 0 && viewList.size() < placeHolderCount) {
            needSize = placeHolderCount;
        } else {
            needSize = adapter.getCount();
        }
        if (viewContainerList.size() != needSize) {
            viewContainerList.clear();
            viewContainerLayoutParamsCopyList.clear();
            for (int i = 0; i < needSize; i++) {
                FrameLayout containerFL = new FrameLayout(getContext());
                setDefaultLayoutParamsIfDontHave(containerFL);
                LayoutParams layoutParamsCopy = new LayoutParams(containerFL.getLayoutParams());
                layoutParamsCopy.weight = ((LayoutParams)containerFL.getLayoutParams()).weight;
                layoutParamsCopy.gravity = ((LayoutParams)containerFL.getLayoutParams()).gravity;
                viewContainerLayoutParamsCopyList.add(layoutParamsCopy);
                viewContainerList.add(containerFL);
            }
        }
        for (int i = 0; i < viewList.size(); i++) {
            FrameLayout containerFL = viewContainerList.get(i);
            View contentV = viewList.get(i);
            containerFL.removeAllViews();
            UiUtils.clearParent(contentV);
            containerFL.addView(contentV);
        }
    }

    private void resetContainerSizeForAverage() {
        for (int i = 0; i < viewContainerList.size(); i++) {
            LayoutParams layoutParams = (LayoutParams) viewContainerList.get(i).getLayoutParams();
            if (isAverage) {
                layoutParams.weight = 1;
                if (getOrientation() == HORIZONTAL) {
                    layoutParams.width = 0;
                } else {
                    layoutParams.height = 0;
                }
            } else {
                LayoutParams layoutParamsCopy = viewContainerLayoutParamsCopyList.get(i);
                layoutParams.width = layoutParamsCopy.width;
                layoutParams.height = layoutParamsCopy.height;
                layoutParams.gravity = layoutParamsCopy.gravity;
                layoutParams.weight = layoutParamsCopy.weight;
            }
        }
    }

    private void inflateContainerToListViewLayout() {
        for (int i = 0; i < viewList.size() && i < viewContainerList.size(); i++) {
            addView(viewContainerList.get(i));
            if (i < viewList.size() - 1) {
                addDivider(false);
            }
        }
        if (placeHolderCount != 0 && viewList.size() < placeHolderCount) {
            addDivider(true);
            for (int i = viewList.size(); i < placeHolderCount && i < viewContainerList.size(); i++) {
                addView(viewContainerList.get(i));
                if (i < placeHolderCount - 1) {
                    addDivider(true);
                }
            }
        }
    }

    private void addDivider(boolean forPlaceHolder) {
        View divider = null;
        if (forPlaceHolder) {
            divider = new View(getContext());
        } else {
            divider = dividerGenerator.generateDivider(this, dividerDrawable == null ? null : dividerDrawable.getConstantState().newDrawable());
        }
        if (divider != null) {
            setDefaultLayoutParamsIfDontHave(divider);
            LayoutParams layoutParams = (LayoutParams) divider.getLayoutParams();
            if (getOrientation() == LinearLayout.VERTICAL) {
                layoutParams.width = LayoutParams.MATCH_PARENT;
                if (this.dividerHeight == DIVIDER_HEIGHT_FILL_AVALIABLE) {
                    layoutParams.weight = 1;
                    layoutParams.height = 0;
                } else {
                    layoutParams.height = this.dividerHeight;
                }
            } else {
                layoutParams.height = LayoutParams.MATCH_PARENT;
                if (this.dividerHeight == DIVIDER_HEIGHT_FILL_AVALIABLE) {
                    layoutParams.weight = 1;
                    layoutParams.width = 0;
                } else {
                    layoutParams.width = this.dividerHeight;
                }
            }
            addView(divider);
        }
    }

    private void setDefaultLayoutParamsIfDontHave(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = generateDefaultLayoutParams();
            view.setLayoutParams(layoutParams);
        }
    }

    public void setDividerHeight(int dividerHeight) {
        if (dividerHeight < 0) {
            dividerHeight = 0;
        }
        this.dividerHeight = dividerHeight;
    }

    private void registerItemClickListenerIfNeed() {
        for (int i = 0; i < viewContainerList.size(); i++) {
            if (itemClickListener == null) {
                UiUtils.setViewClickListener(viewContainerList.get(i), null);
            } else {
                UiUtils.setViewClickListener(viewContainerList.get(i), i < viewList.size() ? new OnClickListenerImpl() : null);
            }
        }
    }

    private class OnClickListenerImpl implements OnClickListener {
        public void onClick(View v) {
            int childIndex = viewContainerList.indexOf(v);
            itemClickListener.onItemClick(ListViewLayout.this, v, childIndex, adapter.getItem(childIndex), adapter.getItemId(childIndex));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 服务刷新页面方法 end
    ///////////////////////////////////////////////////////////////////////////

    @Override public LayoutParams generateDefaultLayoutParams() {
        return super.generateDefaultLayoutParams();
    }

    public List<FrameLayout> getViewContainerList() {
        return viewContainerList;
    }

    public List<View> getViewList() {
        return viewList;
    }
}
