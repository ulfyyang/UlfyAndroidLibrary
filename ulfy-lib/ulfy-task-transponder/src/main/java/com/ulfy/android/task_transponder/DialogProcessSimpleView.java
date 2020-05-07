package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public final class DialogProcessSimpleView extends FrameLayout implements ITipView {
    private ImageView loadingIV;
    private TextView loadingTV;

    public DialogProcessSimpleView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_dialog_process_simple, this);
        loadingIV = findViewById(R.id.loadingIV);
        loadingTV = findViewById(R.id.loadingTV);
    }

    @Override public void setTipMessage(Object message) { }
}
