package com.ulfy.android.bus;

import android.os.Looper;

public class SubscriberMainThread {
    public boolean executedOnMainThread;

    @Subscribe public void Event(Event1 event) {
        executedOnMainThread = Looper.getMainLooper() == Looper.myLooper();
    }
}
