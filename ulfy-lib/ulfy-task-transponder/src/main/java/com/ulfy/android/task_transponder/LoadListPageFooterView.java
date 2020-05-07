package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 当上拉加载的时候使用加载头
 */
public final class LoadListPageFooterView extends FrameLayout implements IListPageFooterView {
    private LinearLayout loadingLL;
    private LinearLayout loadErrorLL;
    private TextView loadErrorTV;
    private LinearLayout noDataLL;
    private OnReloadListener onReloadListener;

    public LoadListPageFooterView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.ulfy_task_transponder_load_list_page_footer, this);
        loadingLL = findViewById(R.id.loadingLL);
        loadErrorLL = findViewById(R.id.errorLL);
        loadErrorTV = findViewById(R.id.errorTipTV);
        noDataLL = findViewById(R.id.noDataLL);
        loadErrorLL.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (onReloadListener != null) {
                    onReloadListener.onReload();
                }
            }
        });
    }

    @Override public void setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
    }

    @Override public void reload() {
        if (loadErrorLL.getVisibility() == View.VISIBLE && onReloadListener != null) {
            onReloadListener.onReload();
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NetStateListener.registerView(this);
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NetStateListener.unregisterView(this);
    }

    @Override public void gone() {
        setVisibility(View.GONE);
    }

    @Override public void showLoading() {
        setVisibility(View.VISIBLE);
        loadingLL.setVisibility(View.VISIBLE);
        loadErrorLL.setVisibility(View.INVISIBLE);
        noDataLL.setVisibility(View.INVISIBLE);
    }

    @Override public void showNoData() {
        setVisibility(View.VISIBLE);
        loadingLL.setVisibility(View.INVISIBLE);
        loadErrorLL.setVisibility(View.INVISIBLE);
        noDataLL.setVisibility(View.VISIBLE);
    }

    @Override public void showError(Object message) {
        setVisibility(View.VISIBLE);
        loadingLL.setVisibility(View.INVISIBLE);
        loadErrorLL.setVisibility(View.VISIBLE);
        noDataLL.setVisibility(View.INVISIBLE);
        if (message != null) {
            loadErrorTV.setText(message.toString());
        }
    }
}
