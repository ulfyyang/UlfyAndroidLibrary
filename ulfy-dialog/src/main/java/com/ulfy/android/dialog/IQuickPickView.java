package com.ulfy.android.dialog;


import java.util.List;

public interface IQuickPickView {
    public IQuickPickView setDialogId(String dialogId);
    public IQuickPickView setTitle(CharSequence title);
    public IQuickPickView setData(List<CharSequence> list);
    public IQuickPickView setData(List<CharSequence> list, boolean is_comment);
    public IQuickPickView setOnItemClickListener(OnItemClickListener onItemClickListener);

    public interface OnItemClickListener {
        public void onItemClick(int index, String item);
    }
}
