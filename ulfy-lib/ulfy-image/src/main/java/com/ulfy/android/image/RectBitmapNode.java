package com.ulfy.android.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 取一个位图的内切矩形
 */
public final class RectBitmapNode extends BitmapProcessNode {
    public static final int CORNER_TOP_LEFT = 1;            // 左上角圆角
    public static final int CORNER_TOP_RIGHT = 1 << 1;      // 右上角圆角
    public static final int CORNER_BOTTOM_LEFT = 1 << 2;    // 左下角圆角
    public static final int CORNER_BOTTOM_RIGHT = 1 << 3;   // 右下角圆角
    public static final int CORNER_ALL = CORNER_TOP_LEFT | CORNER_TOP_RIGHT | CORNER_BOTTOM_LEFT | CORNER_BOTTOM_RIGHT; // 全部圆角

    // 这两个值是相对距离，相当于比例关系。
    private int widthRatio;		     // 相对宽度
    private int heightRatio;		 // 相对高度
    private int radius;             // 正方形圆角大小
    private int radiusFlag;         // 记录哪个角是圆角

    public RectBitmapNode() {
        this(0, 0, 0, 0);
    }

    public RectBitmapNode(int widthRatio, int heightRatio) {
        this(widthRatio, heightRatio, 0, 0);
    }

    public RectBitmapNode(int radius) {
        this(0, 0, radius, CORNER_ALL);
    }

    public RectBitmapNode(int widthRatio, int heightRatio, int radius) {
        this(widthRatio, heightRatio, radius, CORNER_ALL);
    }

    public RectBitmapNode(int widthRatio, int heightRatio, int radius, int flag) {
        if (widthRatio < 0) {
            widthRatio = 0;
        }
        if (heightRatio < 0) {
            heightRatio = 0;
        }
        if (radius < 0) {
            radius = 0;
        }
        this.widthRatio = widthRatio;
        this.heightRatio = heightRatio;
        this.radius = radius;
        this.radiusFlag = flag;
    }

    @Override protected Bitmap onProcessBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int targetBitmapWidth = 0;
        int targetBitmapHeight = 0;

        // 按照指定的比例计算最终的宽和高
        if (widthRatio == 0 || heightRatio == 0) {
            targetBitmapWidth = bitmap.getWidth();
            targetBitmapHeight = bitmap.getHeight();
        } else {
            targetBitmapWidth = bitmap.getWidth();
            targetBitmapHeight = targetBitmapWidth * heightRatio / widthRatio;

            if(targetBitmapHeight > bitmap.getHeight()) {
                targetBitmapWidth = bitmap.getHeight() * targetBitmapWidth / targetBitmapHeight;
                targetBitmapHeight = bitmap.getHeight();
            }
        }

        Bitmap targetBitmap = Bitmap.createBitmap(targetBitmapWidth, targetBitmapHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);

        /*
        如果有圆角则处理圆角
            实现思路：思路就是先画一个圆角矩形把这个图片变成圆角，然后你想让那个角不是圆角，
            就把对应角位置那部分的原图画出来即可，画一个矩形就可以把原来的角显示出来
         */
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        if (radius > 0 && radiusFlag != 0) {
            // 先绘制圆角矩形
            RectF rectF = new RectF(new Rect(0, 0, targetBitmap.getWidth(), targetBitmap.getHeight()));
            canvas.drawRoundRect(rectF, radius, radius, paint);
            //哪个角不是圆角我再把你用矩形画出来
            int notRoundedCorners = radiusFlag ^ CORNER_ALL;
            if ((notRoundedCorners & CORNER_TOP_LEFT) != 0) {
                canvas.drawRect(0, 0, radius, radius, paint);
            }
            if ((notRoundedCorners & CORNER_TOP_RIGHT) != 0) {
                canvas.drawRect(rectF.right - radius, 0, rectF.right, radius, paint);
            }
            if ((notRoundedCorners & CORNER_BOTTOM_LEFT) != 0) {
                canvas.drawRect(0, rectF.bottom - radius, radius, rectF.bottom, paint);
            }
            if ((notRoundedCorners & CORNER_BOTTOM_RIGHT) != 0) {
                canvas.drawRect(rectF.right - radius, rectF.bottom - radius, rectF.right, rectF.bottom, paint);
            }
            // 绘制圆角需要讲后续的画笔模式设置为图片在图形中属性
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        }

        canvas.drawBitmap(bitmap, -(bitmap.getWidth() - targetBitmapWidth) / 2, -(bitmap.getHeight() - targetBitmapHeight) / 2, paint);

        // 返回生成的图形
        return targetBitmap;
    }
}
