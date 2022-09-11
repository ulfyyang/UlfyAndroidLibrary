package com.ulfy.android.cache;

import android.content.Context;

import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public final class CacheInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        CacheConfig.initDefaultCache(context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
