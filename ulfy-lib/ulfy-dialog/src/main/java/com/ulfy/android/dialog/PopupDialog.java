package com.ulfy.android.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

/**
 * 弹出层弹出框
 */
class PopupDialog extends PopupWindow implements IDialog {
    private Context context;
    private String dialogId;
    private View contentView;
    private View anchorView;

    PopupDialog(Context context, String dialogId, View contentView, View anchorView) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        this.dialogId = dialogId;
        this.contentView = contentView;
        this.anchorView = anchorView;
    }

    PopupDialog build() {
        FrameLayout containerFL = new FrameLayout(context);
        this.setContentView(containerFL);
        UiUtils.clearParent(this.contentView);
        containerFL.addView(this.contentView, generateLayoutParams());
        this.contentView.setVisibility(View.VISIBLE);
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setOutsideTouchable(false);
        this.setFocusable(true);
        this.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                DialogRepository.getInstance().removeDialog(PopupDialog.this);
            }
        });
        return this;
    }

    private FrameLayout.LayoutParams generateLayoutParams() {
        if (this.contentView.getLayoutParams() == null) {
            return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            return new FrameLayout.LayoutParams(this.contentView.getLayoutParams());
        }
    }

    @Override public String getDialogId() {
        return dialogId;
    }

    @Override public Context getDialogContext() {
        return context;
    }

    @Override public void show() {
        DialogRepository.getInstance().addDialog(this);
        super.showAsDropDown(anchorView);
    }

    @Override public void dismiss() {
        DialogRepository.getInstance().removeDialog(this);
        super.dismiss();
    }
}
