package com.ulfy.android.dialog;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ulfy.android.adapter.RecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 快速选择一项内容的弹出框视图
 */
public final class QuickPickView extends FrameLayout implements IQuickPickView {
    private TextView titleTV;
    private View titleLineV;
    private RecyclerView pickListRV;
    private TextView cancelTV;
    private String dialogId;
    private List<QuickPickCM> cmList = new ArrayList<>();
    private RecyclerAdapter<QuickPickCM> adapter = new RecyclerAdapter<>(cmList);
    private OnItemClickListener onItemClickListener;

    public QuickPickView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.ulfy_dialog_view_quick_pick, this);
        this.titleTV = findViewById(R.id.titleTV);
        this.titleLineV = findViewById(R.id.titleLineV);
        this.pickListRV = findViewById(R.id.pickListRV);
        this.cancelTV = findViewById(R.id.cancelTV);
        pickListRV.setLayoutManager(new MatchParentLinearLayoutManager(context));
        DividerItemDecoration divider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
        divider.setDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_divider_quick_pick, null));
        pickListRV.addItemDecoration(divider);
        pickListRV.setAdapter(adapter);
        adapter.setOnItemClickListener((parent, view, position, model) -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position, cmList.get(position).text.toString());
            }
            DialogUtils.dismissDialog(dialogId);
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
        pickListRV.setAdapter(adapter);
        return this;
    }

    @Override public IQuickPickView setData(List<CharSequence> list, boolean is_comment) {
        cmList.clear();
        for (CharSequence charSequence : list) {
            cmList.add(new QuickPickCM(charSequence, is_comment));
        }
        pickListRV.setAdapter(adapter);
        return this;
    }

    @Override public QuickPickView setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }
}


class MatchParentLinearLayoutManager extends LinearLayoutManager {

    public MatchParentLinearLayoutManager(Context context) {
        super(context);
    }

    @Override public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        return super.generateLayoutParams(lp);
    }
}
