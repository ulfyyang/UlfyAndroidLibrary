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
import com.ulfy.android.views.RatioLayout;

import java.text.DecimalFormat;

public final class MediaPickerVideoCell extends FrameLayout implements IView {
    private RatioLayout containerASL;
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
        containerASL = (RatioLayout) findViewById(R.id.containerASL);
        pictureIV = (ImageView) findViewById(R.id.pictureIV);
        checkStateIV = (ImageView) findViewById(R.id.checkStateIV);
        timeTV = (TextView) findViewById(R.id.timeTV);
    }

    @Override public void bind(IViewModel model) {
        cm = (MediaPickerVideoCM) model;

        MediaPickWrapper wrapper = cm.wrapper;
        VideoEntity videoEntity = (VideoEntity) cm.wrapper.getMediaEntity();

        Glide.with(getContext()).load(videoEntity.file).placeholder(R.drawable.ulfy_system_background_load_image)
                .thumbnail(0.5f).transition(DrawableTransitionOptions.withCrossFade()).into(pictureIV);

        if (wrapper.isSelect()) {
            checkStateIV.setImageResource(R.drawable.ulfy_system_icon_selected_true);
        } else {
            checkStateIV.setImageResource(R.drawable.ulfy_system_icon_selected_false);
        }

        timeTV.setText(convertTimeToStr(videoEntity.duration));
    }

    private String convertTimeToStr(long time) {
        time /= 1000;
        int hour = (int) (time / 3600);
        int minute = (int) ((time - hour * 3600) / 60);
        int second = (int) (time - hour * 3600 - minute * 60);
        DecimalFormat timeDecimalFormat = new DecimalFormat("00");
        return String.format("%s:%s:%s", hour, timeDecimalFormat.format(minute), timeDecimalFormat.format(second));
    }
}
