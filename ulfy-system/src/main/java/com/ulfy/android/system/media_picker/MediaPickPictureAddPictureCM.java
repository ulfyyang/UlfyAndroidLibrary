package com.ulfy.android.system.media_picker;


import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

class MediaPickPictureAddPictureCM implements IViewModel {

    @Override public Class<? extends IView> getViewClass() {
        return MediaPickPictureAddPictureCell.class;
    }
}
