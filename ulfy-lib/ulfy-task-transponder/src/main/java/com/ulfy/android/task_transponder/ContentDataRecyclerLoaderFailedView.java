package com.ulfy.android.task_transponder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 当加载内容时出现错误重新加载显示的界面
 */
public final class ContentDataRecyclerLoaderFailedView extends FrameLayout implements ITipView, IReloadView {
    private LinearLayout reloadLL;                  // 点击重新加载布局
    private TextView tipTV;                         // 显示提示信息
    private OnReloadListener onReloadListener;      // 重新加载回调

    public ContentDataRecyclerLoaderFailedView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_content_data_recycler_loader_failed, this);
        this.reloadLL = findViewById(R.id.reloadLL);
        this.tipTV = findViewById(R.id.tipTV);
    }

    @Override public void setTipMessage(Object message) {
        if (message != null) {
            tipTV.setText(message.toString());
        }
    }

    @Override public void setOnReloadListener(OnReloadListener onReloadListener) {
        this.onReloadListener = onReloadListener;
        this.reloadLL.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ContentDataRecyclerLoaderFailedView.this.onReloadListener == null) {
                    Toast.makeText(getContext(), "未设置处理逻辑", Toast.LENGTH_LONG).show();
                } else {
                    ContentDataRecyclerLoaderFailedView.this.onReloadListener.onReload();
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
