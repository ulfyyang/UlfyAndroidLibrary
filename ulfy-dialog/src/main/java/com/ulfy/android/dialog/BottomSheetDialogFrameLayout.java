package com.ulfy.android.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

class BottomSheetDialogFrameLayout extends FrameLayout {
    private CoordinatorLayout coordinator;

    public BottomSheetDialogFrameLayout(Context context) {
        super(context);
    }

    public BottomSheetDialogFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        if (coordinator != null) {
//            int size = (int) ((float) (getResources().getDisplayMetrics().heightPixels * 0.618));
//            heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, Integer.MAX_VALUE);
//        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    void setCoordinator(CoordinatorLayout coordinator) {
        this.coordinator = coordinator;
    }
}
