package com.ulfy.android.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Objects;

/**
 * 网络任务，该任务配有网络访问的常用配置。目前网络任务只对无网络的情况机型拦截，
 * 如果存在网络则正常执行代理任务，否则会响应一个无网络回调。
 *
 * @see Transponder#onTranspondMessage(Message)
 * @see Message#TYPE_NO_NET_CONNECTION
 * @see Message#TYPE_NET_ERROR
 */
public final class NetUiTask extends UiTask {
    private UiTask proxyTask;           // 被代理的任务（必须存在）
    private Transponder transponder;    // 网络任务执行过程中的应答器，没有则不执行响应

    /**
     * 构造方法，传入一个被代理的任务和网络任务响应器
     */
    public NetUiTask(Context context, UiTask proxyTask, Transponder transponder) {
        super(context);
        Objects.requireNonNull(proxyTask, "proxy task can not be null");
        this.proxyTask = proxyTask;
        this.transponder = transponder;
    }

    @SuppressLint("MissingPermission") @Override protected void run(Task task) {
        // 不执行已经取消更新 ui 的任务
        if (isCancelUiHandler()) {
            return;
        }

        // 获取网络连接信息
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        // 没有打开网络连接开关 或 网络连接开关已经打开，但是无网络连接
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            if (transponder != null) {		// 没有设置响应器则不执行
                runOnUiThread(new Runnable() {  // 执行无网络回调
                    @Override public void run() {
                        transponder.onTranspondMessage(new Message(Message.TYPE_NO_NET_CONNECTION, TaskConfig.Config.NO_NET_CONNECTION_TIP));
                    }
                });
            }
        }

        // 有网络但是非 wifi
        // else if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) { }

        // 有网络并且是 wifi
        else if (!isCancelUiHandler()) {
            try {
                proxyTask.run();
            } catch (final Exception e) {
                if (transponder != null) {		// 没有设置响应器则不执行
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            transponder.onTranspondMessage(new Message(Message.TYPE_NET_ERROR, e.getMessage()));
                        }
                    });
                }
            }
        }
    }

    @Override public synchronized final void setCancelUiHandler(boolean cancelUiHandler) {
        super.setCancelUiHandler(cancelUiHandler);
        if (proxyTask != null) {
            proxyTask.setCancelUiHandler(cancelUiHandler);
        }
    }

    @Override public synchronized final boolean isCancelUiHandler() {
        return proxyTask == null ? super.isCancelUiHandler() : super.isCancelUiHandler() && proxyTask.isCancelUiHandler();
    }
}
