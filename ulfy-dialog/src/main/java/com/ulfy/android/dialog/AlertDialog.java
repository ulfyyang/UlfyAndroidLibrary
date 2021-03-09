package com.ulfy.android.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.WindowManager;

/**
 * 警告弹出框
 */
public final class AlertDialog implements IDialog {
    private Context context;
    private String dialogId, title, message;
    private int buttonCount;
    private OnClickAlertOkListener onClickAlertOkListener;
    private android.app.AlertDialog systemDialog;

    AlertDialog(Context context, String dialogId, String title, String message, int buttonCount, OnClickAlertOkListener onClickAlertOkListener) {
        this.context = context;
        this.dialogId = dialogId;
        this.title = title;
        this.message = message;
        this.buttonCount = buttonCount;
        this.onClickAlertOkListener = onClickAlertOkListener;
    }

    AlertDialog build() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        if (title != null && title.length() > 0) {
            builder.setTitle(title);
        }
        if (message != null && message.length() > 0) {
            builder.setMessage(message);
        }
        if (buttonCount == 1) {
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (onClickAlertOkListener != null) {
                        onClickAlertOkListener.onClickAlertOk((android.app.AlertDialog) dialog);
                    }
                }
            });
        }
        if (buttonCount == 2) {
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (onClickAlertOkListener != null) {
                        onClickAlertOkListener.onClickAlertOk((android.app.AlertDialog) dialog);
                    }
                }
            });
        }
        systemDialog = builder.create();
        systemDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialog) {
                DialogRepository.getInstance().addDialog(AlertDialog.this);
            }
        });
        systemDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                DialogRepository.getInstance().removeDialog(AlertDialog.this);
            }
        });
        return this;
    }

    @Override public String getDialogId() {
        return dialogId;
    }

    @Override public Context getDialogContext() {
        return context;
    }

    @Override public void show() {
        DialogRepository.getInstance().addDialog(AlertDialog.this);
        systemDialog.show();
    }

    @Override public void dismiss() {
        DialogRepository.getInstance().removeDialog(AlertDialog.this);
        systemDialog.dismiss();
    }

    @Override public void ignoreSoftInputMethod() {
        systemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    /**
     * 点击 Alert 弹出框的 Ok 时执行的操作
     */
    public interface OnClickAlertOkListener {
        /**
         * 点击弹出框的OK按钮
         * @param dialog    点击的弹出框
         */
        void onClickAlertOk(android.app.AlertDialog dialog);
    }
}
