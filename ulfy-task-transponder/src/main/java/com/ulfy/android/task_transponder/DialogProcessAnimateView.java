package com.ulfy.android.task_transponder;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 当加载页面正在加载中时显示的界面，旋转加载动画风格
 */
public final class DialogProcessAnimateView extends FrameLayout implements ITipView {
    private ImageView loadingIV;
    private TextView loadingTV;

    public DialogProcessAnimateView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_task_transponder_dialog_process_animate, this);
        loadingIV = findViewById(R.id.loadingIV);
        loadingTV = findViewById(R.id.loadingTV);
    }

    @Override public void setTipMessage(Object message) {
        if (message != null) {
            loadingTV.setText(message.toString());
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AnimationDrawable animationDrawable = (AnimationDrawable) loadingIV.getDrawable();
        animationDrawable.start();
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AnimationDrawable animationDrawable = (AnimationDrawable) loadingIV.getDrawable();
        animationDrawable.stop();
    }
}
