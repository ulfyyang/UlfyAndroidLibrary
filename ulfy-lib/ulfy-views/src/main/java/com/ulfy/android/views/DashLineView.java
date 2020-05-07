package com.ulfy.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.views.R;

public class DashLineView extends View {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    private int mDashWidth = 10;        // 每根虚线的长度
    private int mDashGap = 1;           // 虚线的间隙
    private int mThickness = 10;        // 画笔的粗细
    private int[] mColors = {Color.parseColor("#ECECEC"), Color.TRANSPARENT};
    private int mOrientation;

    private int mLineCount;
    private int mColorCount;
    private Paint mPaint;

    public DashLineView(Context context) {
        super(context);
        init(null);
    }

    public DashLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.dashLine);
            mDashWidth = (int) typedArray.getDimension(R.styleable.dashLine_dashWidth, mDashWidth);
            mDashGap = (int) typedArray.getDimension(R.styleable.dashLine_dashGap, mDashGap);
            mColors[0] = typedArray.getColor(R.styleable.dashLine_dashColor, mColors[0]);
            mOrientation = typedArray.getInt(R.styleable.dashLine_orientation, HORIZONTAL);
        }
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);

    }

    public void setColors(int color) {
        mColors[0] = color;
        postInvalidate();
    }

    public void setDashWidth(int dashWidth) {
        mDashWidth = dashWidth;
        postInvalidate();
    }

    public void setDashGap(int dashGap) {
        mDashGap = dashGap;
        postInvalidate();
    }

    public void setThickness(int thickness) {
        mThickness = thickness;
        postInvalidate();
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
        postInvalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        mPaint.setStrokeWidth(mThickness);
        if (mLineCount == 0) {
            calCount();
        }

        if (mOrientation == VERTICAL) {
            int yWidth = mDashWidth + mDashGap;
            int x = (getMeasuredWidth() - getPaddingRight() + getPaddingLeft()) / 2;
            int myWidth = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

            for (int i = 0; i < mLineCount; i++) {
                mPaint.setColor(mColors[i % mColorCount]);
                if (i == mLineCount - 1 && ((mLineCount - 1) * yWidth + mDashWidth > myWidth)) {
                    canvas.drawLine(x, i * yWidth + getPaddingTop(), x, myWidth, mPaint);
                } else {
                    canvas.drawLine(x, i * yWidth + getPaddingTop(), x, i * yWidth + getPaddingTop() + mDashWidth, mPaint);
                }
            }
        } else {
            int xWidth = mDashWidth + mDashGap;
            int y = (getMeasuredHeight() - getPaddingBottom() + getPaddingTop()) / 2;
            int myWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            for (int i = 0; i < mLineCount; i++) {
                mPaint.setColor(mColors[i % mColorCount]);
                if (i == mLineCount - 1 && ((mLineCount - 1) * xWidth + mDashWidth > myWidth)) {
                    canvas.drawLine(i * xWidth + getPaddingLeft(), y, myWidth, y, mPaint);
                } else {
                    canvas.drawLine(i * xWidth + getPaddingLeft(), y, i * xWidth + mDashWidth + getPaddingLeft(), y, mPaint);
                }
            }
        }
        super.onDraw(canvas);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 0, height = 0;
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int specHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (mOrientation == VERTICAL) {
            if (getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT)
                width = specWidth;
            else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT)
                width = mThickness;
            else if (getLayoutParams().width != ViewGroup.LayoutParams.WRAP_CONTENT)
                width = getLayoutParams().width;
        } else {
            if (getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT)
                height = specHeight;
            else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
                height = mThickness;
            else if (getLayoutParams().height != ViewGroup.LayoutParams.WRAP_CONTENT)
                height = getLayoutParams().height;
        }
        if (mOrientation == VERTICAL) {
            switch (MeasureSpec.getMode(widthMeasureSpec)) {
                case MeasureSpec.EXACTLY:
                    width = specWidth;
                    break;
                case MeasureSpec.AT_MOST:
                    width = Math.min(width, specWidth);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    break;
            }
        } else {
            width = widthMeasureSpec;
        }
        if (mOrientation == HORIZONTAL) {
            switch (MeasureSpec.getMode(heightMeasureSpec)) {
                case MeasureSpec.EXACTLY:
                    height = specHeight;
                    break;
                case MeasureSpec.AT_MOST:
                    height = Math.min(height, specHeight);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    break;
            }
        } else {
            height = heightMeasureSpec;
        }
        if (mOrientation == VERTICAL) {
            mThickness = width - getPaddingLeft() - getPaddingRight();
            setMeasuredDimension(width, heightMeasureSpec);
        } else {
            mThickness = height - getPaddingTop() - getPaddingBottom();
            setMeasuredDimension(widthMeasureSpec, height);
        }
    }

    private void calCount() {
        if (mOrientation == VERTICAL) {
            int myHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            mLineCount = myHeight / (mDashWidth + mDashGap);
            if (mLineCount * (mDashWidth + mDashGap) < myHeight) {
                ++mLineCount;
            }
        } else {
            int myWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            mLineCount = myWidth / (mDashWidth + mDashGap);
            if (mLineCount * (mDashWidth + mDashGap) < myWidth) {
                ++mLineCount;
            }
        }
        mColorCount = mColors.length;
    }
}
