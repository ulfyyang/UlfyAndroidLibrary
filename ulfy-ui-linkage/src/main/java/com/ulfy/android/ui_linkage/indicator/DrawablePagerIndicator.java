package com.ulfy.android.ui_linkage.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;

import net.lucode.hackware.magicindicator.FragmentContainerHelper;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.model.PositionData;

import java.util.List;

public class DrawablePagerIndicator extends View implements IPagerIndicator {
    private List<PositionData> positionDataList;
    private Drawable drawable;
    private int left, top, right, bottom;
    private Interpolator startInterpolator = new LinearInterpolator();
    private Interpolator endInterpolator = new LinearInterpolator();
    // 提供给外接定制的属性
    private int paddingLeft, paddingTop, paddingRight, paddingBottom;   // 指示器背景与现实内容间的内边距

    public DrawablePagerIndicator(Context context) {
        super(context);
    }

    @Override protected void onDraw(Canvas canvas) {
        if (drawable != null) {
            drawable.setBounds(left, top, right, bottom);
            drawable.draw(canvas);
        }
    }

    @Override public void onPositionDataProvide(List<PositionData> dataList) {
        this.positionDataList = dataList;
    }

    @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (positionDataList == null || positionDataList.isEmpty()) {
            return;
        }
        PositionData current = FragmentContainerHelper.getImitativePositionData(positionDataList, position);
        PositionData next = FragmentContainerHelper.getImitativePositionData(positionDataList, position + 1);
        float leftX, nextLeftX, rightX, nextRightX;
        leftX = current.mContentLeft - paddingLeft;
        nextLeftX = next.mContentLeft - paddingLeft;
        rightX = current.mContentRight + paddingRight;
        nextRightX = next.mContentRight + paddingRight;
        left = (int) (leftX + (nextLeftX - leftX) * startInterpolator.getInterpolation(positionOffset));
        right = (int) (rightX + (nextRightX - rightX) * endInterpolator.getInterpolation(positionOffset));
        top = current.mContentTop - paddingTop; bottom = current.mContentBottom + paddingBottom;
        invalidate();
    }

    // 这里就不频繁调用invalidate方法了，外界设置后手动调用一下
    public void setIndicatorDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
    public void setIndicatorResource(@DrawableRes int resource) {
        this.drawable = ResourcesCompat.getDrawable(getResources(), resource, null);
    }
    public void setPadding(int padding) {
        this.paddingLeft = this.paddingTop = this.paddingRight = this.paddingBottom = padding;
    }
    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = paddingLeft;
    }
    public void setPaddingTop(int paddingTop) {
        this.paddingTop = paddingTop;
    }
    public void setPaddingRight(int paddingRight) {
        this.paddingRight = paddingRight;
    }
    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    @Override public void onPageSelected(int position) { }
    @Override public void onPageScrollStateChanged(int state) { }
}
