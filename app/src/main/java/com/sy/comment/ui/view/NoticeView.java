package com.sy.comment.ui.view;
import android.content.Context;
import android.util.AttributeSet;

import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.vm.NoticeVM;
import com.sy.comment.ui.base.BaseView;
@Layout(id = R.layout.view_notice)
public class NoticeView extends BaseView {
    private NoticeVM vm;
    public NoticeView(Context context) {
        super(context);
        init(context, null);
    }
    public NoticeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    private void init(Context context, AttributeSet attrs) {
    }
    @Override public void bind(IViewModel model) {
        vm = (NoticeVM) model;
    }
}