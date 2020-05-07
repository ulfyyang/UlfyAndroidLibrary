package com.ulfy.android.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class MainPoster extends Handler implements Poster {

	MainPoster() {
		super(Looper.getMainLooper());
	}

	@Override public void post(PendingPost pendingPost) {
		Message msg = obtainMessage();
		msg.obj = pendingPost;
		sendMessage(msg);
	}

	@Override public void handleMessage(Message msg) {
		PendingPost pendingPost = (PendingPost) msg.obj;
		try {
			pendingPost.invoke();
		} finally {
			PendingPost.releasePendingPost(pendingPost);
		}
	}

}
