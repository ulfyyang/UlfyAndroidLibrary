package com.ulfy.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

/**
 * 仿网易云的弹窗效果，具有回弹的功能
 *      不要用ListView为显示内容（会有莫名其妙的bug），应该使用RecyclerView或其它常规View作为显示内容
 *      BottomSheetDialog对于输入框的支持会有各种各样的问题，因此对于需要输入的内容建议采用一个新的弹窗单独把输入框顶起来
 *      无论哪种模式的输入法模式，在输入框被顶起来的时候都会导致窗口的整个位移，目前无法解决
 */
class BounceBottomSheetDialog extends Dialog implements IDialog {
    private Context context;
    private String dialogId;

    private BottomSheetBehavior<BottomSheetDialogFrameLayout> mBehavior;

    boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;
    private boolean mCanceledOnTouchOutsideSet;

    private CoordinatorLayout coordinator;
    private BottomSheetDialogFrameLayout bottomSheet;
    private Rect r = new Rect();

    private VelocityTracker velocityTracker;

    public BounceBottomSheetDialog(Context context, String dialogId, boolean noBackground) {
        this(context, 0);
        this.context = context;
        this.dialogId = dialogId;
        if (noBackground) {
            getWindow().setDimAmount(0f);
        }
        this.setOnDismissListener(new OnDismissListener() {
            @Override public void onDismiss(DialogInterface dialog) {
                DialogRepository.getInstance().removeDialog(BounceBottomSheetDialog.this);
            }
        });
    }

    public BounceBottomSheetDialog(@NonNull Context context, @StyleRes int theme) {
        super(context, getThemeResId(context, theme));
        // We hide the title bar for any style configuration. Otherwise, there will be a gap
        // above the bottom sheet when it is expanded.
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    protected BounceBottomSheetDialog(@NonNull Context context, boolean cancelable,
                                      DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        mCancelable = cancelable;
    }

    @Override public String getDialogId() {
        return dialogId;
    }

    @Override public Context getDialogContext() {
        return context;
    }

    @Override public void show() {
        DialogRepository.getInstance().addDialog(this);
        super.show();
    }

    @Override public void dismiss() {
        DialogRepository.getInstance().removeDialog(this);
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
        super.dismiss();
    }

    @Override public void ignoreSoftInputMethod() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResId) {
        super.setContentView(wrapInBottomSheet(layoutResId, null, null));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window != null) {

            // 该方案支持安卓5.0全透明，但是由于安卓5.0无法修改通知栏图表字体颜色，为了更好的兼容性，将要求放宽到6.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.setStatusBarColor(Color.TRANSPARENT);
                //去除效果不佳的半透明状态栏(如果设置了的话)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                //这里的原理是直接设置了完全透明的状态栏，并且保留了内容延伸的效果
                //全屏显示设置新的状态栏:延伸内容到状态栏
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            // 安卓4.4以上
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }

