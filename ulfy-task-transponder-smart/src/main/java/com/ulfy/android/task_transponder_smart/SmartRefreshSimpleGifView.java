package com.ulfy.android.task_transponder_smart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;

public final class SmartRefreshSimpleGifView extends FrameLayout implements RefreshHeader {

    public SmartRefreshSimpleGifView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_smart_refresh_simple_gif, this);
    }

    @Override public View getView() {
        return this;
    }

    @Override public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;          // 设置平移模式
    }

    @Override public void onInitialized(RefreshKernel kernel, int height, int maxDragHeight) {
        kernel.requestDefaultTranslationContentFor(this, true);     // 设置内容跟随header移动
    }

    @Override public void setPrimaryColors(int... colors) { }
    @Override public void onMoving(boolean isDragging, float percent, int offset, int height, int maxDragHeight) { }
    @Override public void onReleased(RefreshLayout refreshLayout, int height, int maxDragHeight) { }
    @Override public void onStartAnimator(RefreshLayout refreshLayout, int height, int maxDragHeight) { }
    @Override public int onFinish(RefreshLayout refreshLayout, boolean success) { return 0; }
    @Override public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) { }
    @Override public boolean isSupportHorizontalDrag() { return false; }
    @Override public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) { }
}
