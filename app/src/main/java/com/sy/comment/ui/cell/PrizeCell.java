package com.sy.comment.ui.cell;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.cm.PrizeCM;
import com.sy.comment.ui.base.BaseCell;

@Layout(id = R.layout.cell_prize)
public class PrizeCell extends BaseCell {
    @ViewById(id = R.id.textTV) private TextView textTV;
    private PrizeCM cm;

    public PrizeCell(Context context) {
        super(context);
        init(context, null);
    }

    public PrizeCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        cm = (PrizeCM) model;
    }
}