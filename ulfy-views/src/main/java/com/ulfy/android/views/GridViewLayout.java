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

public class GridViewLayout extends LinearLayout {
    public static final int DIVIDER_HEIGHT_FILL_AVALIABLE = -1; // 填充可用空间
    private BaseAdapter adapter;                             // 适配器
    private DataSetObserver dataSetObserver;                // 监听源数据变化
    private OnItemClickListener itemClickListener;          // 单击事件
    private List<FrameLayout> viewContainerList = new ArrayList<>();   // 包裹View的容器
    private List<View> viewList = new ArrayList<>();        // 其中存放的元素，后续会根据这个进行分组
    private DividerGenerator dividerGenerator = new DefaultDividerGenerator();
    private Drawable dividerDrawable;                       // 分割线背景
    private int dividerHeight;                              // 分割线高度
    private int columnCount;                                // 每行的数量

    public GridViewLayout(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
    }

    public GridViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GridViewLayout);
        columnCount = typedArray.getInt(R.styleable.GridViewLayout_layoutColumnCount, 1);
        if (columnCount < 1) {
            columnCount = 1;
        }
        dividerDrawable = typedArray.getDrawable(R.styleable.GridViewLayout_layoutDivider);
        dividerHeight = typedArray.getDimensionPixelSize(R.styleable.GridViewLayout_layoutDividerHeight, 0);
        typedArray.recycle();
    }

    public GridViewLayout setAdapter(BaseAdapter adapter) {
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

    public GridViewLayout setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
        registerItemClickListenerIfNeed();
        return this;
    }

    public GridViewLayout setColumnCount(int columnCount) {
        if (columnCount < 1) {
            columnCount = 1;
        }
        this.columnCount = columnCount;
        return this;
    }

    public interface OnItemClickListener {
        void onItemClick(LinearLayout parent, View view, int position, Object item, long itemId);
    }

    /**
     * 分隔符生成器
     */
    public interface DividerGenerator {
        View generateDivider(GridViewLayout gridViewLayout, Drawable dividerDrawable);
    }

    private class DefaultDividerGenerator implements DividerGenerator {
        public View generateDivider(GridViewLayout gridViewLayout, Drawable dividerDrawable) {
            if (dividerHeight == 0) {
                return null;
            } else {
                View view = new View(getContext());
                view.setBackground(dividerDrawable);
                return view;
            }
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
        inflateViewListToContainer();
        resetContainerSizeForAverage();
        inflateContainerToGridViewLayout();
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

    private void inflateViewListToContainer() {
        int line = adapter.getCount() % columnCount == 0 ? adapter.getCount() / columnCount : adapter.getCount() / columnCount + 1;
        int needSize = line * columnCount;
        if (viewContainerList.size() != needSize) {
            viewContainerList.clear();
            for (int i = 0; i < needSize; i++) {
                FrameLayout containerFL = new FrameLayout(getContext());
                setDefaultLayoutParamsIfDontHave(containerFL);
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
        for (FrameLayout containerFL : viewContainerList) {
            LayoutParams layoutParams = (LayoutParams) containerFL.getLayoutParams();
            layoutParams.weight = 1;
            layoutParams.width = 0;     // 放到横向容器中，因此宽为0
        }
    }

    private void inflateContainerToGridViewLayout() {
        int line = adapter.getCount() % columnCount == 0 ? adapter.getCount() / columnCount : adapter.getCount() / columnCount + 1;
        for (int i = 0; i < line; i++) {
            addView(inflateContainerToLinearLayout(i));
            if (i < line - 1) {
                addDivider(this, false);
            }
        }
    }

    private LinearLayout inflateContainerToLinearLayout(int lineIndex) {
        // 生成一个行布局
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        // 循环从容器列表中取出添加到行布局中
        for (int i = 0, containerIndex = lineIndex * columnCount + i;                // 初始设置
             i < columnCount && containerIndex < viewContainerList.size();          // 满足条件
             i++, containerIndex = lineIndex * columnCount + i) {                     // 每次修改
            linearLayout.addView(viewContainerList.get(containerIndex));
            // 如果不是该行的最后一个则添加分隔符
            if (i < columnCount - 1) {
                // 如果超出了容器下标则添加空分隔符，在容器列表内添加设置的分隔符
                addDivider(linearLayout, containerIndex >= viewList.size() - 1);
            }
        }
        return linearLayout;
    }

    private void addDivider(LinearLayout linearLayout, boolean forPlaceHolder) {
        View divider = null;
        if (forPlaceHolder) {
            divider = new View(getContext());
        } else {
            divider = dividerGenerator.generateDivider(this, dividerDrawable == null ? null : dividerDrawable.getConstantState().newDrawable());
        }
        if (divider != null) {
            setDefaultLayoutParamsIfDontHave(divider);
            LayoutParams layoutParams = (LayoutParams) divider.getLayoutParams();
            if (linearLayout.getOrientation() == LinearLayout.VERTICAL) {
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
            linearLayout.addView(divider);
        }
    }

    private void setDefaultLayoutParamsIfDontHave(View view) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = generateDefaultLayoutParams();
            view.setLayoutParams(layoutParams);
        }
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
            itemClickListener.onItemClick(GridViewLayout.this, v, childIndex, adapter.getItem(childIndex), adapter.getItemId(childIndex));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // 服务刷新页面方法 end
    ///////////////////////////////////////////////////////////////////////////

    @Override public LayoutParams generateDefaultLayoutParams() {
        return super.generateDefaultLayoutParams();
    }
}
