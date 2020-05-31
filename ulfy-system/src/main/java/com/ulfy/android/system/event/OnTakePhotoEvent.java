package com.ulfy.android.system.event;

import java.io.File;

/**
 * 拍照后收到的事件，该事件作用域为Activity级别
 */
public final class OnTakePhotoEvent {
    public final int requestCode;
    public final File file;

    public OnTakePhotoEvent(int requestCode, File file) {
        this.requestCode = requestCode;
        this.file = file;
    }
}
