package com.ulfy.android.system.base;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

public class UlfyBaseVisibilityFragment extends Fragment implements View.OnAttachStateChangeListener, OnFragmentVisibilityChangedListener {
    private boolean parentActivityVisible = false;              // ParentActivity是否可见
    private boolean isVisible = false;                          // 是否可见（Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach）
    private UlfyBaseVisibilityFragment parentFragment;          // ParentFragment，用于判定是否可见
    // 当可见性发生变化时的监听
    private OnFragmentVisibilityChangedListener onFragmentVisibilityChangedListener;

    /**
     * 设置当可见性变化时的监听
     */
    public void setOnVisibilityChangedListener(OnFragmentVisibilityChangedListener onFragmentVisibilityChangedListener) {
        this.onFragmentVisibilityChangedListener = onFragmentVisibilityChangedListener;
    }

    /**
     * 子类复写该方法来直接监听是否可见
     */
    protected void onVisibilityChanged(boolean visible) {
        if (onFragmentVisibilityChangedListener != null) {
            onFragmentVisibilityChangedListener.onFragmentVisibilityChanged(visible);
        }
    }

    /**
     * 是否可见（Activity处于前台、Tab被选中、Fragment被添加、Fragment没有隐藏、Fragment.View已经Attach）
     */
    public boolean isVisibleToUser() {
        return isVisible;
    }

    ///////////////////////////////////////////////////////////////////////////
    //      具体实现
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Fragment被添加与移除
     */
    @Override public void onAttach(Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null && parentFragment instanceof UlfyBaseVisibilityFragment) {
            this.parentFragment = ((UlfyBaseVisibilityFragment) parentFragment);
            this.parentFragment.setOnVisibilityChangedListener(this);
        }
        checkVisibility(true);
    }
    /**
     * Fragment被添加与移除
     */
    @Override public void onDetach() {
        if (parentFragment != null) {
            parentFragment.setOnVisibilityChangedListener(null);
        }
        super.onDetach();
        checkVisibility(false);
        parentFragment = null;
    }
    /**
     * View创建完毕
     */
    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.addOnAttachStateChangeListener(this);
    }
    @Override public void onViewAttachedToWindow(View v) {
        checkVisibility(true);
    }
    @Override public void onViewDetachedFromWindow(View v) {
        v.removeOnAttachStateChangeListener(this);
        checkVisibility(false);
    }

    /**
     * Activity可见性判断
     */
    @Override public void onStart() {
        super.onStart();
        onActivityVisibilityChanged(true);
    }
    /**
     * Activity可见性判断
     */
    @Override public void onStop() {
        super.onStop();
        onActivityVisibilityChanged(false);
    }
    /**
     * Activity可见性判断
     */
    private void onActivityVisibilityChanged(boolean visible) {
        parentActivityVisible = visible;
        checkVisibility(visible);
    }

    /**
     * ParentFragment可见性改变
     */
    @Override public void onFragmentVisibilityChanged(boolean visible) {
        checkVisibility(visible);
    }
    /**
     * Fragment可见性判断
     */
    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // hidden 表示是否是隐藏的，后续 checkVisibility 里面的 isVisible 表示是否可见
        // 所以这两个应该是相反的
        checkVisibility(!hidden);
    }

    /**
     * ViewPager可见性判断
     * Tab切换时会回调此方法。对于没有Tab的页面，{@link Fragment#getUserVisibleHint()}默认为true。
     */
    @Override public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        checkVisibility(isVisibleToUser);
    }

    /**
     * 检查可见性是否变化
     * @param expected 可见性期望的值。只有当前值和expected不同，才需要做判断
     */
    private void checkVisibility(boolean expected) {
        if (expected == isVisible) return;
        final boolean parentVisible = parentFragment == null ? parentActivityVisible : parentFragment.isVisibleToUser();
        final boolean superVisible = /*super.isVisible()*/isAdded() && !isHidden();
        final boolean hintVisible = getUserVisibleHint();
        final boolean visible = parentVisible && superVisible && hintVisible;
        if (visible != isVisible) {
            isVisible = visible;
            onVisibilityChanged(isVisible);
        }
    }
}
