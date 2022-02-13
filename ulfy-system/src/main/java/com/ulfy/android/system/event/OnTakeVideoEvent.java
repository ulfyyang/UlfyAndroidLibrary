package com.ulfy.android.system.event;

import java.io.File;

/**
 * 拍摄视频后收到的事件，该事件作用域为Activity级别
 */
public final class OnTakeVideoEvent {
    public final int requestCode;
    public final File file;
    public long duration;       // 视频时长

    public OnTakeVideoEvent(int requestCode, File file, long duration) {
        this.requestCode = requestCode;
        this.file = file;
        this.duration = duration;
    }
}
