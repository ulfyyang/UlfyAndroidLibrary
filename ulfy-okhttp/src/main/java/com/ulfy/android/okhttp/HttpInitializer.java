package com.ulfy.android.okhttp;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public final class HttpInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        HttpConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
