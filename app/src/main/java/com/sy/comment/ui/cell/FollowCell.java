package com.sy.comment.ui.cell;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.cm.FollowCM;
import com.sy.comment.ui.base.BaseCell;

@Layout(id = R.layout.cell_follow)
public class FollowCell extends BaseCell {
    @ViewById(id = R.id.textTV) private TextView textTV;
    private FollowCM cm;

    public FollowCell(Context context) {
        super(context);
        init(context, null);
    }

    public FollowCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        cm = (FollowCM) model;

    }
}