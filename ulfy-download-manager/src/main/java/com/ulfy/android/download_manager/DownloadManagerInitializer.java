package com.ulfy.android.download_manager;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import com.ulfy.android.bus.BusInitializer;
import com.ulfy.android.cache.CacheInitializer;

import java.util.Arrays;
import java.util.List;

public final class DownloadManagerInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        DownloadManagerConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Arrays.asList(CacheInitializer.class, BusInitializer.class);
    }
}
