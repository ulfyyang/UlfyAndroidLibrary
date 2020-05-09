package com.sy.comment.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import com.ulfy.android.mvvm.IViewModel;
import com.ulfy.android.ui_injection.Layout;
import com.ulfy.android.ui_injection.ViewById;
import com.sy.comment.R;
import com.sy.comment.application.vm.FollowVM;
import com.sy.comment.ui.base.BaseView;

@Layout(id = R.layout.view_follow)
public class FollowView extends BaseView {
    
    private FollowVM vm;

    public FollowView(Context context) {
        super(context);
        init(context, null);
    }

    public FollowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override public void bind(IViewModel model) {
        vm = (FollowVM) model;
    }
}