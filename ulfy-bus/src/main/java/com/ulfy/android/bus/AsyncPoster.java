package com.ulfy.android.bus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class AsyncPoster implements Poster {
	private Executor executer = Executors.newCachedThreadPool();

	@Override public void post(final PendingPost pendingPost) {
		executer.execute(new Runnable() {
			public void run() {
				try {
					pendingPost.invoke();
				} finally {
					PendingPost.releasePendingPost(pendingPost);
				}
			}
		});
	}
}
