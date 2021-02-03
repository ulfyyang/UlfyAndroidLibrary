package com.ulfy.android.system.media_picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.system.R;

public final class MediaPickerVideoCell extends FrameLayout implements IView {
    private ImageView pictureIV;
    private ImageView checkStateIV;
    private TextView timeTV;
    private MediaPickerVideoCM cm;

    public MediaPickerVideoCell(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.ulfy_system_cell_media_picker_video, this);
        pictureIV = findViewById(R.id.pictureIV);
        checkStateIV = findViewById(R.id.checkStateIV);
        timeTV = findViewById(R.id.timeTV);
    }

    @Override public void bind(IViewModel model) {
        cm = (MediaPickerVideoCM) model;
        Glide.with(getContext()).load(((VideoEntity) cm.wrapper.getMediaEntity()).file)
                .placeholder(R.drawable.ulfy_system_background_load_image)
                .thumbnail(0.5f).transition(DrawableTransitionOptions.withCrossFade())
                .into(pictureIV);
        if (cm.wrapper.isSelect()) {
            checkStateIV.setImageResource(R.drawable.ulfy_system_icon_selected_true);
        } else {
            checkStateIV.setImageResource(R.drawable.ulfy_system_icon_selected_false);
        }
        timeTV.setText(cm.getTimeString());
    }
}
