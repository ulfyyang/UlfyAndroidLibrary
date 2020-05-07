package com.ulfy.android.system.event;

import com.ulfy.android.system.media_picker.MediaEntity;

import java.util.List;

/**
 * 选择多媒体文件后收到的事件，该事件作用域为Activity级别
 */
public final class OnPickMediaEvent {
    public final int requestCode;
    public final int search;
    public final int max;
    public final List<MediaEntity> entities;

    public OnPickMediaEvent(int requestCode, int search, int max, List<MediaEntity> entities) {
        this.requestCode = requestCode;
        this.search = search;
        this.max = max;
        this.entities = entities;
    }
}

