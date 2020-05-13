package com.ulfy.android.system.media_picker;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

class MediaOverMaxSelectCountVM implements IViewModel {
    private int maxCount;
    private String unitName;
    private String typeName;

    MediaOverMaxSelectCountVM(int maxCount, int typeName) {
        this.maxCount = maxCount;
        switch (typeName) {
            case MediaRepository.SEARCH_TYPE_VIDEO:
                unitName = "个";
                this.typeName = "视频";
            break;
            case MediaRepository.SEARCH_TYPE_PICTURE:
                unitName = "张";
                this.typeName = "照片";
            break;
            case MediaRepository.SEARCH_TYPE_VOICE:
                unitName = "个";
                this.typeName = "音频";
            break;
        }
    }

    String getTipText() {
        return String.format("您最多只能选择%d%s%s", maxCount, unitName, typeName);
    }

    @Override
    public Class<? extends IView> getViewClass() {
        return MediaOverMaxSelectCountView.class;
    }
}
