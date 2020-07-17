package com.ulfy.android.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class ShapeLayout extends FrameLayout {
    public static final int SHAPE_CIRCLE = 1;       // 圆形。和xml中属性文件对应
    public static final int SHAPE_RECT = 2;         // 矩形。和xml中属性文件对应
    private Drawable shapeBackground;               // 背景。和xml中属性文件对应
    private Drawable transparentBackground = new ColorDrawable(Color.TRANSPARENT);      // 透明背景图
    private Shape contentShape;                     // 内容形状
    private Shape backgroundShape;                  // 背景形状

    public ShapeLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ShapeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeLayout);

            // 获取背景参数
            shapeBackground = typedArray.getDrawable(R.styleable.ShapeLayout_shape_background);

            // 处理形状参数 如果没有设置形状的值，则不错任何处理
            if (typedArray.hasValue(R.styleable.ShapeLayout_layout_shape)) {

                // 获取形状类型
                int shape = typedArray.getInt(R.styleable.ShapeLayout_layout_shape, 0);
                // 设置了形状后需要修复背景
                fixBackgroundIfNeed();

                // 圆形
                if (shape == SHAPE_CIRCLE) {
                    contentShape = new CircleShape();
                    backgroundShape = new CircleShape();
                }

                // 矩形
                if (shape == SHAPE_RECT) {
                    RectShape contentShape = new RectShape();
                    RectShape backgroundShape = new RectShape();
                    // 设置顺序按照先总后分的方式设置，因此分别设置可覆盖部分总的设置
                    if (typedArray.hasValue(R.styleable.ShapeLayout_rect_radius)) {
                        float radius = typedArray.getDimension(R.styleable.ShapeLayout_rect_radius, 0);
                        contentShape.setRadius(radius);
                        backgroundShape.setRadius(radius);
                    }
                    if (typedArray.hasValue(R.styleable.ShapeLayout_rect_radius_left_top)) {
                        float radiusLeftTop = typedArray.getDimension(R.styleable.ShapeLayout_rect_radius_left_top, 0);
                        contentShape.setRadiusLeftTop(radiusLeftTop);
                        backgroundShape.setRadiusLeftTop(radiusLeftTop);
                    }
                    if (typedArray.hasValue(R.styleable.ShapeLayout_rect_radius_right_top)) {
                        float radiusRightTop = typedArray.getDimension(R.styleable.ShapeLayout_rect_radius_right_top, 0);
                        contentShape.setRadiusRightTop(radiusRightTop);
                        backgroundShape.setRadiusRightTop(radiusRightTop);
                    }
                    if (typedArray.hasValue(R.styleable.ShapeLayout_rect_radius_right_bottom)) {
                        float radiusRightBottom = typedArray.getDimension(R.styleable.ShapeLayout_rect_radius_right_bottom, 0);
                        contentShape.setRadiusRightBottom(radiusRightBottom);
                        backgroundShape.setRadiusRightBottom(radiusRightBottom);
                    }
                    if (typedArray.hasValue(R.styleable.ShapeLayout_rect_radius_left_bottom)) {
                        float radiusLeftBottom = typedArray.getDimension(R.styleable.ShapeLayout_rect_radius_left_bottom, 0);
                        contentShape.setRadiusLeftBottom(radiusLeftBottom);
                        backgroundShape.setRadiusLeftBottom(radiusLeftBottom);
                    }
                    this.contentShape = contentShape;
                    this.backgroundShape = backgroundShape;
                }
            }

            typedArray.recycle();
        }
    }

    /**
     * 对背景进行裁切
     */
    @Override protected void onDraw(Canvas canvas) {
        if (shapeBackground == null) {
            super.onDraw(canvas);
        } else {
            if (getWidth() != 0 && getHeight() != 0) {
                int layerId = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
                // 绘制背景图片
                shapeBackground.setBounds(0, 0, getWidth(), getHeight());
                shapeBackground.draw(canvas);
                // 背景裁切
                backgroundShape.draw(canvas, 0, 0, getWidth(), getHeight(), getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
                canvas.restoreToCount(layerId);
            }
        }
    }

    /**
     * 对内容进行裁切
     */
    @Override protected void dispatchDraw(Canvas canvas) {
        if (contentShape == null) {
            super.dispatchDraw(canvas);
        } else {
            if (canvas.getWidth() != 0 && canvas.getHeight() != 0) {
                int layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
                // 绘制子View内容
                super.dispatchDraw(canvas);
                // 内容裁切
                contentShape.draw(canvas, getPaddingLeft(), getPaddingTop(), canvas.getWidth() - getPaddingRight(), canvas.getHeight() - getPaddingBottom(), 0, 0, 0, 0);
                canvas.restoreToCount(layerId);
            }
        }
    }

    /**
     * 设置为圆形
     */
    public ShapeLayout setShapeCircle() {
        this.contentShape = new CircleShape();
        this.backgroundShape = new CircleShape();
        this.fixBackgroundIfNeed();
        this.invalidate();
        return this;
    }

    /**
     * 设置为矩形
     */
    public ShapeLayout setShapeRect(float radius) {
        return setShapeRect(radius, radius, radius, radius);
    }

    /**
     * 设置为矩形
     */
    public ShapeLayout setShapeRect(float radiusLeftTop, float radiusRightTop, float radiusRightBottom, float radiusLeftBottom) {
        this.contentShape = new RectShape();
        ((RectShape)this.contentShape).radiusLeftTop = radiusLeftTop;
        ((RectShape)this.contentShape).radiusRightTop = radiusRightTop;
        ((RectShape)this.contentShape).radiusRightBottom = radiusRightBottom;
        ((RectShape)this.contentShape).radiusLeftBottom = radiusLeftBottom;
        this.backgroundShape = new RectShape();
        ((RectShape)this.backgroundShape).radiusLeftTop = radiusLeftTop;
        ((RectShape)this.backgroundShape).radiusRightTop = radiusRightTop;
        ((RectShape)this.backgroundShape).radiusRightBottom = radiusRightBottom;
        ((RectShape)this.backgroundShape).radiusLeftBottom = radiusLeftBottom;
        this.fixBackgroundIfNeed();
        this.invalidate();
        return this;
    }

    /**
     * 设置背景颜色
     */
    public ShapeLayout setShapeBackgroundColor(int color) {
        return setShapeBackground(new ColorDrawable(color));
    }

    /**
     * 设置背景资源
     */
    public ShapeLayout setShapeBackgroundResource(int resid) {
        return setShapeBackground(resid != 0 ? getResources().getDrawable(resid) : null);
    }

    /**
     * 设置背景
     */
    public ShapeLayout setShapeBackground(Drawable shapeBackground) {
        this.shapeBackground = shapeBackground;
        this.fixBackgroundIfNeed();
        this.invalidate();
        return this;
    }

    /**
     * 当设置了形状背景后自身背景会设置为透明色，当没有设置时会清空自身背景
     *      只有设置了背景才会触发onDraw方法，才会绘制背景
     */
    private void fixBackgroundIfNeed() {
        if (shapeBackground != null) {
            if (getBackground() != transparentBackground) {
                setBackground(transparentBackground);
            }
        } else {
            setBackground(null);
        }
    }

    /**
     * 形状定义接口
     */
    private interface Shape {
        /**
         * 为了减少对象创建，这里直接使用基本数据类型
         *      1） 左上右下坐标表示的是其在原始Canvas中的坐标，即如果有内边距则这些坐标构成了内部的一个子区域
         *      2） 该区域表示实现类可处理的区域
         * @param canvas        需要绘制的画布
         * @param canvasLeft    待绘制的左边界
         * @param canvasTop     待绘制的上边界
         * @param canvasRight   待绘制的右边界
         * @param canvasBottom  待绘制的下边界
         * @param paddingLeft   控件的内左边距
         * @param paddingTop    控件的内上边距
         * @param paddingRight  控件的内右边距
         * @param paddingBottom 控件的内下边距
         */
        public void draw(Canvas canvas, int canvasLeft, int canvasTop, int canvasRight, int canvasBottom, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);
    }

    /**
     * 外部裁切形状
     *      该类定义了裁切内容边框的基本流程，子类可定制具体的形状
     *      由于绘制是经常触发的操作，因此不要使用临时对象
     *      子类负责提供具体的形状，因此子类需要控制形状对象的创建
     */
    private abstract static class OutsideClipShape implements Shape {
        private static Map<Shape, Bitmap> clipBitmapMap = new WeakHashMap<>();
        private Paint contentClipPaint;         // 用于裁切内容位图的画笔
        private Bitmap clipBitmap;              // 用于裁切目标位图的位图，目标位图被留下的部分为该图中空的部分
        private Canvas clipBitmapCanvas;        // 用于绘制裁切形状的画布
        private Paint clipDstPaint;             // 用于绘制裁切位图背景的画笔
        private Paint clipSrcPaint;             // 用于绘制裁切位图中空的画笔

        @Override public void draw(Canvas canvas, int canvasLeft, int canvasTop, int canvasRight, int canvasBottom, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
            /*
            排除掉内容位图和裁切位图相交的部分。因为图形一般为中间透明的中空形状，因此会留下形状区域的位图
             */
            if (contentClipPaint == null) {
                contentClipPaint = new Paint();
                contentClipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            }
            // 使用内容裁切位图裁切目标内容位图
            canvas.drawBitmap(getClipBitmap(this, canvasRight - canvasLeft, canvasBottom - canvasTop, paddingLeft, paddingTop, paddingRight, paddingBottom), canvasLeft, canvasTop, contentClipPaint);
        }

        private Bitmap getClipBitmap(Shape shape, int width, int height, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
            // 获取子类传递过来的裁切位图形状路径，这个需要先调用，通过子类提供了裁切路径以后shape才会具有完整的尺寸
            Path clipPath = provideClipPath(0, 0, width, height, paddingLeft, paddingTop, paddingRight, paddingBottom);
            Bitmap bitmap = clipBitmapMap.get(shape);
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                // 创建用于裁切的画布
                Canvas clipCanvas = new Canvas(bitmap);
                // 填充裁切位图背景的画笔，背景设为黑色
                Paint clipDstPaint = new Paint();
                clipDstPaint.setColor(Color.BLACK);
                clipDstPaint.setStyle(Paint.Style.FILL);
                // 裁切位图中空抠图的画笔
                Paint clipSrcPaint = new Paint();
                clipSrcPaint.setColor(Color.WHITE);
                clipSrcPaint.setAntiAlias(true);
                clipSrcPaint.setStyle(Paint.Style.FILL);
                clipSrcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                // 绘制背景进行裁切生成中空形状裁切位图
                clipCanvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), clipDstPaint);
                clipCanvas.drawPath(clipPath, clipSrcPaint);
                clipBitmapMap.put(shape, bitmap);
            }
            return bitmap;
        }

        /**
         * 因为这个路径是提供给裁切位图使用的，因此相应的边界以裁切位图的边界为准
         * @param canvasLeft        裁切位图的左边界
         * @param canvasTop         裁切位图的上边界
         * @param canvasRight       裁切位图的右边界
         * @param canvasBottom      裁切位图的下边界
         */
        protected abstract Path provideClipPath(int canvasLeft, int canvasTop, int canvasRight, int canvasBottom, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);
    }

    /**
     * 圆形形状
     */
    private static class CircleShape extends OutsideClipShape {
        private Path clipPath = new Path();     // 用于绘制裁切形状的路径
        private int x, y, r;

        @Override protected Path provideClipPath(int canvasLeft, int canvasTop, int canvasRight, int canvasBottom, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
            clipPath.reset();
            // 计算圆心和最小半径
            x = canvasRight / 2;
            y = canvasBottom / 2;
            r = x < y ? x : y;
            // 添加到Path中
            clipPath.addCircle(x, y, r, Path.Direction.CW);
            return clipPath;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            CircleShape circleShape = (CircleShape) obj;
            return circleShape.x == x && circleShape.y == y && circleShape.r == r;
        }

        @Override public int hashCode() {
            return Objects.hash(x, y, r);
        }
    }

    /**
     * 矩形形状
     */
    private static class RectShape extends OutsideClipShape {
        private float radiusLeftTop;            // 左上角圆角
        private float radiusRightTop;           // 右上角圆角
        private float radiusRightBottom;        // 右下角圆角
        private float radiusLeftBottom;         // 左下角圆角
        private Path clipPath = new Path();     // 用于绘制裁切形状的路径
        private RectF rectF = new RectF();      // 创建绘制路径的中间量
        private float[] radius = new float[8];  // 创建绘制路径的中间量

        @Override protected Path provideClipPath(int canvasLeft, int canvasTop, int canvasRight, int canvasBottom, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
            // 重置路径
            clipPath.reset();
            // 设置四个方向上圆角的x、y值
            radius[0] = radiusLeftTop + paddingLeft; radius[1] = radiusLeftTop + paddingTop;
            radius[2] = radiusRightTop + paddingRight; radius[3] = radiusRightTop + paddingTop;
            radius[4] = radiusRightBottom + paddingRight; radius[5] = radiusRightBottom + paddingBottom;
            radius[6] = radiusLeftBottom + paddingLeft; radius[7] = radiusLeftBottom + paddingBottom;
            // 设置矩形区域
            rectF.left = canvasLeft;
            rectF.top = canvasTop;
            rectF.right = canvasRight;
            rectF.bottom = canvasBottom;
            // 添加到路径中
            clipPath.addRoundRect(rectF, radius, Path.Direction.CW);
            return clipPath;
        }

        @Override public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RectShape rectShape = (RectShape) obj;
            return rectShape.rectF.equals(rectF) && rectShape.radius[0] == radius[0] && rectShape.radius[1] == radius[1] && rectShape.radius[2] == radius[2]
                    && rectShape.radius[3] == radius[3] && rectShape.radius[4] == radius[4] && rectShape.radius[5] == radius[5] && rectShape.radius[6] == radius[6]
                    && rectShape.radius[7] == radius[7];
        }

        @Override public int hashCode() {
            return Objects.hashCode(rectF) + Arrays.hashCode(radius);
        }

        private RectShape setRadius(float radius) {
            setRadiusLeftTop(radius);
            setRadiusRightTop(radius);
            setRadiusRightBottom(radius);
            setRadiusLeftBottom(radius);
            return this;
        }

        private RectShape setRadiusLeftTop(float radiusLeftTop) {
            this.radiusLeftTop = radiusLeftTop;
            return this;
        }

        private RectShape setRadiusRightTop(float radiusRightTop) {
            this.radiusRightTop = radiusRightTop;
            return this;
        }

        private RectShape setRadiusRightBottom(float radiusRightBottom) {
            this.radiusRightBottom = radiusRightBottom;
            return this;
        }

        private RectShape setRadiusLeftBottom(float radiusLeftBottom) {
            this.radiusLeftBottom = radiusLeftBottom;
            return this;
        }
    }
}