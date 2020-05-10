package com.sy.comment.application.cm;

import com.ulfy.android.mvvm.IView;
import com.ulfy.android.task.LoadDataUiTask;
import com.ulfy.android.utils.LogUtils;
import com.sy.comment.application.base.BaseCM;
import com.sy.comment.ui.cell.PrizeCell;

public class PrizeCM extends BaseCM {

    @Override public Class<? extends IView> getViewClass() {
        return PrizeCell.class;
    }
}