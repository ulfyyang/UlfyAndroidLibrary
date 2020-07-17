package com.ulfy.android.system.media_picker;

/**
 * 声音媒体实体
 */
public final class VoiceEntity extends MediaEntity {
    public long duration;

    public VoiceEntity(int id, String title, String filePath, long size, long duration) {
        super(id, title, filePath, size);
        this.duration = duration;
    }
}
