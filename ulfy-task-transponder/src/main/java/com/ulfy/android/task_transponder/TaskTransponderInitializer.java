package com.ulfy.android.task_transponder;

import android.app.Application;
import android.content.Context;

import androidx.startup.Initializer;

import com.ulfy.android.task.TaskInitializer;

import java.util.Arrays;
import java.util.List;

public final class TaskTransponderInitializer implements Initializer<Void> {
    @Override public Void create(Context context) {
        TaskTransponderConfig.init((Application) context);
        return null;
    }
    @Override public List<Class<? extends Initializer<?>>> dependencies() {
        return Arrays.asList(TaskInitializer.class);
    }
}
