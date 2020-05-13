package com.ulfy.android.bus;

public class SubscriberBackgroundThread {
    public long threadId;

    @Subscribe(mode = MethodMode.BACKGROUND) public void Event(Event1 event) {
        threadId = Thread.currentThread().getId();
    }
}
