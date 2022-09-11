package com.ulfy.android.image;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import com.ulfy.android.cache.CacheInitializer;

import java.util.Arrays;
import java.util.List;

public final class ImageInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        ImageConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Arrays.asList(CacheInitializer.class);
    }
}
