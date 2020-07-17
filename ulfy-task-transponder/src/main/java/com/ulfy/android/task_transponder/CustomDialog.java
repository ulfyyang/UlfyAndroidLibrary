package com.ulfy.android.task_transponder;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

class CustomDialog extends Dialog {
    private Context context;                                        // 弹出框使用的Activity环境
    private View dialogView;                                        // 弹出框显示的View
    private boolean noBackground;                                   // 是否去除默认的遮罩层
    private boolean isFullDialog;                                   // 是否全屏显示内容
    private int gravity = Gravity.CENTER;                           // 内容显示的位置
    private boolean isTouchOutsideDismiss = true;                   // 点击内容外部是否关闭弹出框
    private boolean isCancelable = true;                            // 设置是否可以手动取消显示框

    CustomDialog(Context context, View dialogView, boolean noBackground, boolean isFullDialog, int gravity, boolean isTouchOutsideDismiss, boolean isCancelable) {
        super(context, R.style.CustomDialog);
        this.context = context;
        this.dialogView = dialogView;
        this.noBackground = noBackground;
        this.isFullDialog = isFullDialog;
        this.gravity = gravity;
        this.isTouchOutsideDismiss = isTouchOutsideDismiss;
        this.isCancelable = isCancelable;
    }

    CustomDialog build() {
        // 清除父容器
        if (dialogView.getParent() != null) {
            ((ViewGroup)dialogView.getParent()).removeView(dialogView);
        }
        // 创建一个容器作为弹出框的根容器，这样做的目的是为了控制点击View外部是否消失
        FrameLayout container = new FrameLayout(context);
        int matchParent = ViewGroup.LayoutParams.MATCH_PARENT;
        int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
        this.setContentView(container, new ViewGroup.LayoutParams(matchParent, matchParent));
        // 弹出框设置为屏幕的宽和高，防止弹出框弹出时状态栏变黑。只设置宽为屏幕宽即可避免状态栏变黑，如果高度也设置的话有些位移计算会错位
        this.getWindow().setLayout(getContext().getResources().getDisplayMetrics().widthPixels, /*UiUtils.screenHeight()*/matchParent);
        // 去除默认遮罩层
        if (noBackground) {
            this.getWindow().setDimAmount(0f);
        }
        // 按照位置向容器(container)中添加显示的视图
        FrameLayout.LayoutParams containerLP = null;
        if(isFullDialog) {
            containerLP = new FrameLayout.LayoutParams(matchParent, matchParent);
        } else {
            ViewGroup.LayoutParams dialogViewLP = dialogView.getLayoutParams();
            if (dialogViewLP == null) {
                containerLP = new FrameLayout.LayoutParams(wrapContent, wrapContent, gravity);
            } else {
                containerLP = new FrameLayout.LayoutParams(dialogViewLP.width, dialogViewLP.height, gravity);
            }
        }
        container.addView(dialogView, containerLP);
        // 点击内容外部是否消失弹出框
        container.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View v, MotionEvent event) {
                if (isTouchOutsideDismiss && !UiUtils.isTouchView(event, dialogView)) {
                    dismiss();
                }
                return false;
            }
        });
        this.setCancelable(isCancelable);
        return this;
    }

    View getDialogView() {
        return dialogView;
    }

    @Override public final void show() {
        DialogRepository.getInstance().addDialog(CustomDialog.this);
        super.show();
    }

    @Override public final void dismiss() {
        DialogRepository.getInstance().removeDialog(CustomDialog.this);
        super.dismiss();
    }
}
