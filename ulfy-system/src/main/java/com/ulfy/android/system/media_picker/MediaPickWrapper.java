package com.ulfy.android.system.media_picker;

import java.util.List;

class MediaPickWrapper {
    private List<MediaEntity> mediaEntityList;
    private int index;
    private boolean isSelect;                   // 是否被选中

    MediaPickWrapper(List<MediaEntity> mediaEntityList, int index, boolean isSelect) {
        this.mediaEntityList = mediaEntityList;
        this.index = index;
        this.isSelect = isSelect;
    }

    MediaEntity getMediaEntity() {
        return mediaEntityList.get(index);
    }

    boolean isSelect() {
        return isSelect;
    }

    void setSelect(boolean select) {
        isSelect = select;
    }
}
