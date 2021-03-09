package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * 当加载页面正在加载中时显示的界面，IOS风格
 */
public final class DialogProcessIOSView extends FrameLayout implements ITipView {
    private ImageView loadingIV;
    private TextView loadingTV;

    public DialogProcessIOSView(@NonNull Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_dialog_process_ios, this);
        loadingIV = findViewById(R.id.loadingIV);
        loadingTV = findViewById(R.id.loadingTV);
    }

    @Override public void setTipMessage(Object message) {
        if (message != null) {
            loadingTV.setText(message.toString());
        }
    }
}
