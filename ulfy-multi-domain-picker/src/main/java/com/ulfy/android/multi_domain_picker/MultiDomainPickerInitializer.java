package com.ulfy.android.multi_domain_picker;

import android.content.Context;

import androidx.startup.Initializer;

import com.ulfy.android.cache.CacheInitializer;

import java.util.Arrays;
import java.util.List;

public final class MultiDomainPickerInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        MultiDomainPickerConfig.init(context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Arrays.asList(CacheInitializer.class);
    }
}
