package com.sy.comment.application.cm;

import com.sy.comment.application.base.BaseCM;
import com.sy.comment.ui.cell.MomentCell;
import com.ulfy.android.mvvm.IView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MomentCM extends BaseCM {
    public List<PictureSquareCM> pictureCMList = new ArrayList<>();
    public boolean showPicture;     // 是否显示图片

    public MomentCM() {
        // 模拟添加图片的过程
        int number = new Random().nextInt(5);
        for (int i = 0; i < number; i++) {
            pictureCMList.add(new PictureSquareCM());
        }
        showPicture = new Random().nextBoolean();
    }

    @Override public Class<? extends IView> getViewClass() {
        return MomentCell.class;
    }
}