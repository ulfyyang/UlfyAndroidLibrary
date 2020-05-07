package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

public final class ContentDataLoaderLoadingSimpleView extends FrameLayout implements ITipView {
    private ImageView loadingIV;

    public ContentDataLoaderLoadingSimpleView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_content_data_loader_loading_simple, this);
        loadingIV = findViewById(R.id.loadingIV);
    }

    @Override public void setTipMessage(Object message) { }
}
