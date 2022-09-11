package com.ulfy.android.task;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;

public class TaskInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        TaskConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
