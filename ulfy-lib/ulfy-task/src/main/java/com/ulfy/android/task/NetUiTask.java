package com.ulfy.android.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络任务
 * 该任务配有网络访问的常用配置
 */
public final class NetUiTask extends UiTask {
    private Context context;
    private UiTask proxyTask;
    private Transponder transponder;

    public NetUiTask(Context context, UiTask proxyTask, Transponder transponder) {
        super(context);
        this.context = context;
        this.proxyTask = proxyTask;
        this.transponder = transponder;
    }

    @SuppressLint("MissingPermission") @Override protected void run(Task task) {
        // 获取网络连接信息
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // 没有打开网络连接开关 或 网络连接开关已经打开，但是无网络连接
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            runOnUiThread(new Runnable() {  // 执行无网络回调
                public void run() {
                    transponder.onTranspondMessage(new Message(Message.TYPE_NO_NET_CONNECTION, TaskConfig.Config.NO_NET_CONNECTION_TIP));
                }
            });
        }
        // 有网络但是非wifi
        // else if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) { }
        // 有网络并且是wifi
        else if (!isCancelUiHandler()) {
            try {
                proxyTask.run();
            } catch (final Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        transponder.onTranspondMessage(new Message(Message.TYPE_NET_ERROR, e.getMessage()));
                    }
                });
            }
        }
    }

    @Override public synchronized void setCancelUiHandler(boolean cancelUiHandler) {
        super.setCancelUiHandler(cancelUiHandler);
        if (proxyTask != null) {
            proxyTask.setCancelUiHandler(cancelUiHandler);
        }
    }

    @Override public synchronized boolean isCancelUiHandler() {
        return proxyTask == null ? super.isCancelUiHandler() : super.isCancelUiHandler() && proxyTask.isCancelUiHandler();
    }
}
