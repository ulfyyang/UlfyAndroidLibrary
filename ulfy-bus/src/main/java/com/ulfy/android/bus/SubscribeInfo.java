package com.ulfy.android.bus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SubscribeInfo {
	/*
	一个事件有多个订阅者订阅
	一个订阅者可以订阅多个事件
	 */
	// 记录订阅者对应的相关描述信息
	private Map<Class<?>, SubscriberEventMethodPairs> subscriberEventMethodPairsMap = new HashMap<>();
	// 记录事件对应的众多订阅者，该记录表示订阅者是否能收到事件
	private Map<Class<?>, Set<Object>> eventSubscriberPairsMap = new HashMap<>();

	void registerSubscriber(Object subscriber) {
		// 生成订阅者信息，并将订阅者信息放到订阅者对应的关联中
		SubscriberEventMethodPairs subscriberEventMethodPairs = recordSubscriberEventMethodPairsIfNotExist(subscriber);
		// 将订阅这信息分配到事件对应的组中
		allocateSubscriberToEventSubscribersMapIfNeed(subscriber, subscriberEventMethodPairs);
	}

	private SubscriberEventMethodPairs recordSubscriberEventMethodPairsIfNotExist(Object subscriber) {
		SubscriberEventMethodPairs info = subscriberEventMethodPairsMap.get(subscriber.getClass());
		if (info == null) {
			info = new SubscriberEventMethodPairs(subscriber);
			subscriberEventMethodPairsMap.put(subscriber.getClass(), info);
		}
		return info;
	}

	private void allocateSubscriberToEventSubscribersMapIfNeed(Object subscriber, SubscriberEventMethodPairs subscriberEventWithMethods) {
		for (Class<?> eventClazz : subscriberEventWithMethods.getAllEventClazzs()) {
			Set<Object> subscribers = eventSubscriberPairsMap.get(eventClazz);
			if (subscribers == null) {
				subscribers = new HashSet<>();
				eventSubscriberPairsMap.put(eventClazz, subscribers);
			}
			subscribers.add(subscriber);
		}
	}

	void unregisterSubscriber(Object subscriber) {
    	/*
    	 * 取消订阅者订阅并不会取消记录的订阅者中关于事件和方法描述的信息，这些信息即使一直在内存中也不会占用
    	 * 多少内存。只是会取消该订阅者在对应的所有订阅事件上的绑定，在之后发送事件时，由于已经接触了绑定，
    	 * 因此该订阅者将不在收到订阅事件的通知
    	 */
		SubscriberEventMethodPairs info = subscriberEventMethodPairsMap.get(subscriber.getClass());
		if (info != null) {
			for (Class<?> eventClazz : info.getAllEventClazzs()) {
				Set<Object> subscribers = eventSubscriberPairsMap.get(eventClazz);
				subscribers.remove(subscriber);
			}
		}
	}

	Set<Object> findAllSubscribers() {
		Set<Object> allSubscribers = new HashSet<>();
		for (Set<Object> subscribers : eventSubscriberPairsMap.values()) {
			allSubscribers.addAll(subscribers);
		}
		return allSubscribers;
	}

	Set<Object> findSubscribersByEventClazz(Class<?> eventClazz) {
		if (eventClazz == null) {
			return new HashSet<>();
		}
		Set<Object> subscribers = eventSubscriberPairsMap.get(eventClazz);
		if (subscribers == null) {
			return new HashSet<>();
		}
		return subscribers;
	}

	Set<SubscribeMethod> findAllSubscribeMethodsOfSubscriber(Class<?> subscriberClazz) {
		SubscriberEventMethodPairs subscriberEventMethodPairs = subscriberEventMethodPairsMap.get(subscriberClazz);
		if (subscriberEventMethodPairs == null) {
			return new HashSet<>();
		} else {
			return subscriberEventMethodPairs.getAllSubscribeMethods();
		}
	}

	Set<SubscribeMethod> findSubscribeMethodOfSubscriberByEventClazz(Class<?> subscriberClazz, Class<?> eventClazz) {
		if (subscriberClazz == null || eventClazz == null) {
			return new HashSet<>();
		}
		SubscriberEventMethodPairs subscriberEventMethodPairs = subscriberEventMethodPairsMap.get(subscriberClazz);
		if (subscriberEventMethodPairs == null) {
			return new HashSet<>();
		}
		Set<SubscribeMethod> subscribeMethods = subscriberEventMethodPairs.getSubscribeMethodsByEventClazz(eventClazz);
		if (subscribeMethods == null) {
			return new HashSet<>();
		}
		return subscribeMethods;
	}
}
