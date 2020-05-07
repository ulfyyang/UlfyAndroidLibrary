package com.ulfy.android.system.event;

/**
 * 当网络状态发生变化时会发出该全局事件
 */
public final class OnNetworkStateChangedEvent {
    public boolean connected;

    public OnNetworkStateChangedEvent(boolean connected) {
        this.connected = connected;
    }
}
