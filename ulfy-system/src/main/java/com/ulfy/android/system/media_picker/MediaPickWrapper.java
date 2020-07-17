package com.ulfy.android.system.media_picker;

class MediaPickWrapper {
    private MediaEntity mediaEntity;  // 多媒体实体
    private boolean isSelect;                   // 是否被选中

    MediaPickWrapper(MediaEntity mediaEntity, boolean isSelect) {
        this.mediaEntity = mediaEntity;
        this.isSelect = isSelect;
    }

    MediaEntity getMediaEntity() {
        return mediaEntity;
    }

    boolean isSelect() {
        return isSelect;
    }

    void setMediaEntity(MediaEntity mediaEntity) {
        this.mediaEntity = mediaEntity;
    }

    void setSelect(boolean select) {
        isSelect = select;
    }
}
