package com.ulfy.android.utils;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

class RecycledViewPoolRepository {
    private static final RecycledViewPoolRepository instance = new RecycledViewPoolRepository();
    private Map<Context, RecyclerView.RecycledViewPool> recycledViewPoolMap = new HashMap<>();

    private RecycledViewPoolRepository() { }

    static RecycledViewPoolRepository getInstance() {
        return instance;
    }

    synchronized RecyclerView.RecycledViewPool findRecycledViewPoolFromContext(Context context) {
        RecyclerView.RecycledViewPool pool = recycledViewPoolMap.get(context);
        if (pool == null) {
            pool = new RecyclerView.RecycledViewPool();
            recycledViewPoolMap.put(context, pool);
        }
        return pool;
    }

    synchronized void releasePoolOnActivityDestoryed(Context context) {
        recycledViewPoolMap.remove(context);
    }
}
