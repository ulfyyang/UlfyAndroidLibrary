package com.ulfy.android.ui_linkage;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 输入框文字数量联动，该联动用于统一不断变化的输入框文字的数量
 */
public final class EditTextWordCountLinkage {

    public interface OnWordCountChageListener {
        void onTextWordCountChange(int wordCount);
    }

    private EditText editText;
    private OnWordCountChageListener onWordCountChageListener;

    public EditTextWordCountLinkage(OnWordCountChageListener onWordCountChageListener, EditText editText) {
        this.onWordCountChageListener = onWordCountChageListener;
        this.editText = editText;
        bindListener();
    }

    private void bindListener() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (onWordCountChageListener != null) {
                    onWordCountChageListener.onTextWordCountChange(editText.length());
                }
            }
        });
    }

}
