package com.ulfy.android.bus;

import java.util.LinkedList;
import java.util.List;

class PendingPost {
	// 缓存池大小
	private static final int POOL_SIZE = 500;
	// 缓存池
	private static final List<PendingPost> POST_POOL = new LinkedList<>();
	/*
	执行需要的信息：方法、目标对象、方法参数
	 */
	private SubscribeMethod subscribeMethod;
	private Object subscriber;
	private Object event;
	
	private void init() {
		subscribeMethod = null;
		subscriber = null;
		event = null;
	}

	/**
	 * 从缓存池中取出
	 */
	static synchronized PendingPost obtainPendingPost(SubscribeMethod subscribeMethod, Object subscriber, Object event) {
		PendingPost post;
		if (POST_POOL.size() > 0) {
			post = POST_POOL.remove(POST_POOL.size() - 1);
		} else {
			post = new PendingPost();
		}
		post.subscribeMethod = subscribeMethod;
		post.subscriber = subscriber;
		post.event = event;
		return post;
	}

	/**
	 * 归还到缓存池中
	 */
	static synchronized void releasePendingPost(PendingPost post) {
		post.init();
		if (POST_POOL.size() < POOL_SIZE) {
			POST_POOL.add(post);
		}
	}

	/**
	 * 执行一个
	 */
	void invoke() {
		boolean accessible = subscribeMethod.getMethod().isAccessible();
		try {
			subscribeMethod.getMethod().setAccessible(true);
			subscribeMethod.getMethod().invoke(subscriber, event);
		} catch (Exception e) {
			throw new IllegalStateException("invoke method failed", e);
		} finally {
			subscribeMethod.getMethod().setAccessible(accessible);
		}
	}
}
