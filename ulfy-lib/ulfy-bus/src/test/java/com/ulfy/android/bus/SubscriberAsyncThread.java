package com.ulfy.android.bus;

public class SubscriberAsyncThread {
    public long threadId;

    @Subscribe(mode = MethodMode.ASYNC) public void Event(Event1 event) {
        threadId = Thread.currentThread().getId();
    }
}
