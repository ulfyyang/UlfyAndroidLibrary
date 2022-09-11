package com.ulfy.android.system;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import com.ulfy.android.bus.BusInitializer;
import com.ulfy.android.dialog.DialogInitializer;

import java.util.Arrays;
import java.util.List;

public final class SystemInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        SystemConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Arrays.asList(BusInitializer.class, DialogInitializer.class);
    }
}
