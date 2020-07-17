package com.ulfy.android.task_transponder;

import com.ulfy.android.task.Transponder;

public class NoNetConnectionTransponder extends Transponder {

    @Override protected void onNoNetConnection(Object data) {
        DialogRepository.getInstance().getNotNetConnectionDialog().show();
        onNetError(data);
    }

}
