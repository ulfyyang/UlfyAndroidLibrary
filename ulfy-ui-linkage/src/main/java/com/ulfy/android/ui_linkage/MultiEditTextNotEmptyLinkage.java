package com.ulfy.android.ui_linkage;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 可以通过注册监听器实时获取状态，也可以通过isAllNotEmpty方法获取
 */
public final class MultiEditTextNotEmptyLinkage {

    public interface OnEditChangeListener {
        void onAllEditChange(boolean isAllNotEmpty);
    }

    private EditText[] editTexts;
    private OnEditChangeListener onEditChangeListener;
    private boolean isAllNotEmpty;

    public MultiEditTextNotEmptyLinkage(EditText... editTexts) {
        this(null, editTexts);
    }

    public MultiEditTextNotEmptyLinkage(OnEditChangeListener onEditChangeListener, EditText... editTexts) {
        this.onEditChangeListener = onEditChangeListener;
        this.editTexts = editTexts;
        bindListener();
        executeCallback();
    }

    private void bindListener() {
        class TextWatcherImpl implements TextWatcher {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                executeCallback();
            }
        }
        for (EditText editText : editTexts) {
            editText.addTextChangedListener(new TextWatcherImpl());
        }
    }

    private void executeCallback() {
        if (onEditChangeListener != null) {
            isAllNotEmpty = judgeAllNotEmpty();
            onEditChangeListener.onAllEditChange(isAllNotEmpty);
        }
    }

    private boolean judgeAllNotEmpty() {
        for (EditText editText : editTexts) {
            if (UiUtils.isEmpty(editText)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllNotEmpty() {
        return isAllNotEmpty;
    }

}
