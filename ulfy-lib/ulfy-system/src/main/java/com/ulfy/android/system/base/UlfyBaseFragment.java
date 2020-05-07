package com.ulfy.android.system.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.bus.Subscribe;
import com.ulfy.android.system.event.OnNetworkStateChangedEvent;
import com.ulfy.android.ui_injection.InjectUtils;

public abstract class UlfyBaseFragment extends UlfyBaseVisibilityFragment {
    private boolean isFirstVisable;     // 用于标记当前页面是否已经显示了一次

    ///////////////////////////////////////////////////////////////////////////
    //      布局文件处理和事件总线处理
    ///////////////////////////////////////////////////////////////////////////

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layoutId = InjectUtils.findUILayoutFileID(this.getClass());
        if (layoutId == -1) {
            return null;
        } else {
            View view = inflater.inflate(layoutId, container, false);
            InjectUtils.processViewById(view, this);
            InjectUtils.processViewClick(view, this);
            return view;
        }
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusUtils.register(getActivity(), this);
        BusUtils.register(this);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        BusUtils.unregister(getActivity(), this);
        BusUtils.unregister(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    //      可见性处理
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 当可见性改变时的回调
     */
    @Override protected void onVisibilityChanged(boolean visible) {
        super.onVisibilityChanged(visible);
        if (visible) {
            if (!isFirstVisable) {
                isFirstVisable = true;
                onVisibleFirstToUser();
            }
            onVisiableToUser();
        }
    }

    /**
     * 当用户可见时的回调
     */
    public void onVisiableToUser() {}

    /**
     * 当第一次用户可见时的回调
     */
    public void onVisibleFirstToUser() {}

    ///////////////////////////////////////////////////////////////////////////
    //      网络重连处理
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 当网络重新链接后调用onNetworkConnected回调
     */
    @Subscribe private void OnNetworkStateChangedEvent(OnNetworkStateChangedEvent event) {
        if (event.connected) {
            onNetworkReconnected();
        }
    }

    protected void onNetworkReconnected() {}
}
