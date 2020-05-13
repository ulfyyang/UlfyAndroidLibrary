package com.sy.comment.ui.cell;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.sy.comment.R;
import com.sy.comment.application.cm.MomentCM;
import com.sy.comment.application.cm.PictureSquareCM;
import com.sy.comment.ui.base.BaseCell;
import com.ulfy.android.adapter.RecyclerAdapter;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.ulfy.android.utils.RecyclerViewUtils;
import com.ulfy.android.views.ShapeLayout;

@Layout(id = R.layout.cell_moment)
public class MomentCell extends BaseCell {
    @ViewById(id = R.id.avatarIV) private ImageView avatarIV;
    @ViewById(id = R.id.nicknameTV) private TextView nicknameTV;
    @ViewById(id = R.id.contentTV) private TextView contentTV;
    @ViewById(id = R.id.contentSL) private ShapeLayout contentSL;
    @ViewById(id = R.id.contentRV) private RecyclerView contentRV;
    @ViewById(id = R.id.contentIV) private ImageView contentIV;
    @ViewById(id = R.id.supportNumberTV) private TextView supportNumberTV;
    @ViewById(id = R.id.commentNumberTV) private TextView commentNumberTV;
    @ViewById(id = R.id.optionIV) private ImageView optionIV;
    private RecyclerAdapter<PictureSquareCM> pictureAdapter = new RecyclerAdapter<>();
    private MomentCM cm;

    public MomentCell(Context context) {
        super(context);
        init(context, null);
    }

    public MomentCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        RecyclerViewUtils.gridLayout(contentRV).vertical(3).dividerDp(Color.TRANSPARENT, 5, 5, 0, 0);
        contentRV.setAdapter(pictureAdapter);
    }

    @Override public void bind(IViewModel model) {
        cm = (MomentCM) model;
        contentRV.setVisibility(cm.showPicture ? View.VISIBLE : View.GONE);
        contentSL.setVisibility(cm.showPicture ? View.GONE : View.VISIBLE);
        pictureAdapter.setData(cm.pictureCMList);
        pictureAdapter.notifyDataSetChanged();
    }
}