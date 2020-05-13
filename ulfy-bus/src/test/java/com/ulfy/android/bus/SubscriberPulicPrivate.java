package com.ulfy.android.bus;

public class SubscriberPulicPrivate {
    public boolean publicExecuted;
    public boolean privateExecuted;

    @Subscribe public void publicEvent(Event1 event) {
        this.publicExecuted = true;
    }

    @Subscribe private void privateEvent(Event2 event2) {
        this.privateExecuted = true;
    }
}
