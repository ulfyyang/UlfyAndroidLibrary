package com.ulfy.android.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * 用于显示进度的弹出框界面
 */
public class ProgressView extends FrameLayout {
    public static final int SHOW_MODE_PERCENT = 0;  // 百分比显示模式
    public static final int SHOW_MODE_NUMBER = 1;   // 数字显示模式
    private TextView titleTV;
    private ProgressBar progressPB;
    private TextView progressTV;
    private int showMode = SHOW_MODE_PERCENT;

    public ProgressView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_dialog_view_progress, this);
        titleTV = findViewById(R.id.titleTV);
        progressPB = findViewById(R.id.progressPB);
        progressTV = findViewById(R.id.progressTV);
    }

    public ProgressView setTitle(String title) {
        titleTV.setText(title);
        return this;
    }

    public ProgressView updatePrograss(int total, int current) {
        progressPB.setMax(total);
        progressPB.setProgress(current);
        if (showMode == SHOW_MODE_PERCENT) {
            progressTV.setText(new DecimalFormat("0.00").format(current * 1.0 / total * 100) + "%");
        } else {
            progressTV.setText(String.format("%d/%d", current, total));
        }
        return this;
    }
}
