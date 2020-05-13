package com.ulfy.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 解决多个控件争夺焦点而无法滚动的问题
 */
public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public boolean isFocused() {
        return true;
    }
}
