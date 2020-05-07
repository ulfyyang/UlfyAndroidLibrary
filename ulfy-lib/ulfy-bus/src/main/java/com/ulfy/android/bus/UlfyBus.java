package com.ulfy.android.bus;

import android.content.Context;

/**
 * 事件总线
 */
class UlfyBus {

    //-- 单例 start --

    private static final UlfyBus instance = new UlfyBus();

    UlfyBus() {}

    static UlfyBus getDefault() {
        return instance;
    }

    static UlfyBus with(Context context) {
        return BusRepository.getInstance().findBusByContext(context);
    }

    //-- 单例 end --

    final SubscribeInfo subscribeInfo = new SubscribeInfo();
    private Poster mainPoster = new MainPoster();
    private Poster sameThreadPoster = new SameThreadPoster();
    private Poster backgroundPoster = new BackgroundPoster();
    private Poster asyncPoster = new AsyncPoster();

    /**
     * 注册一个订阅对象
     */
    void register(Object subscriber) {
        if (subscriber != null) {
            subscribeInfo.registerSubscriber(subscriber);
        }
    }

    /**
     * 取消注册一个订阅对象
     */
    void unregister(Object subscriber) {
        if (subscriber != null) {
            subscribeInfo.unregisterSubscriber(subscriber);
        }
    }

    /**
     * 发送一个订阅事件
     * 无论当前线程是否是 主线程，提交一个事件都会被派发器进行处理
     * 派发器会把代码派发到合适的线程中
     *
     * 处理一个事件的过程被定义成一个任务，由于任务是经常重复出现的东西，因此应该使用一个池来缓存任务对象
     * 一个任务要在一个工作线程中执行，多个线程工作则引入了线程池
     */
    void post(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("can not post a null event");
        }
        // 找到所有订阅了该事件的订阅者
        for (Object subscriber : subscribeInfo.findSubscribersByEventClazz(event.getClass())) {
            // 找到所有该订阅者订阅该事件的方法
            for (SubscribeMethod subscribeMethod : subscribeInfo.findSubscribeMethodOfSubscriberByEventClazz(subscriber.getClass(), event.getClass())) {
                // 生成即将发布的执行，将执行订阅者的订阅方法，以event作为参数
                PendingPost pendingPost = PendingPost.obtainPendingPost(subscribeMethod, subscriber, event);
                // 根据订阅类型在不同的线程中执行
                switch (subscribeMethod.getMode()) {
                    case MethodMode.MAIN:
                        mainPoster.post(pendingPost);
                        break;
                    case MethodMode.SAME_THREAD:
                        sameThreadPoster.post(pendingPost);
                        break;
                    case MethodMode.BACKGROUND:
                        backgroundPoster.post(pendingPost);
                        break;
                    case MethodMode.ASYNC:
                        asyncPoster.post(pendingPost);
                        break;
                }
            }
        }
    }

}
