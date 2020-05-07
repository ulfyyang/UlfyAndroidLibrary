package com.ulfy.android.bus;

public class SubscriberOneEventMoreMethods {
    public boolean public1Executed;
    public boolean public2Executed;

    @Subscribe public void public1Event(Event1 event) {
        this.public1Executed = true;
    }
    @Subscribe public void public2Event(Event1 event) {
        this.public2Executed = true;
    }
}
