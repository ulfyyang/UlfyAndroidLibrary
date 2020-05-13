package com.sy.comment.ui.cell;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.sy.comment.R;
import com.sy.comment.application.cm.PictureSquareCM;
import com.sy.comment.ui.base.BaseCell;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;

@Layout(id = R.layout.cell_picture_square)
public class PictureSquareCell extends BaseCell {
    @ViewById(id = R.id.pictureIV) private ImageView pictureIV;
    private PictureSquareCM cm;

    public PictureSquareCell(Context context) {
        super(context);
        init(context, null);
    }

    public PictureSquareCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        cm = (PictureSquareCM) model;

    }
}