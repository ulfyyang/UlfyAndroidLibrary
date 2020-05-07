package com.ulfy.android.bus;

import java.lang.reflect.Method;

class SubscribeMethod {
    private Method method;
    private int mode;

    SubscribeMethod(Method method, int mode) {
        this.method = method;
        this.mode = mode;
    }

	Method getMethod() {
		return method;
	}

	int getMode() {
		return mode;
	}
}