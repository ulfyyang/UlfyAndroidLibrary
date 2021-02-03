package com.ulfy.android.system.media_picker;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

class MediaPickerPictureCM implements IViewModel {
    MediaPickWrapper wrapper;

    public MediaPickerPictureCM(MediaPickWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override public Class<? extends IView> getViewClass() {
        return MediaPickerPictureCell.class;
    }
}
