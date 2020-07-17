package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * 以弹出框的方式进行数据处理
 */
public class DialogProcesser extends NoNetConnectionTransponder {
    private Context context;            // 当前所处的上下文
    private CustomDialog dialog;        // 处理中显示的弹出框
    private TaskTransponderConfig.DialogProcesserConfig dialogProcesserConfig;                          // 表现配置

    public DialogProcesser(Context context) {
        this.context = context;
        dialogProcesserConfig = TaskTransponderConfig.Config.dialogProcesserConfig;
    }

    @Override public void onNetError(Object data) {
        if (dialogProcesserConfig.tipError()) {
            Toast.makeText(context, data.toString(), Toast.LENGTH_LONG).show();
        }
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override public void onStart(Object tipData) {
        ITipView loadingView = dialogProcesserConfig.getLoadingView(context);
        loadingView.setTipMessage(tipData);
        if (dialog == null) {
            dialog = UiUtils.generateDialogByConfig(context, (View) loadingView, dialogProcesserConfig);
        }
        dialog.show();
    }

    /**
     * 当加载失败后提示错误提示
     */
    @Override public void onFail(Object tipData) {
        if (dialogProcesserConfig.tipError()) {
            Toast.makeText(context, tipData.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override protected void onUpdate(Object data) {
        if (dialog.isShowing()) {
            ITipView loadingView = (ITipView) dialog.getDialogView();
            loadingView.setTipMessage(data);
        }
    }

    /**
     * 当任务结束后确保加载框关闭
     */
    @Override public void onFinish(Object tipData) {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /**
     * 设置页面显示样式配置
     */
    public final DialogProcesser setDialogProcesserConfig(TaskTransponderConfig.DialogProcesserConfig dialogProcesserConfig) {
        this.dialogProcesserConfig = dialogProcesserConfig;
        return this;
    }
}
