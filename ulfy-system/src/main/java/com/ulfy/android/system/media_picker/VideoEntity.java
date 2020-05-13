package com.ulfy.android.system.media_picker;

/**
 * 视频媒体实体
 */
public final class VideoEntity extends MediaEntity {
    public long duration;   // 视频的长度，单位毫秒

    public VideoEntity(int id, String title, String filePath, long size, long duration) {
        super(id, title, filePath, size);
        this.duration = duration;
    }
}
