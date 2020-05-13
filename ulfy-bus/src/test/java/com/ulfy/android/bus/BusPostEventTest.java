package com.ulfy.android.bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLooper;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class BusPostEventTest {
    private UlfyBus ulfyBus;

    @Before public void initUlfyBus() {
        ulfyBus = new UlfyBus();
    }

    /**
     * 测试共有方法和私有方法的执行
     */
    @Test public void testPublicPrivateMethodExecute() {
        SubscriberPulicPrivate subscriber = new SubscriberPulicPrivate();

        ulfyBus.register(subscriber);

        ulfyBus.post(new Event1());
        ulfyBus.post(new Event2());

        assertTrue(subscriber.publicExecuted);
        assertTrue(subscriber.privateExecuted);
    }

    /**
     * 测试同一事件多方法订阅
     */
    @Test public void testOneEventMoreSubscribeMethods() {
        SubscriberOneEventMoreMethods subscriber = new SubscriberOneEventMoreMethods();

        ulfyBus.register(subscriber);

        ulfyBus.post(new Event1());

        assertTrue(subscriber.public1Executed);
        assertTrue(subscriber.public2Executed);
    }

    /**
     * 在任意线程中发布事件在主线程订阅
     */
    @Test public void testPostEventToMainThread() throws InterruptedException {
        SubscriberMainThread subscriber = new SubscriberMainThread();

        ulfyBus.register(subscriber);

        postEventOnNewThread(ulfyBus, new Event1());
        ShadowLooper.runMainLooperOneTask();

        assertTrue(subscriber.executedOnMainThread);
    }

    /**
     * 在相同线程发布相同订阅
     */
    @Test public void testPostEventToSameThread() throws InterruptedException {
        SubscriberSameThread subscriber = new SubscriberSameThread();

        ulfyBus.register(subscriber);

        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                ulfyBus.post(new Event1());
            }
        });
        thread.start();
        thread.join();

        assertEquals(thread.getId(), subscriber.threadId);
    }

    /**
     * 在任意线程中发布到后台线程（只有一个固定的后台线程）
     */
    @Test public void testPostEventToBackgroundThread() throws InterruptedException {
        SubscriberBackgroundThread subscriber1 = new SubscriberBackgroundThread();
        SubscriberBackgroundThread subscriber2 = new SubscriberBackgroundThread();

        ulfyBus.register(subscriber1);
        ulfyBus.register(subscriber2);

        postEventOnNewThread(ulfyBus, new Event1());

        assertEquals(subscriber1.threadId, subscriber2.threadId);
    }

    /**
     * 在任意线程中发布到任意线程
     */
    @Test public void testPostEventToAsyncThread() throws InterruptedException {
        SubscriberAsyncThread subscriber1 = new SubscriberAsyncThread();
        SubscriberAsyncThread subscriber2 = new SubscriberAsyncThread();

        ulfyBus.register(subscriber1);
        ulfyBus.register(subscriber2);

        postEventOnNewThread(ulfyBus, new Event1());
        Thread.sleep(500);

        assertNotEquals(subscriber1.threadId, subscriber2.threadId);
    }

    private void postEventOnNewThread(final UlfyBus ulfyBus, final Object event) throws InterruptedException {
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                ulfyBus.post(event);
            }
        });
        thread.start();
        thread.join();
    }

}
