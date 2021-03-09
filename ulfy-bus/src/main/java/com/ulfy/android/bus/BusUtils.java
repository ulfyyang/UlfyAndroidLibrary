package com.ulfy.android.bus;

import android.content.Context;

/**
 * 事件总线工具类
 */
public final class BusUtils {

    /**
     * 将接收者注册到默认的全局事件总线中
     */
    public static void register(Object subscriber) {
        BusConfig.throwExceptionIfConfigNotConfigured();
        UlfyBus.getDefault().register(subscriber);
    }

    /**
     * 将接收者注册到和上下文相关的事件总线中
     */
    public static void register(Context context, Object subscriber) {
        BusConfig.throwExceptionIfConfigNotConfigured();
        UlfyBus.with(context).register(subscriber);
    }

    /**
     * 将接收者从全局事件总线中取消注册
     */
    public static void unregister(Object subscriber) {
        BusConfig.throwExceptionIfConfigNotConfigured();
        UlfyBus.getDefault().unregister(subscriber);
    }

    /**
     * 将接收者从上下文相关的事件总线中取消注册
     */
    public static void unregister(Context context, Object subscriber) {
        BusConfig.throwExceptionIfConfigNotConfigured();
        UlfyBus.with(context).unregister(subscriber);
    }

    /**
     * 发布事件到全局总线中
     */
    public static void post(Object event) {
        BusConfig.throwExceptionIfConfigNotConfigured();
        UlfyBus.getDefault().post(event);
    }

    /**
     * 发布事件到上下文相关的总线中
     */
    public static void post(Context context, Object event) {
        BusConfig.throwExceptionIfConfigNotConfigured();
        UlfyBus.with(context).post(event);
    }
}
