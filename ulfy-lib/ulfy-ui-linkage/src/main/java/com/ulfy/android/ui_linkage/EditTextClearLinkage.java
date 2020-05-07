package com.ulfy.android.ui_linkage;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * 输入框自动清除联动
 * 该联动会关联一个输入框和一个点击清除输入框内容的View
 */
public final class EditTextClearLinkage {
    private EditText editText;
    private View clearView;
    private OnClearClickListener onClearClickListener;
    private OnClearListener onClearListener;

    public EditTextClearLinkage(EditText editText, View clearView) {
        this.editText = editText;
        this.clearView = clearView;
        updateState();
        bindListener();
    }

    private void bindListener() {
        this.editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateState();
            }
        });
        this.clearView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (onClearClickListener != null) {
                    onClearClickListener.onClearClick(EditTextClearLinkage.this, clearView);
                }
                editText.setText("");
            }
        });
    }

    private void updateState() {
        clearView.setVisibility(UiUtils.isEmpty(editText) ? View.INVISIBLE : View.VISIBLE);
        if (onClearListener != null && UiUtils.isEmpty(editText)) {
            onClearListener.onClear(this);
        }
    }

    public EditTextClearLinkage setOnClearClickListener(OnClearClickListener onClearClickListener) {
        this.onClearClickListener = onClearClickListener;
        return this;
    }

    public EditTextClearLinkage setOnClearListener(OnClearListener onClearListener) {
        this.onClearListener = onClearListener;
        return this;
    }

    /**
     * 当点击清空按钮时的监听
     */
    public interface OnClearClickListener {
        public void onClearClick(EditTextClearLinkage linkage, View cliearView);
    }

    /**
     * 当输入框被清空时的监听
     */
    public interface OnClearListener {
        public void onClear(EditTextClearLinkage linkage);
    }
}
