package com.sy.comment.ui.activity;


import android.app.Activity;
import android.os.Bundle;

import com.sy.comment.R;
import com.sy.comment.ui.base.BaseActivity;
import com.ulfy.android.system.AppUtils;
import com.ulfy.android.ui_injection.Layout;


@Layout(id = R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @Override public void onBackPressed() {
        AppUtils.exitTwice("再按一次退出" + AppUtils.getAppName());
    }

}
