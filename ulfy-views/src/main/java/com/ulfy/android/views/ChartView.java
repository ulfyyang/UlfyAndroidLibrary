package com.ulfy.android.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ChartView extends View {
    private GradientDrawable gradientDrawable;
    private RectF layerRect = new RectF();
    private List<Data> dataList;
    private List<Position> spotPositionList;

    public ChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int offset = dp2px(20);
        layerRect.left = offset;
        layerRect.top = offset;
        layerRect.right = getWidth() - offset;
        layerRect.bottom = getHeight() - offset;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dataList != null && dataList.size() > 0) {
            int layerId = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
            generateSpotPositionList();
            if (dataList.size() == 1) {
                drawStopLine(canvas);
                drawSpotText(canvas);
            } else {
                drawGradientLayer(canvas);
                clipGradientLayer(canvas);
                drawStopLine(canvas);
                drawSpotText(canvas);
            }
            canvas.restoreToCount(layerId);
        }
    }

    private void generateSpotPositionList() {
        spotPositionList = new ArrayList<>();
        float stepWidth = dataList.size() <= 1 ? 0 : (layerRect.right - layerRect.left) / (dataList.size() - 1);
        float maxDataValue = dataList.size() == 0 ? 0 : dataList.get(0).value;
        for (Data data : dataList) {
            maxDataValue = Math.max(maxDataValue, data.value);
        }
        // 只有一个点的时候最大高度升高一倍使得其居中
        if (dataList.size() == 1) {
            maxDataValue *= 2;
        }
        // 如果最大值为0，则设置为当前控件的高度
        if (maxDataValue == 0) {
            maxDataValue = getHeight();
        }
        for (int i = 0; i < dataList.size(); i++) {
            spotPositionList.add(new Position(
                    layerRect.left +  stepWidth * i,
                    layerRect.bottom - dataList.get(i).value * (layerRect.bottom - layerRect.top) / maxDataValue));
        }
    }

    /**
     * 绘制渐变层
     */
    private void drawGradientLayer(Canvas canvas) {
        if (gradientDrawable == null) {
            gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setColors(new int[] {Color.parseColor("#77367DFD"), Color.parseColor("#00000000")});
            gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        }
        gradientDrawable.setBounds((int)layerRect.left, (int)layerRect.top, (int)layerRect.right, (int)layerRect.bottom - dp2px(10));
        gradientDrawable.draw(canvas);
    }

    /**
     * 切割渐变层
     */
    private void clipGradientLayer(Canvas canvas) {
        // 生成画笔
        Paint clipPaint = new Paint();
        clipPaint.setColor(Color.BLACK);
        clipPaint.setStyle(Paint.Style.FILL);
        clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        // 生成路径
        Path clipPath = new Path();
        clipPath.moveTo(layerRect.left, layerRect.top);
        clipPath.lineTo(layerRect.left, layerRect.bottom);
        for (Position position : spotPositionList) {
            clipPath.lineTo(position.x, position.y);
        }
        clipPath.lineTo(layerRect.right, layerRect.bottom);
        clipPath.lineTo(layerRect.right, layerRect.top);
        clipPath.close();
        // 进行裁切
        canvas.drawPath(clipPath, clipPaint);
    }

    private void drawStopLine(Canvas canvas) {
        int lineWidth = dp2px(1);
        Paint linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.parseColor("#367DFD"));
        linePaint.setStrokeWidth(lineWidth);
        if (spotPositionList.size() > 1) {
            for (int i = 1; i < spotPositionList.size(); i++) {
                Position startPosition = spotPositionList.get(i - 1);
                Position endPosition = spotPositionList.get(i);
                canvas.drawLine(startPosition.x, startPosition.y, endPosition.x, endPosition.y, linePaint);
            }
        }
        int circleR = dp2px(2);
        Paint circlePaint = new Paint();
        linePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(lineWidth * 2 / 3);
        for (Position position : spotPositionList) {
            circlePaint.setStyle(Paint.Style.FILL);
            circlePaint.setColor(Color.WHITE);
            canvas.drawCircle(position.x, position.y, circleR, circlePaint);
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setColor(Color.parseColor("#367DFD"));
            canvas.drawCircle(position.x, position.y, circleR, circlePaint);
        }
    }

    private void drawSpotText(Canvas canvas) {
        int offsetY = dp2px(10);
        // 绘制key的笔
        Paint keyPaint = new Paint();
        keyPaint.setAntiAlias(true);
        keyPaint.setColor(Color.parseColor("#BBBBBB"));
        keyPaint.setTextSize(dp2px(10));
        // 绘制值的笔
        Paint valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(Color.BLACK);
        valuePaint.setTextSize(dp2px(10));
        // 测量字符串宽度的笔
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(dp2px(10));
        for (int i = 0; i < dataList.size(); i++) {
            canvas.drawText(dataList.get(i).key,
                    spotPositionList.get(i).x - textPaint.measureText(dataList.get(i).key) / 2,
                    getHeight() - offsetY, keyPaint);
            canvas.drawText("+" + dataList.get(i).value,
                    spotPositionList.get(i).x - textPaint.measureText("+" + dataList.get(i).value) / 2,
                    spotPositionList.get(i).y - offsetY, valuePaint);
        }
    }

    private static class Position {
        public float x, y;
        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Data {
        public String key;
        public int value;
        public Data(String key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    public static int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }
}
