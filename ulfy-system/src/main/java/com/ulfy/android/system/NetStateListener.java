package com.ulfy.android.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import com.ulfy.android.bus.BusUtils;
import com.ulfy.android.system.event.OnNetworkStateChangedEvent;

/**
 * 系统级别的广播接收器，用于将安卓系统的部分系统广播转换为App级别的全局事件
 * 接收网络状态变化事件只能在7.0以下版本使用，在7.0版本之上将不会接收到该广播时间
 */
public final class NetStateListener extends BroadcastReceiver {
    static boolean connected;      // 当前网络是否链接

    // 在5.0之下使用广播的方式监听网络状态的变化
    @Override public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                postEventOnNetStateChanged();
            }
        }
    }

    // 在5.0之上使用新的方式监听网络状态的变化
    public static void listenNetStateChanged(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manager != null) {
                manager.registerNetworkCallback(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                    public void onAvailable(Network network) {
                        postEventOnNetStateChanged();
                    }
                    public void onLost(Network network) {
                        postEventOnNetStateChanged();
                    }
                });
            }
        }
        // 该方法会在程序初始化时调用，因此这里用作初始化链接状态的位置
        connected = getConnectedState();
    }

    /**
     * 当网络状态发生变化的时候调用更该方法发布事件
     */
    synchronized static void postEventOnNetStateChanged() {
        if (connected != getConnectedState()) {
            connected = getConnectedState();
            BusUtils.post(new OnNetworkStateChangedEvent(connected));
        }
    }

    private static boolean getConnectedState() {
        ConnectivityManager manager = (ConnectivityManager) SystemConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {      // 找不到连接管理器则默认用户是连接的
            return true;
        } else {
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }
}
