package com.sy.comment.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.sy.comment.application.cm.PrizeCM;
import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.vm.PrizeVM;
import com.sy.comment.ui.base.BaseView;
import com.ulfy.android.utils.RecyclerViewUtils;

@Layout(id = R.layout.view_prize)
public class PrizeView extends BaseView {
    @ViewById(id = R.id.prizeRV) private RecyclerView noticeRLV;
    private RecyclerAdapter<PrizeCM> prizeAdapter = new RecyclerAdapter<>();
    private PrizeVM vm;

    public PrizeView(Context context) {
        super(context);
        init(context, null);
    }

    public PrizeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        RecyclerViewUtils.linearLayout(noticeRLV).vertical();
        noticeRLV.setAdapter(prizeAdapter);
    }

    @Override public void bind(IViewModel model) {
        vm = (PrizeVM) model;
    }
}