package com.ulfy.android.system.media_picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ulfy.android.dialog.DialogUtils;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.system.R;

/**
 * 当超出最大选择数的时候显示的弹出框
 */
public final class MediaOverMaxSelectCountView extends FrameLayout implements IView, View.OnClickListener {
    private TextView maxCountTV;
    private TextView haveKnowTV;
    private MediaOverMaxSelectCountVM vm;

    public MediaOverMaxSelectCountView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_system_view_media_over_max_select_count, this);
        maxCountTV = findViewById(R.id.maxCountTV);
        haveKnowTV = findViewById(R.id.haveKnowTV);
        haveKnowTV.setOnClickListener(this);
    }

    @Override public void bind(IViewModel model) {
        vm = (MediaOverMaxSelectCountVM) model;
        maxCountTV.setText(vm.getTipText());
    }

    @Override public void onClick(View v) {
        DialogUtils.dismissDialog();
    }
}
