package com.ulfy.android.image;

import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * 高斯模糊算法模糊一个图片
 * 该处理比较耗时，尽量不要使用该类，应为该类处理所需的事件比较长
 */
public final class BlurBitmapNode extends BitmapProcessNode {
    private int radius;                     // 模糊半径：0-25，越大越模糊。默认值为12

    public BlurBitmapNode() {
        this(12);
    }

    public BlurBitmapNode(int radius) {
        if (radius < 0) {
            radius = 0;
        }
        if (radius > 25) {
            radius = 25;
        }
        this.radius = radius;
    }

    @Override protected Bitmap onProcessBitmap(Bitmap bitmap) {
        ImageConfig.throwExceptionIfConfigNotConfigured();

        if (bitmap == null) {
            return null;
        }
        if (radius == 0) {
            return bitmap;
        }

        // 谷歌官方的方法
        RenderScript renderScript = RenderScript.create(ImageConfig.context);
        final Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
        final Allocation output = Allocation.createTyped(renderScript, input.getType());
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        scriptIntrinsicBlur.setInput(input);
        scriptIntrinsicBlur.setRadius(radius);
        scriptIntrinsicBlur.forEach(output);
        output.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;
    }
}
