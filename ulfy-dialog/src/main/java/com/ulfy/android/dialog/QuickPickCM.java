package com.ulfy.android.dialog;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.mvvm.IViewModel;

public final class QuickPickCM implements IViewModel {
    public CharSequence text;

    public QuickPickCM(CharSequence text) {
        this.text = text;
    }

    @Override public Class<? extends IView> getViewClass() {
        return QuickPickCell.class;
    }
}
