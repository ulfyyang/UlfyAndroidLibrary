package com.sy.comment.ui.cell;

import android.content.Context;
import android.util.AttributeSet;

import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.cm.SoundCM;
import com.sy.comment.ui.base.BaseCell;

@Layout(id = R.layout.cell_sound)
public class SoundCell extends BaseCell {
    
    private SoundCM cm;

    public SoundCell(Context context) {
        super(context);
        init(context, null);
    }

    public SoundCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        cm = (SoundCM) model;

    }
}