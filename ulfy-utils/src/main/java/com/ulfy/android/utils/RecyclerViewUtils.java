package com.ulfy.android.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.adapter.RecyclerGroupAdapter;

import java.lang.reflect.Field;

public final class RecyclerViewUtils {

    /*
    =========================================  设置布局方式  =========================================
     */

    /*
    共享缓存池可以提高性能，但是在RecyclerView嵌套的页面里边共享缓存池，如果内层的Recycler有单击事件则会容易发生单击错位而引发崩溃
    因此是否共享缓存池应该由具体的业务来决定
     */

    public static LinearLayoutConfig linearLayout(RecyclerView recyclerView) {
        return linearLayout(recyclerView, false);
    }

    public static LinearLayoutConfig linearLayout(RecyclerView recyclerView, boolean sharePool) {
        LinearLayoutConfig config = new LinearLayoutConfig(recyclerView);
        initRecyclerView(recyclerView, sharePool);
        initMaxFlingVelocity(recyclerView);
        return config;
    }

    public static GridLayoutConfig gridLayout(RecyclerView recyclerView) {
        return gridLayout(recyclerView, false);
    }

    public static GridLayoutConfig gridLayout(RecyclerView recyclerView, boolean sharePool) {
        GridLayoutConfig config = new GridLayoutConfig(recyclerView);
        initRecyclerView(recyclerView, sharePool);
        initMaxFlingVelocity(recyclerView);
        return config;
    }

    public static StaggeredLayoutConfig staggeredLayout(RecyclerView recyclerView) {
        return staggeredLayout(recyclerView, false);
    }

    public static StaggeredLayoutConfig staggeredLayout(RecyclerView recyclerView, boolean sharePool) {
        StaggeredLayoutConfig config = new StaggeredLayoutConfig(recyclerView);
        initRecyclerView(recyclerView, sharePool);
        initMaxFlingVelocity(recyclerView);
        return config;
    }

    public static ViewPagerConfig viewPagerLayout(RecyclerView recyclerView) {
        return viewPagerLayout(recyclerView, false);
    }

    public static ViewPagerConfig viewPagerLayout(RecyclerView recyclerView, boolean sharePool) {
        ViewPagerConfig config = new ViewPagerConfig(recyclerView);
        initRecyclerView(recyclerView, sharePool);
        initMaxFlingVelocity(recyclerView);
        return config;
    }

    /*
    =========================================  设置缓存个数  =========================================
     */

    /**
     * 设置每种类型的最大缓存数，默认值是5（该方法针对的是有共享缓存池的RecyclerView）
     *      可以根据业务需求设置一个当前类型曾经出现的最大值或者自己猜测一个比较大的值
     *      如果这个值设置的太小则及时开启了复用也会因为可复用的View不够用而造成卡顿
     *      建议设置的数值：预计每个外层Cell中包含的子Cell数量*（预计可见的Cell外层数量+2）
     */
    public static void setMaxRecycledViews(Context context, Class viewClazz, int max) {
        RecycledViewPoolRepository.getInstance().findRecycledViewPoolFromContext(context).setMaxRecycledViews(viewClazz.hashCode(), max);
    }

