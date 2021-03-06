package com.ulfy.android.system.media_picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.system.R;

public final class MediaPickPictureAddPictureCell extends FrameLayout implements IView {
    private MediaPickPictureAddPictureCM cm;

    public MediaPickPictureAddPictureCell(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.ulfy_system_cell_media_picker_add_picture, this);
    }

    @Override public void bind(IViewModel model) {
        cm = (MediaPickPictureAddPictureCM) model;
    }
}
