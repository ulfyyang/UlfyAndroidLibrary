package com.ulfy.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ulfy.android.views.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 流式布局
 */
public final class FlowLayout extends ViewGroup {
    private BaseAdapter adapter;                                         // 适配器
    private DataSetObserver dataSetObserver;                            // 监听源数据变化
    private OnItemClickListener itemClickListener;       // 单击事件监听
    private List<List<View>> groupViewList = new ArrayList<>();
    private int childMargin;

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        childMargin = (int) typedArray.getDimension(R.styleable.FlowLayout_child_margin, 0);
        typedArray.recycle();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取本身的大小数据
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // 重置View集合
        groupViewList.clear();

        /*
        统计当需要包裹内容时的宽和高
            目标：统计出最宽的行宽，统计出所有行高的和

        统计最宽的行
            确定当前行宽的方式
                尝试累加到当前行宽，如果超出可用宽则累加失败并可确认行宽
            使用一个值记录当前存在最宽行宽

        统计所有行高的和
            确定行高的方式
                使用一个值记录当前行的最大高
                当行宽确定后，该值即为行高
            确定行高后累加
         */

        int availableWidth = sizeWidth - getPaddingLeft() - getPaddingRight();
        int maxLineWidth = 0, totalLineHeight = 0;
        int childIndex = 0, currentWidthIndex = 0, currentHeightIndex = 0;
        int currentLineWidth = 0, currentLineHeight = 0;

        for (childIndex = 0; childIndex < getChildCount(); childIndex++) {
            // 跳过不显示的View
            if (getChildAt(childIndex).getVisibility() == View.GONE) {
                continue;
            }
            // 对子View进行测量
            measureChild(getChildAt(childIndex), widthMeasureSpec, heightMeasureSpec);
            int childNeedWidth = getChildNeedWidth(getChildAt(childIndex), currentWidthIndex);
            int childNeedHeight = getChildNeedHeight(getChildAt(childIndex), currentHeightIndex);
            /*
            尝试将控件添加到当前行，如果能添加到当前行
                更新当前行宽
                更新当前行高
             */
            if (currentLineWidth + childNeedWidth <= availableWidth) {
                currentLineWidth += childNeedWidth;
                currentLineHeight = Math.max(currentLineHeight, childNeedHeight);
                currentWidthIndex++;
                getViewGroupByHeightIndex(currentHeightIndex).add(getChildAt(childIndex));
            }
            /*
            添加到当前行失败（当前行已确定）
                更新最大宽度
                累加高度之和
                重置当前行宽和行高，为下一行统计做准备
                因为添加失败，所以当前View并没有被处理，索引应该回退
             */
            else {
                maxLineWidth = Math.max(maxLineWidth, currentLineWidth);
                totalLineHeight += currentLineHeight;
                currentLineWidth = currentLineHeight = 0;
                childIndex--; currentWidthIndex = 0; currentHeightIndex++;
            }

            /*
            上面统计的只是因为超出可用宽度而产生的行的宽和高
                通常最后一行无法超出可用宽度，因此数据不会被归纳到目标宽和高中
                当统计到最后一行的最后一个时表示最后一行已确定，将处理结果归纳
             */
            if (childIndex == getChildCount() - 1) {
                maxLineWidth = Math.max(maxLineWidth, currentLineWidth);
                totalLineHeight += currentLineHeight;
            }
        }

        /*
        最后汇总一下
         */
        int totalNeedWidth = maxLineWidth + getPaddingLeft() + getPaddingRight();
        int totalNeedHeight = totalLineHeight + getPaddingTop() + getPaddingBottom();

