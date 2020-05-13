package com.ulfy.android.bus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class BackgroundPoster implements Poster {
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override public void post(final PendingPost pendingPost) {
        executor.execute(new Runnable() {
            @Override
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
