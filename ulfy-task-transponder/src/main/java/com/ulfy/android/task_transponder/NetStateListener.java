package com.ulfy.android.task_transponder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 系统级别的广播接收器，用于将安卓系统的部分系统广播转换为App级别的全局事件
 * 接收网络状态变化事件只能在7.0以下版本使用，在7.0版本之上将不会接收到该广播时间
 */
public final class NetStateListener extends BroadcastReceiver {
    static boolean connected;      // 当前网络是否链接
    private static List<View> viewList = new ArrayList<>();

    // 在5.0之下使用广播的方式监听网络状态的变化
    @Override public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                onNetStateChanged();
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
                        onNetStateChanged();
                    }
                    public void onLost(Network network) {
                        onNetStateChanged();
                    }
                });
            }
        }
        // 该方法会在程序初始化时调用，因此这里用作初始化链接状态的位置
        connected = getConnectedState();
    }

    synchronized static void registerView(View view) {
        viewList.add(view);
    }

    synchronized static void unregisterView(View view) {
        Iterator<View> iterator = viewList.iterator();
        while (iterator.hasNext()) {
            if (view == iterator.next()) {
                iterator.remove();
            }
        }
    }

    /**
     * 当网络状态发生变化的时候调用更该方法发布事件
     */
    private synchronized static void onNetStateChanged() {
        try {
            if (connected != getConnectedState()) {
                connected = getConnectedState();
                if (connected) {
                    for (View view : viewList) {
                        if (view instanceof IReloadView) {
                            ((IReloadView) view).reload();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean getConnectedState() {
        ConnectivityManager manager = (ConnectivityManager) TaskTransponderConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {      // 找不到连接管理器则默认用户是连接的
            return true;
        } else {
            NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }
}
