package com.ulfy.android.system.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ulfy.android.bus.Subscribe;
import com.ulfy.android.system.ActivityUtils;
import com.ulfy.android.system.event.OnNetworkStateChangedEvent;

public abstract class UlfyBaseActivity extends FragmentActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.onCreated(this);
    }

    @Override protected void onStart() {
        super.onStart();
        ActivityUtils.onStart(this);
    }

    @Override protected void onStop() {
        super.onStop();
        ActivityUtils.onStop(this);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        ActivityUtils.onDestroy(this);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityUtils.onActivityResultForReceiveData(this, requestCode, resultCode, data);
    }

    /**
     * 当网络重新链接后调用onNetworkConnected回调
     */
    @Subscribe private void OnNetworkStateChangedEvent(OnNetworkStateChangedEvent event) {
        if (event.connected) {
            onNetworkReconnected();
        }
    }

    protected void onNetworkReconnected() {}

    public UlfyBaseActivity getContext() {
        return this;
    }
}

