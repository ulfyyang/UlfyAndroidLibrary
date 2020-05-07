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
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ulfy.android.adapter.RecyclerAdapter;

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
    }

    private static void initMaxFlingVelocity(RecyclerView recyclerView) {
        boolean accessible = false;
        Field field = null;
        try {
            field = recyclerView.getClass().getDeclaredField("mMaxFlingVelocity");
            accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(recyclerView, recyclerView.getMaxFlingVelocity() / 2);
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
        public LinearLayoutConfig dividerDp(int color, int height, int headerCount, int footerCount) {
            return dividerPx(color, UiUtils.dp2px(height), headerCount, footerCount);
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public LinearLayoutConfig dividerPx(int color, int height, int headerCount, int footerCount) {
            recyclerView.addItemDecoration(new LinearItemDecoration(
                    DrawableUtils.gradientBuilder().shapeRectangle().sizePx(height, height).color(color).build(),
                    orientation, headerCount, footerCount));
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
        public GridLayoutConfig dividerDp(int color,  int horizonLineHeight, int verticalLineHeight, int headerCount, int footerCount) {
            return dividerPx(color, UiUtils.dp2px(horizonLineHeight), UiUtils.dp2px(verticalLineHeight), headerCount, footerCount);
        }

        /**
         * 设置分割线
         *      headerCount、footerCount必须设置，否则布局会错乱
         *      添加了RecyclerView版的上拉加载会自动添加一个footer，因此需要把这个footer也算在其中
         */
        public GridLayoutConfig dividerPx(int color,  int horizonLineHeight, int verticalLineHeight, int headerCount, int footerCount) {
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
     * 瀑布流布局配置
     */
    public static class StaggeredLayoutConfig {
        private RecyclerView recyclerView;

        public StaggeredLayoutConfig(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        /**
         * 设置为横向布局
         */
        public StaggeredLayoutConfig horizontal(int spanCount) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.HORIZONTAL));
            return this;
        }

        /**
         * 设置为纵向布局
         */
        public StaggeredLayoutConfig vertical(int spanCount) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
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
                    if (onViewPagerListener != null) {
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

   /*
    ================================= 分割线实现支持 ===============================================
     */

    /**
     * Linear布局的分割线
     */
    static class LinearItemDecoration extends RecyclerView.ItemDecoration {
        public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
        public static final int VERTICAL = LinearLayout.VERTICAL;
        private Drawable diviter;
        private int orientation;
        private int headerCount, footerCount;

        LinearItemDecoration(Drawable divider, int orientation, int headerCount, int footerCount) {
            this.diviter = divider;
            this.orientation = orientation;
            this.headerCount = headerCount;
            this.footerCount = footerCount;
        }

        @Override public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            if (parent.getLayoutManager() != null && diviter != null) {
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

            // 定义横线的左右位置
            int left, right;
            if (parent.getClipToPadding()) {
                left = parent.getPaddingLeft();
                right = parent.getWidth() - parent.getPaddingRight();
                canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
            } else {
                left = 0;
                right = parent.getWidth();
            }

            // 循环定义线条的上下位置并绘制分割线
            int top, bottom;
            Rect viewBounds = new Rect();
            for (int i = 0; i < parent.getChildCount(); i++) {
                final View view = parent.getChildAt(i);
                if (shouldDrawLayoutLine(view, parent, state)) {
                    parent.getDecoratedBoundsWithMargins(view, viewBounds);
                    bottom = viewBounds.bottom + Math.round(view.getTranslationY());
                    top = bottom - diviter.getIntrinsicHeight();
                    diviter.setBounds(left, top, right, bottom);
                    diviter.draw(canvas);
                }
            }

            canvas.restore();
        }

        /**
         * 绘制横向布局的分割线
         */
        private void drawHorizontalLayoutLine(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            canvas.save();

            // 定义竖线的上下位置
            int top, bottom;
            if (parent.getClipToPadding()) {
                top = parent.getPaddingTop();
                bottom = parent.getHeight() - parent.getPaddingBottom();
                canvas.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
            } else {
                top = 0;
                bottom = parent.getHeight();
            }

            // 循环定义线条的左右位置并绘制分割线
            int left, right;
            Rect viewBounds = new Rect();
            for (int i = 0; i < parent.getChildCount(); i++) {
                final View view = parent.getChildAt(i);
                if (shouldDrawLayoutLine(view, parent, state)) {
                    parent.getLayoutManager().getDecoratedBoundsWithMargins(view, viewBounds);
                    right = viewBounds.right + Math.round(view.getTranslationX());
                    left = right - diviter.getIntrinsicWidth();
                    diviter.setBounds(left, top, right, bottom);
                    diviter.draw(canvas);
                }
            }

            canvas.restore();
        }

        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            if (shouldDrawLayoutLine(view, parent, state)) {
                if (orientation == LinearLayoutManager.VERTICAL) {
                    outRect.set(0, 0, 0, diviter.getIntrinsicHeight());
                } else {
                    outRect.set(0, 0, diviter.getIntrinsicWidth(), 0);
                }
            } else {
                outRect.set(0, 0, 0, 0);
            }
        }

        /**
         * 是否应该绘制纵向布局的横线
         */
        private boolean shouldDrawLayoutLine(View view, RecyclerView parent, RecyclerView.State state) {
            // 不绘制header、footer分割线，不绘制最后一行的分割线
            return parent.getChildAdapterPosition(view) > headerCount - 1
                    && parent.getChildAdapterPosition(view) < state.getItemCount() - footerCount - 1;
        }

    }

    /**
     * Grid布局的分割线
     */
    static class DividerGridViewItemDecoration extends RecyclerView.ItemDecoration {
        public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
        public static final int VERTICAL = LinearLayout.VERTICAL;
        private Drawable divider;
        private int orientation;
        private int headerCount, footerCount;

        DividerGridViewItemDecoration(Drawable divider, int orientation, int headerCount, int footerCount) {
            this.divider = divider;
            this.orientation = orientation;
            this.headerCount = headerCount;
            this.footerCount = footerCount;
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

            // 定义横线的左右位置。去除内边距
            int left, right;
            // 没有header和footer时设置的默认值
            if (parent.getClipToPadding()) {
                left = parent.getPaddingLeft();
                right = parent.getWidth() - parent.getPaddingRight();
                canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
            } else {
                left = 0;
                right = parent.getWidth();
            }
            // 当GridLayoutManager布局方向为横向时：有header和footer时采用第一列的左侧和最后一列的右侧作为新的范围
            if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.HORIZONTAL) {
                if (headerCount > 0) {
                    left = getFirstLineTop(parent);
                }
                if (footerCount > 0) {
                    right = getLastLineBottom(parent);
                }
            }

            // 循环绘制每个子View的分割线
            int top, bottom;
            Rect viewBounds = new Rect();
            for (int i = 0; i < parent.getChildCount(); i++) {
                final View view = parent.getChildAt(i);
                if (shouldDrawVerticalLayoutLine(view, parent, state)) {
                    parent.getDecoratedBoundsWithMargins(view, viewBounds);

                    if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                        bottom = viewBounds.bottom + Math.round(view.getTranslationY());
                        top = bottom - divider.getIntrinsicHeight();
                        divider.setBounds(left, top, right, bottom);
                        divider.draw(canvas);
                    } else {
                        // 获取总行数和当前行数
                        int spanCount = getSpanCount(parent);
                        int viewSpanIndex = getViewSpanIndex(view, parent);
                        // 绘制上半边
                        top = viewBounds.top + Math.round(view.getTranslationY());
                        bottom = top + divider.getIntrinsicHeight() * (viewSpanIndex - 1) / spanCount;
                        if (left != right) {
                            divider.setBounds(left, top, right, bottom);
                            divider.draw(canvas);
                        }
                        // 绘制下半边
                        bottom = viewBounds.bottom + Math.round(view.getTranslationY());
                        top = bottom - divider.getIntrinsicHeight() * (spanCount - viewSpanIndex) / spanCount;
                        if (left != right) {
                            divider.setBounds(left, top, right, bottom);
                            divider.draw(canvas);
                        }
                    }
                }
            }

            canvas.restore();
        }

        /**
         * 绘制横向布局的分割线
         */
        private void drawHorizontalLayoutLine(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
            canvas.save();

            // 定义竖线的上下位置。去除内边距
            int top, bottom;
            // 没有header和footer时设置的默认值
            if (parent.getClipToPadding()) {
                top = parent.getPaddingTop();
                bottom = parent.getHeight() - parent.getPaddingBottom();
                canvas.clipRect(parent.getPaddingLeft(), top, parent.getWidth() - parent.getPaddingRight(), bottom);
            } else {
                top = 0;
                bottom = parent.getHeight();
            }
            // 当GridLayoutManager布局方向为纵向时：有header和footer时采用第一行的顶点和最后一行的底点作为新的范围
            if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                if (headerCount > 0) {
                    top = getFirstLineTop(parent);
                }
                if (footerCount > 0) {
                       bottom = getLastLineBottom(parent);
                }
            }

            // 循环定义线条的左右位置并绘制分割线
            int left, right;
            Rect viewBounds = new Rect();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View view = parent.getChildAt(i);
                if (shouldDrawHorizontalLayoutLine(view, parent, state)) {
                    parent.getLayoutManager().getDecoratedBoundsWithMargins(view, viewBounds);

                    if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                        // 获取总列数和当前列数
                        int totalColumNumber = getSpanCount(parent);
                        int currentColumNumber = getViewSpanIndex(view, parent);
                        // 绘制左半边
                        left = viewBounds.left + Math.round(view.getTranslationX());
                        right = left + divider.getIntrinsicWidth() * (currentColumNumber - 1) / totalColumNumber;
                        if (left != right) {
                            divider.setBounds(left, top, right, bottom);
                            divider.draw(canvas);
                        }
                        // 绘制右半边
                        right = viewBounds.right + Math.round(view.getTranslationX());
                        left = right - divider.getIntrinsicWidth() * (totalColumNumber - currentColumNumber) / totalColumNumber;
                        if (left != right) {
                            divider.setBounds(left, top, right, bottom);
                            divider.draw(canvas);
                        }
                    } else {
                        right = viewBounds.right + Math.round(view.getTranslationX());
                        left = right - divider.getIntrinsicWidth();
                        divider.setBounds(left, top, right, bottom);
                        divider.draw(canvas);
                    }
                }
            }

            canvas.restore();
        }

        @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            /*
                当GridLayoutManager布局方向为纵向时
                    横线使用纵向的空间。可用的空间是无限的，因此每条横线的高度都可以固定写死
                    竖线使用横向的空间。可用空间为每一项空出来的，因此需要按照比例进行分配
                当GridLayoutManager布局方向为横向时
                    横线使用横向的空间。可用空间为每一项空出来的，因此需要按照比例进行分配
                    竖线使用纵向的空间。可用的空间是无限的，因此每条横线的高度都可以固定写死
             */
            if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                if (orientation == LinearLayoutManager.VERTICAL) {                  // 横线
                    if (shouldDrawVerticalLayoutLine(view, parent, state)) {
                        outRect.set(0, 0, 0, divider.getIntrinsicHeight());
                    } else {
                        outRect.set(0, 0, 0, 0);
                    }
                } else {                                                            // 竖线
                    if (shouldDrawHorizontalLayoutLine(view, parent, state)) {
                        int totalColumNumber = getSpanCount(parent);
                        int currentColumNumber = getViewSpanIndex(view, parent);
                        int left = divider.getIntrinsicWidth() * (currentColumNumber - 1) / totalColumNumber;
                        int right = divider.getIntrinsicWidth() * (totalColumNumber - currentColumNumber) / totalColumNumber;
                        outRect.set(left, 0, right, 0);
                    } else {
                        outRect.set(0, 0, 0, 0);
                    }
                }
            } else {
                if (orientation == LinearLayoutManager.HORIZONTAL) {                // 竖线
                    if (shouldDrawVerticalLayoutLine(view, parent, state)) {
                        outRect.set(0, 0, divider.getIntrinsicHeight(), 0);
                    } else {
                        outRect.set(0, 0, 0, 0);
                    }
                } else {                                                            // 横线
                    if (shouldDrawHorizontalLayoutLine(view, parent, state)) {
                        int totalColumNumber = getSpanCount(parent);
                        int currentColumNumber = getViewSpanIndex(view, parent);
                        int top = divider.getIntrinsicWidth() * (currentColumNumber - 1) / totalColumNumber;
                        int bottom = divider.getIntrinsicWidth() * (totalColumNumber - currentColumNumber) / totalColumNumber;
                        outRect.set(0, top, 0, bottom);
                    } else {
                        outRect.set(0, 0, 0, 0);
                    }
                }
            }
        }

        /**
         * 当有header时，会取第一行的顶部位置。该方法是相对于LayoutManager来说的
         *      如果布局是纵向的则返回顶部位置
         *      如果布局是横向的则返回左侧位置
         */
        private int getFirstLineTop(RecyclerView parent) {
            if (parent.getChildCount() > headerCount) {
                Rect childViewBound = new Rect();
                View headerView = parent.getChildAt(headerCount);
                parent.getLayoutManager().getDecoratedBoundsWithMargins(headerView, childViewBound);
                if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                    return Math.round(childViewBound.top + headerView.getTranslationY());
                } else {
                    return Math.round(childViewBound.left + headerView.getTranslationX());
                }
            } else {
                return 0;
            }
        }

        /**
         * 当有footer时，会取最后一行的底部位置。该方法是相对于LayoutManager来说的
         *      如果布局是纵向的则返回底部位置
         *      如果布局是横向的则返回右侧位置
         */
        private int getLastLineBottom(RecyclerView parent) {
            int bottomY = 0;
            Rect childViewBounds = new Rect();
            for (int i = parent.getChildCount() - footerCount - 1; i >= parent.getChildCount() - footerCount - ((GridLayoutManager)parent.getLayoutManager()).getSpanCount() && i >= headerCount; i--) {
                parent.getLayoutManager().getDecoratedBoundsWithMargins(parent.getChildAt(i), childViewBounds);
                if (((GridLayoutManager) parent.getLayoutManager()).getOrientation() == GridLayoutManager.VERTICAL) {
                    bottomY = (int) Math.max(bottomY, childViewBounds.bottom + parent.getChildAt(i).getTranslationY());
                } else {
                    bottomY = (int) Math.max(bottomY, childViewBounds.right + parent.getChildAt(i).getTranslationX());
                }
            }
            return bottomY;
        }

        /**
         * 是否应该绘制纵向布局的线。该方法是相对于LayoutManager来说的
         *      如果布局是纵向的则这里判断的就是是否绘制横线
         *      如果布局是横向的则这里判断的就是是否回执竖线
         */
        private boolean shouldDrawVerticalLayoutLine(View view, RecyclerView parent, RecyclerView.State state) {
            // 不绘制header、footer分割线，不绘制最后一行的分割线
            return parent.getChildAdapterPosition(view) > headerCount - 1
                    && parent.getChildAdapterPosition(view) < state.getItemCount() - footerCount
                    && !isLastRow(view, parent, state);
        }

        /**
         * 是否应该绘制横向布局的线。该方法是相对于LayoutManager来说的
         *      如果布局是纵向的则这里判断的就是是否绘制竖线
         *      如果布局是横向的则这里判断的就是是否回执横线
         */
        private boolean shouldDrawHorizontalLayoutLine(View view, RecyclerView parent, RecyclerView.State state) {
            // 不绘制header、footer分割线
            return parent.getChildAdapterPosition(view) > headerCount - 1
                    && parent.getChildAdapterPosition(view) < state.getItemCount() - footerCount;
        }

        /**
         * 是否是最后一行
         */
        private boolean isLastRow(View view, RecyclerView parent, RecyclerView.State state) {
            int itemPosition = parent.getChildAdapterPosition(view);
            int spanCount = getSpanCount(parent);
            int totalCount = state.getItemCount();
            // 计算时去除header的数量
            itemPosition-=headerCount;
            // 计算时去除footer的数量
            totalCount-=footerCount;
            int totalLineCount = totalCount % spanCount > 0 ? totalCount / spanCount + 1 : totalCount / spanCount;
            int itemLineNumber = (itemPosition + 1) % spanCount > 0 ? (itemPosition + 1) / spanCount + 1 : (itemPosition + 1) / spanCount;
            return itemLineNumber == totalLineCount;
        }

        /**
         * 获取当前View所在Span的位置
         */
        private int getViewSpanIndex(View view, RecyclerView parent) {
            int itemPosition = parent.getChildAdapterPosition(view);
            int spanCount = getSpanCount(parent);
            // 计算时去除header的数量
            itemPosition-=headerCount;
            int columNumber = (itemPosition + 1) % spanCount;
            // 如果是最后一列会为零，需要矫正一下
            return columNumber == 0 ? spanCount : columNumber;
        }

        /**
         * 获取行或列的分割数量
         */
        private int getSpanCount(RecyclerView parent) {
            return ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();
        }
    }

}
