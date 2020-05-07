package com.ulfy.android.system.event;

import java.io.File;

/**
 * 选择图片后收到的事件，该事件作用域为Activity级别
 */
public final class OnPickPictureEvent {
    public final int requestCode;
    public final File file;

    public OnPickPictureEvent(int requestCode, File file) {
        this.requestCode = requestCode;
        this.file = file;
    }
}
