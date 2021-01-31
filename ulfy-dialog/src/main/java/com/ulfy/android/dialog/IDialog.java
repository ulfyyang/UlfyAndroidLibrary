package com.ulfy.android.dialog;

import android.content.Context;

public interface IDialog {
    /**
     * 获取弹出框的ID
     */
    public String getDialogId();
    /**
     * 获取弹出框所使用的上下文
     */
    public Context getDialogContext();
    /**
     * 显示弹出框
     */
    public void show();
    /**
     * 关闭弹出框
     */
    public void dismiss();
    /**
     * 忽略输入法对弹出框的影响
     */
    public void ignoreSoftInputMethod();
}
