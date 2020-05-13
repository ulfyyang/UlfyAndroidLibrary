package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 当加载内容时出现错误重新加载显示的界面
 */
public final class ContentDataLoaderFailedView extends FrameLayout implements ITipView, IReloadView {
    private TextView tipTV;                         // 显示提示信息
    private TextView reloadTV;                      // 显示重新加载按钮
    private OnReloadListener onReloadListener;      // 重新加载回调

    public ContentDataLoaderFailedView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_content_data_loader_failed, this);
        this.tipTV = findViewById(R.id.tipTV);
        this.reloadTV = findViewById(R.id.reloadTV);
    }

    @Override public void setTipMessage(Object message) {
        if (message != null) {
            tipTV.setText(message.toString());
        }
    }

    @Override public void setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
        this.reloadTV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContentDataLoaderFailedView.this.onReloadListener == null) {
                    Toast.makeText(getContext(), "未设置处理逻辑", Toast.LENGTH_LONG).show();
                } else {
                    ContentDataLoaderFailedView.this.onReloadListener.onReload();
                }
            }
        });
    }

    @Override public void reload() {
        if (onReloadListener != null) {
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
}