        setMeasuredDimension(
                (modeWidth == MeasureSpec.AT_MOST || modeWidth == MeasureSpec.UNSPECIFIED) ?  totalNeedWidth: sizeWidth,
                (modeHeight == MeasureSpec.AT_MOST || modeHeight == MeasureSpec.UNSPECIFIED) ?  totalNeedHeight: sizeHeight);
    }

    private int getChildNeedWidth(View childView, int currentWidthIndex) {
        MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
        int childViewNeedWidth = lp.leftMargin + childView.getMeasuredWidth() + lp.rightMargin;
        // 每一行的第一个不需要公共宽度，剩余的只需要左边一份公共宽度
        return childViewNeedWidth + (currentWidthIndex == 0 ?  0 : childMargin);
    }

    private int getChildNeedHeight(View childView, int currentHeightIndex) {
        MarginLayoutParams lp = (MarginLayoutParams) childView.getLayoutParams();
        int childNeedHeight = lp.topMargin + childView.getMeasuredHeight() + lp.bottomMargin;
        // 第一行不需要公共高度，剩余的只需要上边一份公共宽度
        return childNeedHeight + (currentHeightIndex == 0 ? 0 : childMargin);
    }

    private List<View> getViewGroupByHeightIndex(int currentHeightIndex) {
        if (currentHeightIndex >= groupViewList.size()) {
            groupViewList.add(new ArrayList<View>());
        }
        return groupViewList.get(currentHeightIndex);
    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 记录布局过程中的位置移动
        int leftPosition = getPaddingLeft(), topPosition = getPaddingTop();
        // 开始布局
        for (List<View> viewList : groupViewList) {
            // 记录每行的行高
            int currentLineHeight = 0;
            // 填充每一行
            for (View view : viewList) {
                // 布局View
                MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
                int left =  leftPosition + lp.leftMargin;
                int top =  topPosition + lp.topMargin;
                int right = left + view.getMeasuredWidth() + lp.rightMargin;
                int bottom = top + view.getMeasuredHeight() + lp.bottomMargin;
                view.layout(left, top, right, bottom);
                // 更新位置移动
                leftPosition = right + childMargin;
                currentLineHeight = Math.max(currentLineHeight, lp.topMargin + view.getMeasuredHeight() + lp.bottomMargin);
            }
            // 更新位置移动
            leftPosition = getPaddingLeft();
            topPosition += currentLineHeight + childMargin;
        }

    }

    /**
     * 设置适配器
     */
    public FlowLayout setAdapter(BaseAdapter adapter) {
        if (adapter == null) {
            throw new NullPointerException("Adapter may not be null");
        }
        if (this.adapter != null && this.dataSetObserver != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = adapter;
        this.dataSetObserver = new DataSetObserver() {
            public void onChanged() {
                refreshAdapterView();
            }
        };
        this.adapter.registerDataSetObserver(this.dataSetObserver);
        this.refreshAdapterView();
        return this;
    }

    private void refreshAdapterView() {
        if (getChildCount() == adapter.getCount()) {
            for (int i = 0; i < adapter.getCount(); i++) {
                View childView = getChildAt(i);
                if (i < getChildCount()) {
                    removeViewAt(i);
                }
                addView(adapter.getView(i, childView, this), i);
            }
        } else {
            removeAllViews();
            for (int i = 0; i < adapter.getCount(); i++) {
                addView(adapter.getView(i, null, this));
            }
        }
        registerItemClickListenerIfNeed();
    }

    /**
     * 设置单机事件
     */
    public FlowLayout setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
        registerItemClickListenerIfNeed();
        return this;
    }

    /**
     * 设置单机事件
     */
    public interface OnItemClickListener {
        void onItemClick(FlowLayout parent, View view, int position, Object item, long itemId);
    }

    private void registerItemClickListenerIfNeed() {
        for (int i = 0; i < getChildCount(); i++) {
            UiUtils.setViewClickListener(getChildAt(i), itemClickListener == null ? null : new OnClickListenerImpl());
        }
    }

    private class OnClickListenerImpl implements OnClickListener {
        public void onClick(View v) {
            int childIndex = indexOfChild(v);
            itemClickListener.onItemClick(FlowLayout.this, v, childIndex, adapter.getItem(childIndex), adapter.getItemId(childIndex));
        }
    }

    @Override public void onViewAdded(View child) {
        if (!(child.getLayoutParams() instanceof MarginLayoutParams)) {
            child.setLayoutParams(generateLayoutParams(child.getLayoutParams()));
        }
    }

    // 在布局文件中使用的布局参数
    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    // 根据已有的布局参数创建布局参数
    @Override protected LayoutParams generateLayoutParams(LayoutParams p) {
        return p == null ? generateDefaultLayoutParams() : new MarginLayoutParams(p);
    }

    // 当没有设置布局参数时默认提供的布局参数
    @Override protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }
}
