package com.ulfy.android.system.media_picker;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

import java.text.DecimalFormat;

class MediaPickerVideoCM implements IViewModel {
    public MediaPickWrapper wrapper;

    public MediaPickerVideoCM(MediaPickWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public String getTimeString() {
        long time = ((VideoEntity) wrapper.getMediaEntity()).duration;
        time /= 1000;
        int hour = (int) (time / 3600);
        int minute = (int) ((time - hour * 3600) / 60);
        int second = (int) (time - hour * 3600 - minute * 60);
        DecimalFormat timeDecimalFormat = new DecimalFormat("00");
        return String.format("%s:%s:%s", hour, timeDecimalFormat.format(minute), timeDecimalFormat.format(second));
    }

    @Override public Class<? extends IView> getViewClass() {
        return MediaPickerVideoCell.class;
    }
}