            window.setLayout(getContext().getResources().getDisplayMetrics().widthPixels, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void setContentView(View view) {
        UiUtils.clearParent(view);
        view.setVisibility(View.VISIBLE);
        super.setContentView(wrapInBottomSheet(0, view, view.getLayoutParams()));
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(wrapInBottomSheet(0, view, params));
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        if (mCancelable != cancelable) {
            mCancelable = cancelable;
            if (mBehavior != null) {
                mBehavior.setHideable(cancelable);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBehavior != null) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
        if (cancel && !mCancelable) {
            mCancelable = true;
        }
        mCanceledOnTouchOutside = cancel;
        mCanceledOnTouchOutsideSet = true;
    }


    /**
     * 添加 top 距离顶部多少的时候触发收缩效果
     * @param targetLimitH int 高度限制
     */
    @SuppressWarnings("all")
    public void addSpringBackDisLimit(final int targetLimitH){
        if(coordinator == null)
            return;
        final int totalHeight = getContext().getResources().getDisplayMetrics().heightPixels;
        final int currentH = (int) ((float)totalHeight*0.618);
        final int leftH    = totalHeight - currentH;
        coordinator.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()){
                            case MotionEvent.ACTION_MOVE:
                                // 计算相对于屏幕的 坐标
                                bottomSheet.getGlobalVisibleRect(r);
                                if (velocityTracker == null) {
                                    velocityTracker = VelocityTracker.obtain();
                                }
                                velocityTracker.addMovement(event);
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                velocityTracker.computeCurrentVelocity(1);
                                int limitH;
                                if(targetLimitH < 0)
                                    limitH = (leftH + currentH/2);
                                else
                                    limitH = targetLimitH;
                                if (velocityTracker.getYVelocity() > 4 || r.top > limitH) {
                                    velocityTracker.clear();
                                    return false;
                                } else {
                                    if (mBehavior != null) {
                                        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    }
                                    velocityTracker.clear();
                                }
                                break;
                        }
                        return false;
                    }
                }
        );
    }

    private View wrapInBottomSheet(int layoutResId, View view, ViewGroup.LayoutParams params) {
        final FrameLayout container = (FrameLayout) View.inflate(getContext(),
                R.layout.ulfy_dialog_sheet_layout, null);
        coordinator =
                (CoordinatorLayout) container.findViewById(R.id.c);
        if (layoutResId != 0 && view == null) {
            view = getLayoutInflater().inflate(layoutResId, coordinator, false);
        }
        bottomSheet = coordinator.findViewById(R.id.sub);
        bottomSheet.setCoordinator(coordinator);
        mBehavior = BottomSheetBehavior.from(bottomSheet);
        mBehavior.setBottomSheetCallback(mBottomSheetCallback);
        mBehavior.setHideable(mCancelable);
        if (params == null) {
            bottomSheet.addView(view);
        } else {
            bottomSheet.addView(view, params);
        }
        // We treat the CoordinatorLayout as outside the dialog though it is technically inside
        coordinator.findViewById(R.id.touch_outside).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCancelable && isShowing() && shouldWindowCloseOnTouchOutside()) {
                    cancel();
                }
            }
        });
        // Handle accessibility events
        ViewCompat.setAccessibilityDelegate(bottomSheet, new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(View host,
                                                          AccessibilityNodeInfoCompat info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                if (mCancelable) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS);
                    info.setDismissable(true);
                } else {
                    info.setDismissable(false);
                }
            }

            @Override
            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && mCancelable) {
                    cancel();
                    return true;
                }
                return super.performAccessibilityAction(host, action, args);
            }
        });
        bottomSheet.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // coordinator intecept, 这没用
                return true;
            }
        });
        return container;
    }

    boolean shouldWindowCloseOnTouchOutside() {
        if (!mCanceledOnTouchOutsideSet) {
            if (Build.VERSION.SDK_INT < 11) {
                mCanceledOnTouchOutside = true;
            } else {
                TypedArray a = getContext().obtainStyledAttributes(
                        new int[]{android.R.attr.windowCloseOnTouchOutside});
                mCanceledOnTouchOutside = a.getBoolean(0, true);
                a.recycle();
            }
            mCanceledOnTouchOutsideSet = true;
        }
        return mCanceledOnTouchOutside;
    }

    private static int getThemeResId(Context context, int themeId) {
        if (themeId == 0) {
            // If the provided theme is 0, then retrieve the dialogTheme from our theme
            TypedValue outValue = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    R.attr.bottomSheetDialogTheme, outValue, true)) {
                themeId = outValue.resourceId;
            } else {
                // bottomSheetDialogTheme is not provided; we default to our light theme
                themeId = R.style.Theme_Design_Light_BottomSheetDialog;
            }
        }
        return themeId;
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetCallback
            = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet,
                                   @BottomSheetBehavior.State int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                cancel();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

}