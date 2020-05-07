package com.ulfy.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * 比例布局
 * 1. 指定长和宽的比例，宽、高会按照指定的比例缩放
 * 2. 在ListView中，需要指定一个最大高度，否则会显得很大
 * 3. 如果按照wrap_content系统无法处理好的话则将wrap_content设置为0，强制让框架认为该边是未知的
 */
public final class RatioLayout extends FrameLayout {
    private int widthRatio, heightRatio;        // 宽度和高度的比例
    private boolean autoScaleChild;             // 是否自动缩放子View使其填满父容器

    public RatioLayout(Context context) {
        super(context);
    }

    public RatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioLayout);
        widthRatio = typedArray.getInt(R.styleable.RatioLayout_ratio_width, 0);
        heightRatio = typedArray.getInt(R.styleable.RatioLayout_ratio_height, 0);
        autoScaleChild = typedArray.getBoolean(R.styleable.RatioLayout_auto_scale_child, false);
        typedArray.recycle();
    }

    @Override public void onViewAdded(View child) {
        if (autoScaleChild) {
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = LayoutParams.MATCH_PARENT;
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthRatio > 0 && heightRatio > 0) {
            // 获得可用的宽高
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            // 如果可用宽高未知，则让其自己测量，然后采用测量后的结果进行处理
            if (widthSize == 0 && heightSize == 0) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                widthSize = getMeasuredWidth();
                heightSize = getMeasuredHeight();
                if (widthSize < heightSize) {
                    heightSize = widthSize * heightRatio / widthRatio;
                } else {
                    widthSize = heightSize * widthRatio / heightRatio;
                }
                setMeasuredDimension(widthSize, heightSize);
            } else {
                // 如果有一个方向可用大小可知则以该方向为标准计算
                if (widthSize == 0 || heightSize == 0) {
                    if (widthSize != 0) {
                        heightSize = widthSize * heightRatio / widthRatio;
                    }
                    if (heightSize != 0) {
                        widthSize = heightSize * widthRatio / heightRatio;
                    }
                }
                // 如果两个方向可用大小都可知，则使用较小边为计算标准
                else {
                    if (widthSize < heightSize) {
                        heightSize = widthSize * heightRatio / widthRatio;
                    } else {
                        widthSize = heightSize * widthRatio / heightRatio;
                    }
                }
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public RatioLayout setWidthRatio(int widthRatio) {
        this.widthRatio = widthRatio;
        return this;
    }

    public RatioLayout setHeightRatio(int heightRatio) {
        this.heightRatio = heightRatio;
        return this;
    }
}
