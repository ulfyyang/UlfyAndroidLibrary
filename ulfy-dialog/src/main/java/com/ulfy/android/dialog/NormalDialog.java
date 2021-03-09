package com.ulfy.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * 对 Dialog 的统一抽象基类
 */
public final class NormalDialog extends Dialog implements IDialog {
    private Context context;                                    // 弹出框使用的Activity环境
    private String dialogId;                                    // 弹出框的ID
    private View dialogView;                                    // 弹出框显示的View
    private boolean noBackground = false;                       // 是否去除给经遮罩曾
    private boolean isFullDialog;                               // 是否全屏显示内容
    private int gravity;                                        // 内容显示的位置
    private boolean isTouchOutsideDismiss;                      // 点击内容外部是否关闭弹出框
    private boolean isCancelable;                               // 设置是否可以手动取消显示框
    private int dialogAnimationId;                              // 窗口使用的动画特效
    private OnDialogShowListener onDialogShowListener;          // 当弹出框显示的时候执行的回调
    private OnDialogDismissListener onDialogDismissListener;    // 当弹出框隐藏的时候执行的回调

    private NormalDialog(Context context, String dialogId, View dialogView) {
        super(context, R.style.Dialog);
        this.context = context;
        this.dialogId = dialogId;
        this.dialogView = dialogView;
    }

