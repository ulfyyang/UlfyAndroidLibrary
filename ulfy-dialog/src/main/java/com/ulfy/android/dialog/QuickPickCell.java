package com.ulfy.android.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

/**
 * 快速选择一项内容的弹出框视图选择项
 */
public final class QuickPickCell extends FrameLayout implements IView {
    private TextView itemTV;
    private QuickPickCM cm;

    public QuickPickCell(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(context).inflate(R.layout.ulfy_dialog_cell_quick_pick, this);
        itemTV = findViewById(R.id.itemTV);
    }

    @Override public void bind(IViewModel model) {
        cm = (QuickPickCM) model;
        itemTV.setText(cm.text);
        itemTV.setTextColor(cm.is_comment ? Color.parseColor("#FFE31D1A") : Color.parseColor("#FF333333"));
    }
}
