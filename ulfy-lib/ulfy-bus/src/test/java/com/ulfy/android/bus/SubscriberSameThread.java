package com.ulfy.android.bus;

public class SubscriberSameThread {
    public long threadId;

    @Subscribe(mode = MethodMode.SAME_THREAD) public void Event(Event1 event) {
        threadId = Thread.currentThread().getId();
    }
}