    private static void initRecyclerView(RecyclerView recyclerView, boolean sharePool) {
        recyclerView.getItemAnimator().setAddDuration(0);
        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.getItemAnimator().setMoveDuration(0);
        recyclerView.getItemAnimator().setRemoveDuration(0);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        if (sharePool) {
            recyclerView.setRecycledViewPool(RecycledViewPoolRepository.getInstance().findRecycledViewPoolFromContext(recyclerView.getContext()));
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                ((LinearLayoutManager)recyclerView.getLayoutManager()).setRecycleChildrenOnDetach(true);
            }
        }
        int decorationCount = recyclerView.getItemDecorationCount();
        for (int i = 0; i < decorationCount; i++) {
            recyclerView.removeItemDecorationAt(0);     // 移除一个之后下一个就会重新变为第一个
        }
    }

    private static void initMaxFlingVelocity(RecyclerView recyclerView) {
        boolean accessible = false;
        Field field = null;
        try {
            field = recyclerView.getClass().getDeclaredField("mMaxFlingVelocity");
            accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(recyclerView, ViewConfiguration.get(recyclerView.getContext()).getScaledMaximumFlingVelocity() / 2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (field != null) {
                field.setAccessible(accessible);
            }
        }
    }


    /*
    =========================================  其它设置  =========================================
     */

    /**
     * 配置空试图
     *      这里只会控制空视图的可见性，不会移动其位置
     */
    public static void emptyView(final RecyclerView recyclerView, final RecyclerAdapter recyclerAdapter, final View emptyView) {
        recyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() {
                int modelListSize = recyclerAdapter.getModelListSize();
                recyclerView.setVisibility(modelListSize == 0 ? View.GONE : View.VISIBLE);
                emptyView.setVisibility(modelListSize == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * 当内容为空的时候隐藏指定的View
     *      使用View.GONE的方式隐藏
     */
    public static void hideWhenEmpty(final View view, final RecyclerAdapter recyclerAdapter) {
        hideWhenEmpty(view, recyclerAdapter, View.GONE);
    }

    /**
     * 当内容为空的时候隐藏指定的View
     *      自己指定隐藏方式
     */
    public static void hideWhenEmpty(final View view, final RecyclerAdapter recyclerAdapter, final int hideCode) {
        recyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() {
                int modelListSize = recyclerAdapter.getModelListSize();
                view.setVisibility(modelListSize == 0 ? hideCode : View.VISIBLE);
            }
        });
    }
    /**
     * 当内容为空的时候显示指定的View
     *      使用View.GONE的方式隐藏
     */
    public static void showWhenEmpty(final View view, final RecyclerAdapter recyclerAdapter) {
        showWhenEmpty(view, recyclerAdapter, View.GONE);
    }

    /**
     * 当内容为空的时候显示指定的View
     *      自己指定隐藏方式
     */
    public static void showWhenEmpty(final View view, final RecyclerAdapter recyclerAdapter, final int hideCode) {
        recyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() {
                int modelListSize = recyclerAdapter.getModelListSize();
                view.setVisibility(modelListSize == 0 ? View.VISIBLE : hideCode);
            }
        });
    }

    /**
     * 线性布局配置
     */
    public static class LinearLayoutConfig {
        private RecyclerView recyclerView;
        private int orientation;

        public LinearLayoutConfig(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        /**
         * 设置为横向布局
         */
        public LinearLayoutConfig horizontal() {
            recyclerView.setLayoutManager(new LinearLayoutManagerInner(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            orientation = LinearLayoutManager.HORIZONTAL;
            return this;
        }

        /**
         * 设置为纵向布局
         */
        public LinearLayoutConfig vertical() {
            recyclerView.setLayoutManager(new LinearLayoutManagerInner(recyclerView.getContext(), LinearLayoutManager.VERTICAL, false));
            orientation = LinearLayoutManager.VERTICAL;
            return this;
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public LinearLayoutConfig dividerDp(int color, float height, int headerCount, int footerCount) {
            return dividerPx(color, UiUtils.dp2px(height), headerCount, footerCount);
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public LinearLayoutConfig dividerPx(int color, float height, int headerCount, int footerCount) {
            return dividerPx(color, height, headerCount, footerCount, 0, 0);
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public LinearLayoutConfig dividerDp(int color, float height, int headerCount, int footerCount, float offsetStart, float offsetEnd) {
            return dividerPx(color, UiUtils.dp2px(height), headerCount, footerCount, UiUtils.dp2px(offsetStart), UiUtils.dp2px(offsetEnd));
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public LinearLayoutConfig dividerPx(int color, float height, int headerCount, int footerCount, float offsetStart, float offsetEnd) {
            recyclerView.addItemDecoration(new LinearItemDecoration(
                    DrawableUtils.gradientBuilder().shapeRectangle().sizePx(height, height).color(color).build(),
                    orientation, headerCount, footerCount, (int) offsetStart, (int) offsetEnd));
            return this;
        }
    }

    /**
     * 表格布局配置
     */
    public static class GridLayoutConfig {
        private RecyclerView recyclerView;

        public GridLayoutConfig(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        /**
         * 设置为纵向布局
         */
        public GridLayoutConfig horizontal(int spanCount) {
            recyclerView.setLayoutManager(new GridLayoutManagerInner(recyclerView.getContext(), spanCount, GridLayoutManager.HORIZONTAL, false));
            return this;
        }

        /**
         * 设置为纵向布局
         */
        public GridLayoutConfig vertical(int spanCount) {
            recyclerView.setLayoutManager(new GridLayoutManagerInner(recyclerView.getContext(), spanCount, GridLayoutManager.VERTICAL, false));
            return this;
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public GridLayoutConfig dividerDp(int color, float horizonLineHeight, float verticalLineHeight, int headerCount, int footerCount) {
            return dividerPx(color, UiUtils.dp2px(horizonLineHeight), UiUtils.dp2px(verticalLineHeight), headerCount, footerCount);
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public GridLayoutConfig dividerPx(int color, float horizonLineHeight, float verticalLineHeight, int headerCount, int footerCount) {
            // 添加横线
            recyclerView.addItemDecoration(new DividerGridViewItemDecoration(
                    DrawableUtils.gradientBuilder().shapeRectangle().sizePx(horizonLineHeight, horizonLineHeight).color(color).build(),
                    DividerGridViewItemDecoration.VERTICAL, headerCount, footerCount));
            // 添加竖线
            recyclerView.addItemDecoration(new DividerGridViewItemDecoration(
                    DrawableUtils.gradientBuilder().shapeRectangle().sizePx(verticalLineHeight, verticalLineHeight).color(color).build(),
                    DividerGridViewItemDecoration.HORIZONTAL, headerCount, footerCount));
            return this;
        }
    }

    /**
     * 瀑布流布局配置，目前只支持纵向且不支持 header 与分割线颜色设置
     */
    public static class StaggeredLayoutConfig {
        private RecyclerView recyclerView;

        public StaggeredLayoutConfig(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        /**
         * 设置为纵向布局
         */
        public StaggeredLayoutConfig vertical(int spanCount) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManagerInner(spanCount, StaggeredGridLayoutManager.VERTICAL));
            return this;
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public StaggeredLayoutConfig dividerDp(int color, float horizonLineHeight, float verticalLineHeight, int headerCount, int footerCount) {
            return dividerPx(color, UiUtils.dp2px(horizonLineHeight), UiUtils.dp2px(verticalLineHeight), headerCount, footerCount);
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public StaggeredLayoutConfig dividerPx(int color, float horizonLineHeight, float verticalLineHeight, int headerCount, int footerCount) {
            recyclerView.addItemDecoration(new StaggeredItemDecoration(
                    DrawableUtils.gradientBuilder().shapeRectangle().sizePx(horizonLineHeight, verticalLineHeight).color(color).build()
            ));
            return this;
        }
    }

    /**
     * ViewPager布局配置
     */
    public static class ViewPagerConfig {
        private RecyclerView recyclerView;

        public ViewPagerConfig(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        public ViewPagerConfig horizontal() {
            return horizontal(null);
        }

        public ViewPagerConfig horizontal(OnViewPagerListener onViewPagerListener) {
            recyclerView.setLayoutManager(new ViewPagerLayoutManagerInner(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, onViewPagerListener));
            return this;
        }

        public ViewPagerConfig vertical() {
            return vertical(null);
        }

        public ViewPagerConfig vertical(OnViewPagerListener onViewPagerListener) {
            recyclerView.setLayoutManager(new ViewPagerLayoutManagerInner(recyclerView.getContext(), LinearLayoutManager.VERTICAL, onViewPagerListener));
            return this;
        }
    }

    /*
    ================================= 定制布局实现支持 ===============================================
     */

    /**
     * 线性管理器定制
     */
    static class LinearLayoutManagerInner extends LinearLayoutManager {

        LinearLayoutManagerInner(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            if (getOrientation() == HORIZONTAL) {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            } else {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }

        @Override protected int getExtraLayoutSpace(RecyclerView.State state) {
            if (UtilsConfig.Config.extraLayoutSpaceMultiple <= 0) {
                return super.getExtraLayoutSpace(state);
            } else {
                return (int) (UiUtils.screenHeight() * UtilsConfig.Config.extraLayoutSpaceMultiple);
            }
        }

        @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren( recycler, state );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 表格管理器定制
     */
    static class GridLayoutManagerInner extends GridLayoutManager {

        GridLayoutManagerInner(Context context, int spanCount, int orientation, boolean reverseLayout) {
            super(context, spanCount, orientation, reverseLayout);
        }

        @Override protected int getExtraLayoutSpace(RecyclerView.State state) {
            if (UtilsConfig.Config.extraLayoutSpaceMultiple <= 0) {
                return super.getExtraLayoutSpace(state);
            } else {
                return (int) (UiUtils.screenHeight() * UtilsConfig.Config.extraLayoutSpaceMultiple);
            }
        }

        @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren( recycler, state );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * ViewPager管理器定制
     */
    static class ViewPagerLayoutManagerInner extends LinearLayoutManager {
        private PagerSnapHelper pagerSnapHelper;                // 一次滚动一页
        private int drift;                                      // 位移，用来判断移动方向
        private OnViewPagerListener onViewPagerListener;        // 相应的监听事件

        ViewPagerLayoutManagerInner(Context context, int orientation, OnViewPagerListener onViewPagerListener) {
            super(context, orientation, false);
            this.onViewPagerListener = onViewPagerListener;
            pagerSnapHelper = new PagerSnapHelper();
        }

        @Override public void onAttachedToWindow(RecyclerView view) {
            super.onAttachedToWindow(view);
            this.pagerSnapHelper.attachToRecyclerView(view);
            view.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
                @Override public void onChildViewAttachedToWindow(View view) {
                    if (onViewPagerListener != null && getChildCount() == 1) {
                        onViewPagerListener.onPageSelected(view, 0, getPosition(view), true, false);
                    }
                }
                @Override public void onChildViewDetachedFromWindow(View view) {
                    if (onViewPagerListener != null && getChildCount() > 0) {
                        int position = getPosition(view);
                        onViewPagerListener.onPageReleased(view, position % getChildCount(), position, drift >= 0);
                    }
                }
            });
        }

        @Override public void onScrollStateChanged(int state) {
            if (onViewPagerListener != null && state == RecyclerView.SCROLL_STATE_IDLE && getChildCount() == 1) {
                View view = pagerSnapHelper.findSnapView(this);
                int position = getPosition(view);
                onViewPagerListener.onPageSelected(view, position % getChildCount(), position, position == 0, position == getItemCount() - 1);
            }
            super.onScrollStateChanged(state);
        }

        @Override public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
            this.drift = dx;
            return super.scrollHorizontallyBy(dx, recycler, state);
        }

        @Override public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
            this.drift = dy;
            return super.scrollVerticallyBy(dy, recycler, state);
        }

        @Override public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren( recycler, state );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 和ViewPager相关的监听事件
     */
    public interface OnViewPagerListener {
        /**
         * 选中的监听以及判断是否滑动到底部
         * @param positionInRecyclerView    选中的位置。在Recycler中的位置
         * @param positionInData            选中的位置。在数据中的位置
         * @param first                     是否是第一个位置
         * @param last                      是否是最后一个位置
         */
        public void onPageSelected(View selectedView, int positionInRecyclerView, int positionInData, boolean first, boolean last);
        /**
         * 页面被释放的监听
         * @param positionInRecyclerView    被释放的位置。在Recycler中的位置
         * @param positionInData            被释放的位置。在数据中的位置
         * @param forward                   释放时滚动的方向，是否向前滚动
         */
        public void onPageReleased(View releasedView, int positionInRecyclerView, int positionInData, boolean forward);
    }

    /**
     * StaggeredGridLayoutManagerInner管理器定制
     */
    static class StaggeredGridLayoutManagerInner extends StaggeredGridLayoutManager {

        public StaggeredGridLayoutManagerInner(int spanCount, int orientation) {
            super(spanCount, orientation);
        }

        @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren( recycler, state );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

   /*
    ================================= 分割线实现支持 ===============================================
     */

    /**
     * Linear布局的分割线
     */
    static class LinearItemDecoration extends RecyclerView.ItemDecoration {
        public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
        public static final int VERTICAL = LinearLayout.VERTICAL;
        private Drawable divider;
        private int orientation;
        private int headerCount, footerCount;
        private int offsetStart, offsetEnd;

        LinearItemDecoration(Drawable divider, int orientation, int headerCount, int footerCount, int offsetStart, int offsetEnd) {
            this.divider = divider;
            this.orientation = orientation;
            this.headerCount = headerCount;
            this.footerCount = footerCount;
            this.offsetStart = offsetStart;
            this.offsetEnd = offsetEnd;
        }

        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (shouldDrawLayoutLine(view, parent, state)) {
                int top, left; top = left = isFirstAfterHeader(view, parent, state) ? offsetStart : 0;
                if (orientation == LinearLayoutManager.VERTICAL) {
                    outRect.set(0, top, 0, divider == null ? 0 : divider.getIntrinsicHeight());
                } else {
                    outRect.set(left, 0, divider == null ? 0 : divider.getIntrinsicWidth(), 0);
                }
            } else {
                int bottom, right; bottom = right = isLastBeforeFooter(view, parent, state) ? offsetEnd : 0;
                if (orientation == LinearLayoutManager.VERTICAL) {
                    outRect.set(0, 0, 0, bottom);
                } else {
                    outRect.set(0, 0, right, 0);
                }
            }
        }

        @Override public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (parent.getLayoutManager() != null && divider != null) {
                if (orientation == VERTICAL) {
                    drawVerticalLayoutLine(c, parent, state);
                } else {
                    drawHorizontalLayoutLine(c, parent, state);
                }
            }
        }

        /**
         * 绘制纵向布局的横线
         */
        private void drawVerticalLayoutLine(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            canvas.save();

            int left, right;        // 定义横线的左右位置
            if (parent.getClipToPadding()) {
                left = parent.getPaddingLeft(); right = parent.getWidth() - parent.getPaddingRight();
                canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
            } else {
                left = 0; right = parent.getWidth();
            }

            // 循环定义线条的上下位置并绘制分割线
            int top, bottom; Rect viewBounds = new Rect();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                if (shouldDrawLayoutLine(view, parent, state)) {
                    parent.getDecoratedBoundsWithMargins(view, viewBounds);
                    bottom = viewBounds.bottom + Math.round(view.getTranslationY());
                    top = bottom - divider.getIntrinsicHeight();
                    divider.setBounds(left, top, right, bottom); divider.draw(canvas);
                }
            }

            canvas.restore();
        }

        /**
         * 绘制横向布局的分割线
         */
        private void drawHorizontalLayoutLine(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            canvas.save();

            int top, bottom;        // 定义竖线的上下位置
            if (parent.getClipToPadding()) {
                top = parent.getPaddingTop(); bottom = parent.getHeight() - parent.getPaddingBottom();
                canvas.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
            } else {
                top = 0; bottom = parent.getHeight();
            }

            // 循环定义线条的左右位置并绘制分割线
            int left, right; Rect viewBounds = new Rect();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                if (shouldDrawLayoutLine(view, parent, state)) {
                    parent.getDecoratedBoundsWithMargins(view, viewBounds);
                    right = viewBounds.right + Math.round(view.getTranslationX());
                    left = right - divider.getIntrinsicWidth();
                    divider.setBounds(left, top, right, bottom); divider.draw(canvas);
                }
            }

            canvas.restore();
        }

        /**
         * 是否应该绘制 item 的分割线（横向时绘制 item 右侧线条，纵向时绘制 item 下方线条）
         *      不绘制 header、footer 的分割线，不绘制最后一行的分割线，不绘制分组的分割线
         *      分组涉及到两条线，一个是分组之前一项的分割线，一个是分组自身的分割线
         */
        private boolean shouldDrawLayoutLine(View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); boolean nearGroup = false;
            if (parent.getAdapter() instanceof RecyclerGroupAdapter) {
                RecyclerGroupAdapter adapter = (RecyclerGroupAdapter) parent.getAdapter();
                nearGroup = adapter.isGroupForPosition(position - headerCount) || adapter.isGroupForPosition(position - headerCount + 1);
            }
            return !nearGroup && position > headerCount - 1 && position < state.getItemCount() - footerCount - 1;
        }

        private boolean isFirstAfterHeader(View view, RecyclerView parent, RecyclerView.State state) {
            return parent.getChildAdapterPosition(view) == headerCount;
        }

        private boolean isLastBeforeFooter(View view, RecyclerView parent, RecyclerView.State state) {
            return parent.getChildAdapterPosition(view) == state.getItemCount() - footerCount - 1;
        }
    }

    /**
     * Grid布局的分割线
     */
    static class DividerGridViewItemDecoration extends RecyclerView.ItemDecoration {
        public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
        public static final int VERTICAL = LinearLayout.VERTICAL;
        private Drawable divider;
        private int orientation;                // 这里表示的是在布局方向中的线条方向，例如值 VERTICAL 在纵向布局中表示横线，在横向布局中表示竖线
        private int headerCount, footerCount;

        DividerGridViewItemDecoration(Drawable divider, int orientation, int headerCount, int footerCount) {
            this.divider = divider;
            this.orientation = orientation;
            this.headerCount = headerCount;
            this.footerCount = footerCount;
        }

        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (orientation == LinearLayoutManager.VERTICAL) {
                setVerticalLayoutItemOffset(outRect, view, parent, state);
            } else {
                setHorizontalLayoutItemOffset(outRect, view, parent, state);
            }
        }

        /**
         * 设置纵向上元素的偏移量：在外部纵向上就是横线，在外部横向上就是竖线
         */
        private void setVerticalLayoutItemOffset(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                if (shouldDrawVerticalLayoutLine(view, parent, state)) {
                    outRect.set(0, 0, 0, divider.getIntrinsicHeight());
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            } else {
                if (shouldDrawVerticalLayoutLine(view, parent, state)) {
                    outRect.set(0, 0, divider.getIntrinsicHeight(), 0);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            }
        }

        /**
         * 设置横向元素上的偏移量：在外部纵向时就是竖线，在外部横向时就是横线
         */
        private void setHorizontalLayoutItemOffset(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                if (shouldDrawHorizontalLayoutLine(view, parent, state)) {
                    int totalColum = getSpanCount(parent);
                    int currentColum = findColumnIndexForView(view, parent);
                    int left = divider.getIntrinsicWidth() * (currentColum - 1) / totalColum;
                    int right = divider.getIntrinsicWidth() * (totalColum - currentColum) / totalColum;
                    /*
                    在分割线大小设置不为 0 的情况下：中间的分割线最少保证分割线有 1 像素
                            第一个奇数位放右边，最后一个奇数放左边，剩余奇数位放两边
                            或者：不是第一个奇数放左边、不是最后一个奇数放右边
                     */
                    if (divider.getIntrinsicWidth() != 0 && left == 0 && right == 0) {
                        if (currentColum == 1) {
                            right = 1;
                        } else if (currentColum == totalColum && currentColum % 2 == 1) {
                            left = 1;
                        } else if (currentColum % 2 == 1) {
                            left = right = 1;
                        }
                    }
                    outRect.set(left, 0, right, 0);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            } else {
                if (shouldDrawHorizontalLayoutLine(view, parent, state)) {
                    int totalColum = getSpanCount(parent);
                    int currentColum = findColumnIndexForView(view, parent);
                    int top = divider.getIntrinsicWidth() * (currentColum - 1) / totalColum;
                    int bottom = divider.getIntrinsicWidth() * (totalColum - currentColum) / totalColum;
                    /*
                    在分割线大小设置不为 0 的情况下：中间的分割线最少保证分割线有 1 像素
                            第一个奇数位放下边，最后一个奇数放上边，剩余奇数位放两边
                            或者：不是第一个奇数放下边、不是最后一个奇数放上边
                     */
                    if (divider.getIntrinsicWidth() != 0 && top == 0 && bottom == 0) {
                        if (currentColum == 1) {
                            bottom = 1;
                        } else if (currentColum == totalColum && currentColum % 2 == 1) {
                            top = 1;
                        } else if (currentColum % 2 == 1) {
                            top = bottom = 1;
                        }
                    }
                    outRect.set(0, top, 0, bottom);
                } else {
                    outRect.set(0, 0, 0, 0);
                }
            }
        }

        @Override public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (parent.getLayoutManager() != null && divider != null) {
                if (orientation == VERTICAL) {
                    drawVerticalLayoutLine(c, parent, state);
                } else {
                    drawHorizontalLayoutLine(c, parent, state);
                }
            }
        }

        /**
         * 绘制纵向布局的线条：在外部纵向时就是横线，在外部横向时就是竖线
         */
        private void drawVerticalLayoutLine(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            canvas.save();
            if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                int left, right;        // 定义横线的左右位置。去除内边距
                if (parent.getClipToPadding()) {
                    left = parent.getPaddingLeft();
                    right = parent.getWidth() - parent.getPaddingRight();
                    canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
                } else {
                    left = 0; right = parent.getWidth();
                }
                int top, bottom; Rect viewBounds = new Rect();      // 循环绘制每个子 View 的分割线
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View view = parent.getChildAt(i);
                    if (shouldDrawVerticalLayoutLine(view, parent, state)) {
                        parent.getDecoratedBoundsWithMargins(view, viewBounds);
                        bottom = viewBounds.bottom + Math.round(view.getTranslationY());
                        top = bottom - divider.getIntrinsicHeight();
                        divider.setBounds(left, top, right, bottom); divider.draw(canvas);
                    }
                }
            } else {
                int top, bottom;        // 定义竖线的上下位置。去除内边距
                if (parent.getClipToPadding()) {
                    top = parent.getPaddingTop();
                    bottom = parent.getHeight() - parent.getPaddingBottom();
                    canvas.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
                } else {
                    top = 0;
                    bottom = parent.getHeight();
                }
                int left, right; Rect viewBounds = new Rect();      // 循环绘制每个子 View 的分割线
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View view = parent.getChildAt(i);
                    if (shouldDrawVerticalLayoutLine(view, parent, state)) {
                        parent.getDecoratedBoundsWithMargins(view, viewBounds);
                        right = viewBounds.right + Math.round(view.getTranslationX());
                        left = right - divider.getIntrinsicWidth();
                        divider.setBounds(left, top, right, bottom);
                        divider.draw(canvas);
                    }
                }
            }
            canvas.restore();
        }

        /**
         * 绘制横向布局的线条：在外部纵向时就是竖线，在外部横向时就是横线
         */
        private void drawHorizontalLayoutLine(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            canvas.save();
            if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                int top, bottom;        // 定义竖线的上下位置。去除内边距
                if (parent.getClipToPadding()) {
                    top = parent.getPaddingTop();
                    bottom = parent.getHeight() - parent.getPaddingBottom();
                    canvas.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
                } else {
                    top = 0;
                    bottom = parent.getHeight();
                }
                if (parent.getAdapter() instanceof RecyclerAdapter) {
                    RecyclerAdapter adapter = (RecyclerAdapter) parent.getAdapter();
                    if (parent.indexOfChild(adapter.getHeaderView()) != -1) {
                        top = getFirstLineTop(parent);
                    }
                    if (parent.indexOfChild(adapter.getFooterView()) != -1) {
                        bottom = getLastLineBottom(parent);
                    }
                }
                int left, right; Rect viewBounds = new Rect();      // 循环绘制每个子 View 的分割线
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View view = parent.getChildAt(i);
                    if (shouldDrawHorizontalLayoutLine(view, parent, state)) {
                        parent.getDecoratedBoundsWithMargins(view, viewBounds);
                        int totalColum = getSpanCount(parent);
                        int currentColum = findColumnIndexForView(view, parent);
                        // 绘制左半边
                        left = viewBounds.left + Math.round(view.getTranslationX());
                        right = left + divider.getIntrinsicWidth() * (currentColum - 1) / totalColum;
                        if (left == right && divider.getIntrinsicWidth() != 0) {
                            if (currentColum != 1 && currentColum % 2 == 1) {
                                right = left + 1;
                            }
                        }
                        divider.setBounds(left, top, right, bottom); divider.draw(canvas);
                        // 绘制右半边
                        right = viewBounds.right + Math.round(view.getTranslationX());
                        left = right - divider.getIntrinsicWidth() * (totalColum - currentColum) / totalColum;
                        if (left == right && divider.getIntrinsicWidth() != 0) {
                            if (currentColum != totalColum && currentColum % 2 == 1) {
                                left = right - 1;
                            }
                        }
                        divider.setBounds(left, top, right, bottom); divider.draw(canvas);
                    }
                }
            } else {
                int left, right;        // 定义横线的左右位置。去除内边距
                if (parent.getClipToPadding()) {
                    left = parent.getPaddingLeft();
                    right = parent.getWidth() - parent.getPaddingRight();
                    canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
                } else {
                    left = 0; right = parent.getWidth();
                }
                if (parent.getAdapter() instanceof RecyclerAdapter) {
                    RecyclerAdapter adapter = (RecyclerAdapter) parent.getAdapter();
                    if (parent.indexOfChild(adapter.getHeaderView()) != -1) {
                        left = getFirstLineTop(parent);
                    }
                    if (parent.indexOfChild(adapter.getFooterView()) != -1) {
                        right = getLastLineBottom(parent);
                    }
                }
                int top, bottom; Rect viewBounds = new Rect();      // 循环绘制每个子 View 的分割线
                for (int i = 0; i < parent.getChildCount(); i++) {
                    View view = parent.getChildAt(i);
                    if (shouldDrawHorizontalLayoutLine(view, parent, state)) {
                        parent.getDecoratedBoundsWithMargins(view, viewBounds);
                        int totalLine = getSpanCount(parent);
                        int currentLine = findColumnIndexForView(view, parent);
                        // 绘制上半边
                        top = viewBounds.top + Math.round(view.getTranslationY());
                        bottom = top + divider.getIntrinsicHeight() * (currentLine - 1) / totalLine;
                        if (top == bottom && divider.getIntrinsicHeight() != 0) {
                            if (currentLine != 1 && currentLine % 2 == 1) {
                                bottom = top + 1;
                            }
                        }
                        divider.setBounds(left, top, right, bottom); divider.draw(canvas);
                        // 绘制下半边
                        bottom = viewBounds.bottom + Math.round(view.getTranslationY());
                        top = bottom - divider.getIntrinsicHeight() * (totalLine - currentLine) / totalLine;
                        if (top == bottom && divider.getIntrinsicHeight() != 0) {
                            if (currentLine != totalLine && currentLine % 2 == 1) {
                                top = bottom - 1;
                            }
                        }
                        divider.setBounds(left, top, right, bottom); divider.draw(canvas);
                    }
                }
            }
            canvas.restore();
        }

        /**
         * 排除 header 以后，从最前一排中找到最顶的位置
         *      如果布局是纵向的则返回顶部位置
         *      如果布局是横向的则返回左侧位置
         */
        private int getFirstLineTop(RecyclerView parent) {
            // 需要预先准备好的状态：header、footer 是否正在显示中，内容中最前一行有几个
            boolean headerShowing = false, footerShowing = false; int firstRowCount = 0;
            if (parent.getAdapter() instanceof RecyclerAdapter) {
                RecyclerAdapter adapter = (RecyclerAdapter) parent.getAdapter();
                headerShowing = parent.indexOfChild(adapter.getHeaderView()) != -1;
                footerShowing = parent.indexOfChild(adapter.getFooterView()) != -1;
            }
            if (parent.getAdapter() instanceof RecyclerGroupAdapter) {
                RecyclerGroupAdapter adapter = (RecyclerGroupAdapter) parent.getAdapter();
                firstRowCount = adapter.getFirstRowCountInAllGroup(getSpanCount(parent));
            } else {
                firstRowCount = getSpanCount(parent);
            }

            // 计算循环的开始位置和结束位置
            int startIndex = 0;
            if (headerShowing) {
                startIndex ++;
            }
            int endIndex = startIndex + firstRowCount;
            if (footerShowing && endIndex > parent.getChildCount() - 1) {
                endIndex = parent.getChildCount() - 1;
            }

            // 循环最前一行，找到最顶的值
            int topY = 0; Rect childViewBounds = new Rect();
            for (int i = startIndex; i < endIndex; i++) {
                View view = parent.getChildAt(i);
                if (view != null) {
                    parent.getDecoratedBoundsWithMargins(view, childViewBounds);
                    if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                        topY = (int) Math.max(topY, childViewBounds.top + view.getTranslationY());
                    } else {
                        topY = (int) Math.max(topY, childViewBounds.left + view.getTranslationX());
                    }
                }
            }
            return topY;
        }

        /**
         * 排除 footer 以后，从最后一排中找到最底的位置
         *      如果布局是纵向的则返回底部位置
         *      如果布局是横向的则返回右侧位置
         */
        private int getLastLineBottom(RecyclerView parent) {
            // 需要预先准备好的状态：header、footer 是否正在显示中，内容中最后一行有几个
            boolean headerShowing = false, footerShowing = false; int lastRowCount = 0;
            if (parent.getAdapter() instanceof RecyclerAdapter) {
                RecyclerAdapter adapter = (RecyclerAdapter) parent.getAdapter();
                headerShowing = parent.indexOfChild(adapter.getHeaderView()) != -1;
                footerShowing = parent.indexOfChild(adapter.getFooterView()) != -1;
            }
            if (parent.getAdapter() instanceof RecyclerGroupAdapter) {
                RecyclerGroupAdapter adapter = (RecyclerGroupAdapter) parent.getAdapter();
                lastRowCount = adapter.getLastRowCountInAllGroup(getSpanCount(parent));
            } else {
                lastRowCount = getSpanCount(parent);
            }

            // 计算循环的开始位置和结束位置
            int startIndex = parent.getChildCount() - 1;
            if (footerShowing) {
                startIndex --;
            }
            int endIndex = startIndex - lastRowCount;
            if (headerShowing && endIndex < 0) {
                endIndex = 0;
            }

            // 循环最后一行，找到最底的值
            int bottomY = 0; Rect childViewBounds = new Rect();
            for (int i = startIndex; i > endIndex; i--) {
                View view = parent.getChildAt(i);
                if (view != null) {
                    parent.getDecoratedBoundsWithMargins(view, childViewBounds);
                    if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                        bottomY = (int) Math.max(bottomY, childViewBounds.bottom + view.getTranslationY());
                    } else {
                        bottomY = (int) Math.max(bottomY, childViewBounds.right + view.getTranslationX());
                    }
                }
            }
            return bottomY;
        }

        /**
         * 是否应该绘制 item 的分割线（纵向时绘制 item 下方线条，横向时绘制 item 右侧线条；纵向布局绘制横线，横向布局绘制竖线）
         *      不绘制 header、footer 的分割线，不绘制最后一行的分割线，不绘制分组的分割线
         *      分组涉及到两条线，一个是分组自身的分割线，另一个是当前组最后一排的分割线
         */
        private boolean shouldDrawVerticalLayoutLine(View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); boolean nearGroup = false;
            if (parent.getAdapter() instanceof RecyclerGroupAdapter) {
                RecyclerGroupAdapter adapter = (RecyclerGroupAdapter) parent.getAdapter();
                nearGroup = adapter.isGroupForPosition(position - headerCount) || adapter.isLastRowInItsGroup(position - headerCount, getSpanCount(parent));
            }
            return !nearGroup && position > headerCount - 1 && position < state.getItemCount() - footerCount && !isLastRowInContent(view, parent, state);
        }

        /**
         * 是否应该绘制 item 的分割线（纵向时绘制 item 右侧线条，横向时绘制 item 下方线条；纵向布局绘制竖线，横向布局绘制横线）
         *      不绘制 header、footer 的分割线，不绘制最后一行的分割线，不绘制分组的分割线
         *      分组涉及到两条线，一个是分组自身的分割线，另一个是当前组最后一排的分割线
         */
        private boolean shouldDrawHorizontalLayoutLine(View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); boolean isGroup = false;
            if (parent.getAdapter() instanceof RecyclerGroupAdapter) {
                RecyclerGroupAdapter adapter = (RecyclerGroupAdapter) parent.getAdapter();
                isGroup = adapter.isGroupForPosition(position - headerCount);         // 对于竖线来说，组的最后一行是允许画线的
            }
            return !isGroup && position > headerCount - 1 && position < state.getItemCount() - footerCount;
        }

        /**
         * 是否是内容区域的最后一行（排除掉 header 和 footer）
         */
        private boolean isLastRowInContent(View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view) - headerCount;      // 计算时去除 header 的数量
            int spanCount = getSpanCount(parent);
            if (parent.getAdapter() instanceof RecyclerGroupAdapter) {
                RecyclerGroupAdapter adapter = (RecyclerGroupAdapter) parent.getAdapter();
                return adapter.isLastRowInAllGroup(position, spanCount);
            } else {
                int totalCount = state.getItemCount() - headerCount - footerCount;      // 计算时去除 header、footer 的数量
                int totalRow = totalCount / spanCount; if (totalCount % spanCount > 0) totalRow ++;
                int positionRow = (position + 1) / spanCount; if ((position + 1) % spanCount > 0) positionRow ++;
                return positionRow == totalRow;
            }
        }

        /**
         * 获取当前 View 所在 Span 中的位置，在纵向上就表现为当前的 item 在当前行中处于第几列，从 1 开始算起
         */
        private int findColumnIndexForView(View view, RecyclerView parent) {
            int position = parent.getChildAdapterPosition(view) - headerCount;   // 计算时去除 header 的数量
            int spanCount = getSpanCount(parent);
            if (parent.getAdapter() instanceof RecyclerGroupAdapter) {
                RecyclerGroupAdapter adapter = (RecyclerGroupAdapter) parent.getAdapter();
                return adapter.findColumnIndexForPosition(position, spanCount);
            } else {
                int column = position % spanCount + 1;  // +1 是为了从 1 开始计数
                if (column == 0) {                      // 如果是最后一项，则这时候取余会等于零，这种情况应该矫正为最后一项
                    column = spanCount;
                }
                return column;
            }
        }

        /**
         * 获取行或列的分割数量
         */
        private int getSpanCount(RecyclerView parent) {
            return ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
        }
    }

    /**
     * Staggered布局的分割线
     */
    static class StaggeredItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable divider;

        StaggeredItemDecoration(Drawable divider) {
            this.divider = divider;
        }

        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            StaggeredGridLayoutManager manager = (StaggeredGridLayoutManager) parent.getLayoutManager();
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            int position = parent.getChildAdapterPosition(view);
            // 计算边距
            int totalColum = manager.getSpanCount(); int currentColum = params.getSpanIndex() + 1;
            int top = position < totalColum ? 0 : divider.getIntrinsicHeight();
            int left = divider.getIntrinsicWidth() * (currentColum - 1) / totalColum;
            int right = divider.getIntrinsicWidth() * (totalColum - currentColum) / totalColum;
            /*
            在分割线大小设置不为 0 的情况下：中间的分割线最少保证分割线有 1 像素
                    第一个奇数位放下边，最后一个奇数放上边，剩余奇数位放两边
                    或者：不是第一个奇数放下边、不是最后一个奇数放上边
             */
            if (divider.getIntrinsicWidth() != 0 && left == 0 && right == 0) {
                if (currentColum == 1) {
                    right = 1;
                } else if (currentColum == totalColum && currentColum % 2 == 1) {
                    left = 1;
                } else if (currentColum % 2 == 1) {
                    left = right = 1;
                }
            }
            outRect.set(left, top, right, 0);
        }
    }
}
