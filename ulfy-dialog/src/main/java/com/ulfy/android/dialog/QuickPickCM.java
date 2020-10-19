package com.ulfy.android.dialog;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

public final class QuickPickCM implements IViewModel {
    public CharSequence text;
    public boolean is_comment;

    public QuickPickCM(CharSequence text) {
        this.text = text;
    }

    public QuickPickCM(CharSequence text, boolean is_comment) {
        this.text = text;
        this.is_comment = is_comment;
    }


    @Override public Class<? extends IView> getViewClass() {
        return QuickPickCell.class;
    }
}
