package com.ulfy.android.system.event;

import java.io.File;

/**
 * 裁切图片后收到的事件，该事件作用域为Activity级别
 */
public final class OnCropPictureEvent {
    public final int requestCode;
    public final File file;

    public OnCropPictureEvent(int reuqestCode, File file) {
        this.requestCode = reuqestCode;
        this.file = file;
    }
}
