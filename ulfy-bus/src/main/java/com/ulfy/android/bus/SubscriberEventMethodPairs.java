package com.ulfy.android.bus;

import android.app.Activity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SubscriberEventMethodPairs {
	private Map<Class<?>, Set<SubscribeMethod>> eventWithMethods = new HashMap<>();
	
	SubscriberEventMethodPairs(Object subscriber) {
		for (Method method : findSubscribMethods(subscriber.getClass(), new HashSet<Method>())) {
			if (method.getParameterTypes().length != 1) {
				throw new IllegalArgumentException("Subscribe Method must have only one parameter");
			}
			Class<?> eventClazz = method.getParameterTypes()[0];
			Set<SubscribeMethod> subscribeMethods = eventWithMethods.get(eventClazz);
			if (subscribeMethods == null) {
				subscribeMethods = new HashSet<>();
				eventWithMethods.put(eventClazz, subscribeMethods);
			}
			subscribeMethods.add(new SubscribeMethod(method, method.getAnnotation(Subscribe.class).mode()));
		}
	}

	private Set<Method> findSubscribMethods(Class<?> clazz, Set<Method> methodSet) {
		if (clazz == Activity.class || clazz == FragmentActivity.class || clazz == android.app.Fragment.class || clazz == Fragment.class || clazz == Object.class) {
			return methodSet;
		}
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Subscribe.class)) {
				methodSet.add(method);
			}
		}
		return findSubscribMethods(clazz.getSuperclass(), methodSet);
	}

	Set<Class<?>> getAllEventClazzs() {
		return eventWithMethods.keySet();
	}

	Set<SubscribeMethod> getAllSubscribeMethods() {
		Set<SubscribeMethod> allSubscribeMethods = new HashSet<>();
		for (Set<SubscribeMethod> subscribeMethods : eventWithMethods.values()) {
			allSubscribeMethods.addAll(subscribeMethods);
		}
		return allSubscribeMethods;
	}

	Set<SubscribeMethod> getSubscribeMethodsByEventClazz(Class<?> eventClazz) {
		return eventWithMethods.get(eventClazz);
	}
}
