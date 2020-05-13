package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 当加载页面正在加载中时显示的界面
 */
public final class ContentDataRecyclerLoaderLoadingView extends FrameLayout implements ITipView {
    private TextView tipTV;

    public ContentDataRecyclerLoaderLoadingView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_content_data_recycler_loader_loading, this);
        tipTV = findViewById(R.id.tipTV);
    }

    @Override public void setTipMessage(Object message) {
        if (message != null) {
            tipTV.setText(message.toString());
        }
    }
}
