package com.ulfy.android.bus;

class SameThreadPoster implements Poster {

	@Override public void post(PendingPost pendingPost) {
		try {
			pendingPost.invoke();
		} finally {
			PendingPost.releasePendingPost(pendingPost);
		}
	}

}
