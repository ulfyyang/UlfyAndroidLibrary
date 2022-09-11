package com.ulfy.android.utils;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public final class UtilsInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        UtilsConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
