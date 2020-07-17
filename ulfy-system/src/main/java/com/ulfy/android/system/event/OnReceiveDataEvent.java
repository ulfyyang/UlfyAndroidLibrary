package com.ulfy.android.system.event;

import android.os.Bundle;

/**
 * 接收到数据后收到的事件，该事件作用域为Activity级别
 */
public final class OnReceiveDataEvent {
    public final int requestCode;
    public final Bundle data;

    public OnReceiveDataEvent(int requestCode, Bundle data) {
        this.requestCode = requestCode;
        this.data = data;
    }
}
