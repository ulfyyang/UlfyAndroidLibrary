package com.ulfy.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public class DialogLayout extends FrameLayout {
    private static final int ANIMATION_TIME = 400;
    private View contentView;
    private boolean open;
    public static final int DIRECTION_LEFT = 0;             // 动画方向：左方出现
    public static final int DIRECTION_TOP = 1;              // 动画方向：上方出现
    public static final int DIRECTION_RIGHT = 2;            // 动画方向：右方出现
    public static final int DIRECTION_BOTTOM = 3;           // 动画方向：下方出现
    private int direction = DIRECTION_BOTTOM;

    public DialogLayout(Context context) {
        super(context);
    }

    public DialogLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DialogLayout);
        direction = typedArray.getInt(R.styleable.DialogLayout_direction, DIRECTION_BOTTOM);
        typedArray.recycle();
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    @Override public void onViewAdded(View child) {
        super.onViewAdded(child);
        contentView = child;
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    /**
     * 显示
     */
    public void show() {
        if (contentView != null && contentView.getAnimation() == null && !open) {
            open = true;
            contentView.setVisibility(View.VISIBLE);
            contentView.startAnimation(generateShowAnimation());
        }
    }

    /**
     * 隐藏
     */
    public void hide() {
        if (contentView != null && contentView.getAnimation() == null && open) {
            open = false;
            contentView.setVisibility(View.VISIBLE);
            contentView.startAnimation(generateHideAnimation());
        }
    }

    private TranslateAnimation generateShowAnimation() {
        if (direction == DIRECTION_LEFT) {
            return generateShowLeftAnimation();
        } else if (direction == DIRECTION_TOP) {
            return generateShowTopAnimation();
        } else if (direction == DIRECTION_RIGHT) {
            return generateShowRightAnimation();
        } else if (direction == DIRECTION_BOTTOM) {
            return generateShowBottomAnimation();
        }
        return null;
    }

    private TranslateAnimation generateHideAnimation() {
        if (direction == DIRECTION_LEFT) {
            return generateHideLeftAnimation();
        } else if (direction == DIRECTION_TOP) {
            return generateHideTopAnimation();
        } else if (direction == DIRECTION_RIGHT) {
            return generateHideRightAnimation();
        } else if (direction == DIRECTION_BOTTOM) {
            return generateHideBottomAnimation();
        }
        return null;
    }

    private TranslateAnimation generateShowLeftAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, -1, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(ANIMATION_TIME);
        return translateAnimation;
    }

    private TranslateAnimation generateShowTopAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, -1, TranslateAnimation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(ANIMATION_TIME);
        return translateAnimation;
    }

    private TranslateAnimation generateShowRightAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(ANIMATION_TIME);
        return translateAnimation;
    }

    private TranslateAnimation generateShowBottomAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 1, TranslateAnimation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(ANIMATION_TIME);
        return translateAnimation;
    }

    private TranslateAnimation generateHideLeftAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, -1,
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(ANIMATION_TIME);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                contentView.setVisibility(View.GONE);
            }
        });
        return translateAnimation;
    }

    private TranslateAnimation generateHideTopAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, -1);
        translateAnimation.setDuration(ANIMATION_TIME);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                contentView.setVisibility(View.GONE);
            }
        });
        return translateAnimation;
    }

    private TranslateAnimation generateHideRightAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 1,
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(ANIMATION_TIME);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                contentView.setVisibility(View.GONE);
            }
        });
        return translateAnimation;
    }

    private TranslateAnimation generateHideBottomAnimation() {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 1);
        translateAnimation.setDuration(ANIMATION_TIME);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationRepeat(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {
                contentView.setVisibility(View.GONE);
            }
        });
        return translateAnimation;
    }

}