    private NormalDialog build() {
        UiUtils.clearParent(dialogView);
        // 创建一个容器作为弹出框的根容器，这样做的目的是为了控制点击View外部是否消失
        FrameLayout container = new FrameLayout(context);
        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        this.setContentView(container, new ViewGroup.LayoutParams(matchParent, matchParent));
        // 弹出框设置为屏幕的宽和高，防止弹出框弹出时状态栏变黑。只设置宽为屏幕宽即可避免状态栏变黑，如果高度也设置的话有些位移计算会错位
        this.getWindow().setLayout(getContext().getResources().getDisplayMetrics().widthPixels, /*UiUtils.screenHeight()*/matchParent);
        if (noBackground) {
            this.getWindow().setDimAmount(0f);
        }
        // 按照位置向容器(container)中添加显示的视图
        FrameLayout.LayoutParams containerLP = null;
        if(isFullDialog) {
            containerLP = new FrameLayout.LayoutParams(matchParent, matchParent);
        } else {
            ViewGroup.LayoutParams dialogViewLP = dialogView.getLayoutParams();
            // 如果设置了布局参数，则使用自带的布局参数
            if (dialogViewLP == null) {
                containerLP = new FrameLayout.LayoutParams(wrapContent, wrapContent, gravity);
            } else {
                containerLP = new FrameLayout.LayoutParams(dialogViewLP.width, dialogViewLP.height, gravity);
            }
        }
        container.addView(dialogView, containerLP);
        // 点击内容外部是否消失弹出框
        container.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (isTouchOutsideDismiss && !UiUtils.isTouchView(event, dialogView)) {
                    NormalDialog.this.dismiss();
                }
                return false;
            }
        });
        this.setCancelable(isCancelable);
        if (dialogAnimationId != 0) {
            this.getWindow().setWindowAnimations(dialogAnimationId);
        }
        this.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                if (onDialogShowListener != null) {
                    onDialogShowListener.onDialogShow(NormalDialog.this);
                }
            }
        });
        this.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (onDialogDismissListener != null) {
                    onDialogDismissListener.onDialogDismiss(NormalDialog.this);
                }
            }
        });
        return this;
    }

    /*
    对于弹出框的管理必须防盗show和dismiss的复写上，若写到相应的回调上若调用速度过快会导致弹出框无法关闭的情况
     */

    @Override public final void show() {
        DialogRepository.getInstance().addDialog(NormalDialog.this);
        super.show();
    }

    @Override public final void dismiss() {
        DialogRepository.getInstance().removeDialog(NormalDialog.this);
        super.dismiss();
    }

    @Override public void ignoreSoftInputMethod() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @Override public final Context getDialogContext() {
        return context;
    }

    @Override public final String getDialogId() {
        return dialogId;
    }

    public View getDialogView() {
        return dialogView;
    }

    /**
     * 当弹出框消失的时候执行的回调
     */
    public interface OnDialogDismissListener {

        /**
         * 当弹出框消失的时候执行的回调
         * @param dialog    消失的弹出框
         */
        void onDialogDismiss(NormalDialog dialog);
    }

    /**
     * 当弹出框显示的时候进行的回调
     */
    public interface OnDialogShowListener {

        /**
         * 当弹出框显示的时候执行的回调
         * @param dialog    显示的弹出框
         */
        void onDialogShow(NormalDialog dialog);
    }

    /*
    采用建造者模式设置该类
     */
    public final static class Builder {
        private Context context;                                    // 弹出框使用的Activity环境
        private String dialogId;                                    // 弹出框的ID
        private View dialogView;                                    // 弹出框显示的View
        private boolean noBackground = false;                       // 是否去除给经遮罩曾
        private boolean isFullDialog;                               // 是否全屏显示内容
        private int gravity;                                        // 内容显示的位置
        private boolean isTouchOutsideDismiss;                      // 点击内容外部是否关闭弹出框
        private boolean isCancelable;                               // 设置是否可以手动取消显示框
        private int dialogAnimationId;                              // 弹出框显示使用的动画资源，设置该属性有些动画将会自动调整View的位置
        private OnDialogShowListener onDialogShowListener;          // 当弹出框显示的时候执行的回调
        private OnDialogDismissListener onDialogDismissListener;    // 当弹出框隐藏的时候执行的回调

        public Builder(Context context) {
            this(context, null);
        }

        public Builder(Context context, View dialogView) {
            this.context = context;
            this.dialogId = "ulfy_dialog" + hashCode();     // 默认产生唯一ID
            this.dialogView = dialogView;
            this.gravity = Gravity.CENTER;                  // 默认在最中间显示
            this.isTouchOutsideDismiss = true;              // 默认点击内容外部关闭弹出框
            this.isCancelable = true;
        }

        public NormalDialog build() {
            dialogView.setVisibility(View.VISIBLE);       // 确保界面内容可显示
            NormalDialog dialog = new NormalDialog(context, dialogId, dialogView);
            dialog.noBackground = noBackground;
            dialog.isFullDialog = isFullDialog;
            dialog.gravity = gravity;
            dialog.isTouchOutsideDismiss = isTouchOutsideDismiss;
            dialog.isCancelable = isCancelable;
            dialog.dialogAnimationId = dialogAnimationId;
            dialog.onDialogDismissListener = onDialogDismissListener;
            dialog.onDialogShowListener = onDialogShowListener;
            return dialog.build();
        }

        public String getDialogId() {
            return dialogId;
        }

        public Builder setDialogId(String dialogId) {
            this.dialogId = dialogId;
            return this;
        }

        public Builder setDialogView(View dialogView) {
            this.dialogView = dialogView;
            return this;
        }

        public Builder setNoBackground(boolean noBackground) {
            this.noBackground = noBackground;
            return this;
        }

        public Builder setFullDialog(boolean fullDialog) {
            isFullDialog = fullDialog;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setTouchOutsideDismiss(boolean touchOutsideDismiss) {
            isTouchOutsideDismiss = touchOutsideDismiss;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            isCancelable = cancelable;
            return this;
        }

        public Builder setDialogAnimationId(int dialogAnimationId) {
            if (dialogAnimationId > 0) {
                this.dialogAnimationId = dialogAnimationId;
                // 因为默认值为中间，因此如果不是中间的话说明进行了自定义的设置就不会再覆盖了
                if (dialogAnimationId == R.style.window_anim_bottom && this.gravity == Gravity.CENTER) {
                    this.gravity = Gravity.CENTER | Gravity.BOTTOM;
                } else if (dialogAnimationId == R.style.window_anim_top && this.gravity == Gravity.CENTER) {
                    this.gravity = Gravity.CENTER | Gravity.TOP;
                }
            }
            return this;
        }

        public Builder setOnDialogShowListener(OnDialogShowListener onDialogShowListener) {
            this.onDialogShowListener = onDialogShowListener;
            return this;
        }

        public Builder setOnDialogDismissListener(OnDialogDismissListener onDialogDismissListener) {
            this.onDialogDismissListener = onDialogDismissListener;
            return this;
        }
    }
}
