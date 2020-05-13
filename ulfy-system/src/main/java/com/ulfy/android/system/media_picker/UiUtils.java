package com.ulfy.android.system.media_picker;

import android.content.Context;
import android.view.View;

class UiUtils {
    /**
     * 根据view的反射类型创建view
     */
    static View createViewFromClazz(Context context, Class<? extends View> clazz) {
        if (clazz == null) {
            return null;
        }
        try {
            return clazz.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            throw new IllegalArgumentException("create view failed", e);
        }
    }
}
