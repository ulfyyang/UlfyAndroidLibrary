package com.ulfy.android.bus;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public final class BusInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        BusConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
