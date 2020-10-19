package com.ulfy.android.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ulfy.android.adapter.ListAdapter;
import com.ulfy.android.views.ListViewLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 快速选择一项内容的弹出框视图
 */
public final class QuickPickView extends FrameLayout implements IQuickPickView {
    private TextView titleTV;
    private View titleLineV;
    private ListViewLayout pickListLV;
    private TextView cancelTV;
    private String dialogId;
    private List<QuickPickCM> cmList = new ArrayList<>();
    private ListAdapter<QuickPickCM> adapter = new ListAdapter<>(cmList);
    private OnItemClickListener onItemClickListener;

    public QuickPickView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_dialog_view_quick_pick, this);
        this.titleTV = findViewById(R.id.titleTV);
        this.titleLineV = findViewById(R.id.titleLineV);
        this.pickListLV = findViewById(R.id.pickListLV);
        this.cancelTV = findViewById(R.id.cancelTV);
        pickListLV.setOnItemClickListener(new ListViewLayout.OnItemClickListener() {
            @Override public void onItemClick(ListViewLayout parent, View view, int position, Object item, long itemId) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position, cmList.get(position).text.toString());
                }
                DialogUtils.dismissDialog(dialogId);
            }
        });
        cancelTV.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                DialogUtils.dismissDialog(dialogId);
            }
        });
    }

    @Override public IQuickPickView setDialogId(String dialogId) {
        this.dialogId = dialogId;
        return this;
    }

    @Override public QuickPickView setTitle(CharSequence title) {
        titleTV.setVisibility(title == null || title.length() == 0 ? View.GONE : View.VISIBLE);
        titleLineV.setVisibility(title == null || title.length() == 0 ? View.GONE : View.VISIBLE);
        if (title != null && title.length() > 0) {
            titleTV.setText(title);
            titleTV.setTextSize(13);
            titleTV.setTextColor(Color.parseColor("#FF666666"));
        }
        return this;
    }

    @Override public QuickPickView setData(List<CharSequence> list) {
        cmList.clear();
        for (CharSequence charSequence : list) {
            cmList.add(new QuickPickCM(charSequence));
        }
        pickListLV.setAdapter(adapter);
        return this;
    }

    @Override
    public IQuickPickView setData(List<CharSequence> list, boolean is_comment) {
        cmList.clear();
        for (CharSequence charSequence : list) {
            cmList.add(new QuickPickCM(charSequence, is_comment));
        }
        pickListLV.setAdapter(adapter);
        return this;
    }

    @Override public QuickPickView setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }
}
