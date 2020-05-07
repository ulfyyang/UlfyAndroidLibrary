package com.ulfy.android.ui_linkage;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class PasswordShowHideLinkage {
    private int openEyeId;
    private int closeEyeId;
    private EditText passwordET;
    private ImageView eyeIV;
    private boolean isShowPassword;

    public PasswordShowHideLinkage(int openEyeId, int closeEyeId, EditText passwordET, ImageView eyeIV) {
        this.openEyeId = openEyeId;
        this.closeEyeId = closeEyeId;
        this.passwordET = passwordET;
        this.eyeIV = eyeIV;
        refresh();
        eyeIV.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                isShowPassword = !isShowPassword;
                refresh();
            }
        });
    }

    private void refresh() {
        eyeIV.setImageResource(isShowPassword ? openEyeId : closeEyeId);
        int selectionStart = passwordET.getSelectionStart();
        passwordET.setTransformationMethod(isShowPassword ? HideReturnsTransformationMethod.getInstance() : PasswordTransformationMethod.getInstance());
        passwordET.setSelection(selectionStart);
    }
}
