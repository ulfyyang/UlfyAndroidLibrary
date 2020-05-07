package com.bumptech.glide.integration.okhttp3;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.model.GlideUrl;

import java.io.InputStream;

public class OkHttpGlideModule implements com.bumptech.glide.module.GlideModule {
    @Override public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        // Do nothing.
    }
    @Override public void registerComponents(Context context, Glide glide, Registry registry) {
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
    }
}