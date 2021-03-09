package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * 弹出框加载时使用的界面
 */
public final class DialogProcessView extends FrameLayout implements ITipView {
    private ProgressBar progressPB;
    private TextView tipTV;

    public DialogProcessView(@NonNull Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_dialog_process, this);
        this.progressPB = findViewById(R.id.progressPB);
        tipTV = findViewById(R.id.tipTV);
    }

    @Override public void setTipMessage(Object message) {
        if (message != null) {
            tipTV.setText(message.toString());
        }
    }
}
