package com.ulfy.android.bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BusRegisterTest {
    private UlfyBus ulfyBus;

    @Before public void initUlfyBus() {
        ulfyBus = new UlfyBus();
    }

    /**
     * 测试注册一个订阅者
     */
    @Test public void testRegisterSubscriber() {
        SubscriberRegister subscriber = new SubscriberRegister();

        ulfyBus.register(subscriber);

        int registerCountAfterRegister = countEventSubscriberInUlfyBus(ulfyBus, Event1.class);
        boolean isRegistered = isIncludeEnevtSubscriberInUlfyBus(ulfyBus, Event1.class, subscriber);
        assertEquals(1, registerCountAfterRegister);
        assertTrue(isRegistered);

        ulfyBus.unregister(subscriber);

        int registerCountAfterUnRegister = countEventSubscriberInUlfyBus(ulfyBus, Event1.class);
        boolean isUnRegistered = !isIncludeEnevtSubscriberInUlfyBus(ulfyBus, Event1.class, subscriber);
        assertEquals(0, registerCountAfterUnRegister);
        assertTrue(isUnRegistered);
    }

    /**
     * 测试对一个订阅者注册多次
     */
    @Test public void testRegsiterMoreSameSubscriber() {
        SubscriberRegister subscriber = new SubscriberRegister();

        ulfyBus.register(subscriber);
        ulfyBus.register(subscriber);
        ulfyBus.register(subscriber);

        int registerCountAfterRegister = countEventSubscriberInUlfyBus(ulfyBus, Event1.class);
        boolean isRegistered = isIncludeEnevtSubscriberInUlfyBus(ulfyBus, Event1.class, subscriber);
        assertEquals(1, registerCountAfterRegister);
        assertTrue(isRegistered);

        ulfyBus.unregister(subscriber);

        int registerCountAfterUnRegister = countEventSubscriberInUlfyBus(ulfyBus, Event1.class);
        boolean isUnRegistered = !isIncludeEnevtSubscriberInUlfyBus(ulfyBus, Event1.class, subscriber);
        assertEquals(0, registerCountAfterUnRegister);
        assertTrue(isUnRegistered);
    }

    /**
     * 测试对多个相同类型的订阅者注册一次
     */
    @Test public void testOneEventMoreTypeSubscribers() {
        SubscriberOneEventMoreSubscribers1 subscriber1 = new SubscriberOneEventMoreSubscribers1();
        SubscriberOneEventMoreSubscribers2 subscriber2 = new SubscriberOneEventMoreSubscribers2();

        ulfyBus.register(subscriber1);
        ulfyBus.register(subscriber2);

        int registerCountAfterRegister = countAllSubscribers(ulfyBus);
        assertEquals(2, registerCountAfterRegister);

        ulfyBus.unregister(subscriber1);
        ulfyBus.unregister(subscriber2);

        int registerCountAfterUnRegister = countAllSubscribers(ulfyBus);
        assertEquals(0, registerCountAfterUnRegister);
    }

    /**
     * 测试公有和私有的方法订阅
     */
    @Test public void testPublicPrivateSubscribeMethod() {
        SubscriberPulicPrivate subscriber = new SubscriberPulicPrivate();

        ulfyBus.register(subscriber);

        int allSubscribeMethodCountAfterRegisterd = countAllSubscribeMethodsOfSubscriber(ulfyBus, SubscriberPulicPrivate.class);
        assertEquals(2, allSubscribeMethodCountAfterRegisterd);
    }

    /**
     * 测试一个事件一个订阅者中有多个方法同时订阅
     */
    @Test public void testOneEventOneSubscriberMoreSubscribeMethods() {
        SubscriberOneEventMoreMethods subscriber = new SubscriberOneEventMoreMethods();

        ulfyBus.register(subscriber);

        int allSubscribeMethodCountAfterRegisterd = countAllSubscribeMethodsOfSubscriber(ulfyBus, SubscriberOneEventMoreMethods.class);
        assertEquals(2, allSubscribeMethodCountAfterRegisterd);
    }


    /**
     * 统计所有的订阅者数量
     */
    private int countAllSubscribers(UlfyBus ulfyBus) {
        return ulfyBus.subscribeInfo.findAllSubscribers().size();
    }

    /**
     * 统计订阅者的全部订阅方法数量
     */
    private int countAllSubscribeMethodsOfSubscriber(UlfyBus ulfyBus, Class<?> subscriberClazz) {
        return ulfyBus.subscribeInfo.findAllSubscribeMethodsOfSubscriber(subscriberClazz).size();
    }

    /**
     * 统计总线中某一事件的订阅者数量
     */
    private int countEventSubscriberInUlfyBus(UlfyBus ulfyBus, Class<?> eventClazz) {
        return ulfyBus.subscribeInfo.findSubscribersByEventClazz(eventClazz).size();
    }

    /**
     * 订阅者是否被注册到了总线中
     */
    private boolean isIncludeEnevtSubscriberInUlfyBus(UlfyBus ulfyBus, Class<?> eventClazz, Object matchSubscriber) {
        for (Object subscriber : ulfyBus.subscribeInfo.findSubscribersByEventClazz(eventClazz)) {
            if (subscriber == matchSubscriber) {
                return true;
            }
        }
        return false;
    }
}