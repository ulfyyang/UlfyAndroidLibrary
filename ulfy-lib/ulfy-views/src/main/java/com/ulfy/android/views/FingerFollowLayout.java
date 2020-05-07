package com.ulfy.android.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ulfy.android.views.R;

public class FingerFollowLayout extends FrameLayout {
    private float initX, initY, initMoveX, initMoveY, initTranslationX, initTranslationY;     // 记录初始化时的位置和位移
    private float fingerMoveX, fingerMoveY, viewMoveX, viewMoveY;                             // 记录手指移动的位置和位移
    private int scaledTouchSlop;            // 系统认为的最小移动偏移量
    private boolean haveMoved;              // 判断组件是否移动过了
    public static final int ACTION_MODE_AUTO_ATTACH_SIDE = 0;
    public static final int ACTION_MODE_POSITION_FIXED = 1;
    private int actionMode = ACTION_MODE_AUTO_ATTACH_SIDE;
    private ValueAnimator valueAnimator;

    public FingerFollowLayout(Context context) {
        super(context);
        init(context);
    }

    public FingerFollowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FingerFollowLayout);
        actionMode = typedArray.getInt(R.styleable.FingerFollowLayout_action_mode, ACTION_MODE_AUTO_ATTACH_SIDE);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setX((float)animation.getAnimatedValue());
            }
        });
        valueAnimator.setDuration(300);
    }

    public FingerFollowLayout setActionMode(int actionMode) {
        this.actionMode = actionMode;
        return this;
    }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (actionMode == ACTION_MODE_AUTO_ATTACH_SIDE) {
                    stopAnimateToSide();
                }
                initX = event.getRawX();
                initY = event.getRawY();
                initTranslationX = getTranslationX();
                initTranslationY = getTranslationY();
                haveMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                fingerMoveX = event.getRawX() - initX;
                fingerMoveY = event.getRawY() - initY;
                if (!haveMoved) {
                    if (Math.abs(fingerMoveX) > scaledTouchSlop || Math.abs(fingerMoveY) > scaledTouchSlop) {
                        initMoveX = event.getRawX();
                        initMoveY = event.getRawY();
                        haveMoved = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (actionMode == ACTION_MODE_AUTO_ATTACH_SIDE) {
                    animateToSide();
                }
                haveMoved = false;
                break;
        }
        return haveMoved;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                fingerMoveX = event.getRawX() - initX;
                fingerMoveY = event.getRawY() - initY;
                if (!haveMoved) {
                    if (Math.abs(fingerMoveX) > scaledTouchSlop || Math.abs(fingerMoveY) > scaledTouchSlop) {
                        initMoveX = event.getRawX();
                        initMoveY = event.getRawY();
                        haveMoved = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                if (haveMoved) {
                    viewMoveX = event.getRawX() - initMoveX;
                    viewMoveY = event.getRawY() - initMoveY;
                    setTranslationX(initTranslationX + viewMoveX);
                    setTranslationY(initTranslationY + viewMoveY);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (actionMode == ACTION_MODE_AUTO_ATTACH_SIDE) {
                    animateToSide();
                }
                haveMoved = false;
                break;
        }
        return haveMoved;
    }

    private void stopAnimateToSide() {
        valueAnimator.cancel();
    }

    private void animateToSide() {
        ViewGroup parent = (ViewGroup) getParent();
        valueAnimator.cancel();
        if (getX() + getWidth() / 2 < parent.getWidth() / 2) {
            valueAnimator.setFloatValues(getX(), ((MarginLayoutParams)getLayoutParams()).leftMargin);
        } else {
            valueAnimator.setFloatValues(getX(), parent.getWidth() - getWidth() - ((MarginLayoutParams)getLayoutParams()).rightMargin);
        }
        valueAnimator.start();
    }
}
