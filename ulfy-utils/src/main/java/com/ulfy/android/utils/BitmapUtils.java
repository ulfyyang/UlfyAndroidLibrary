package com.ulfy.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.TextureView;
import android.view.View;

import com.waynejo.androidndkgif.GifEncoder;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 和图像处理相关的工具类
 */
public final class BitmapUtils {

    /**
     * 获取View的即时图像
     * @param view      支持常规的View和TextureView，不支持SurfaceView
     */
    public static Bitmap viewToBitmap(View view) {
        if (view instanceof TextureView) {
            return ((TextureView)view).getBitmap();
        } else {
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return bitmap;
        }
    }

    /**
     * 生成GIF进度回调
     */
    public interface BitmapToGifProcessListener {
        /**
         * 当处理每张图片时回调该方法，在处理之前回调
         * @param bitmap    正在处理的图片，可能是压缩过的
         * @param current   当前处理的第几张（从1开始）
         * @param total     总共有几张
         */
        void onProcess(Bitmap bitmap, int current, int total);
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath        输出位置
     * @param delay             每帧的延迟（建议100）
     * @param alpha             是否是有透明度的图像（这个根据业务需要，如果不确定就设置为true。透明图像会占用更多的时间）
     * @param expectWidth       期望的宽度（0表示保持原大小，建议400）
     * @param expectHeight      期望的高度（0表示保持原大小，建议400）
     * @param bitmapList        生成GIF的Bitmap
     * @param processListener   生成GIF进度回调
     * @return 生成的路径
     */
    public static String bitmapBitmapToGif(String outputPath, final int delay, final boolean alpha, final int expectWidth, final int expectHeight, List<Bitmap> bitmapList, BitmapToGifProcessListener processListener) {
        try {
            GifEncoder encoder = new GifEncoder();
            for (int i = 0; i < bitmapList.size(); i++) {
                Bitmap bitmap = compressBitmapWithSize(bitmapList.get(i), alpha, expectWidth, expectHeight);
                if (i == 0) {
                    encoder.init(bitmap.getWidth(), bitmap.getHeight(), outputPath);     // 宽和高必须和原图一样
                }
                encoder.encodeFrame(bitmap, delay);
                if (i == bitmapList.size() - 1) {
                    encoder.close();
                }
                if (processListener != null) {
                    processListener.onProcess(bitmap, i + 1, bitmapList.size());
                }
            }
            return outputPath;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小400x400以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath        输出位置
     * @param delay             每帧的延迟（建议100）
     * @param alpha             是否是有透明度的图像（这个根据业务需要，如果不确定就设置为true。透明图像会占用更多的时间）
     * @param expectWidth       期望的宽度（0表示保持原大小，建议400）
     * @param expectHeight      期望的高度（0表示保持原大小，建议400）
     * @param bitmapFileList    生成GIF的Bitmap
     * @param processListener   生成GIF进度回调
     * @return 生成的路径
     */
    public static String bitmapFileToGif(String outputPath, final int delay, final boolean alpha, final int expectWidth, final int expectHeight, List<File> bitmapFileList, BitmapToGifProcessListener processListener) {
        try {
            GifEncoder encoder = new GifEncoder();
            for (int i = 0; i < bitmapFileList.size(); i++) {
                Bitmap bitmap = compressBitmapWithSize(bitmapFileList.get(i), alpha, expectWidth, expectHeight);
                if (i == 0) {
                    encoder.init(bitmap.getWidth(), bitmap.getHeight(), outputPath);     // 宽和高必须和原图一样
                }
                encoder.encodeFrame(bitmap, delay);
                if (i == bitmapFileList.size() - 1) {
                    encoder.close();
                }
                if (processListener != null) {
                    processListener.onProcess(bitmap, i + 1, bitmapFileList.size());
                }
            }
            return outputPath;
        } catch (Exception e) {
            e.printStackTrace(); return "";
        }
    }

    /**
     * 保存图片到本地文件
     * @param bitmap        保存的图片
     * @param file          保存的文件
     * @param alpha         是否是有透明度的图像
     */
    public static File bitmapToFile(Bitmap bitmap, File file, boolean alpha) {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(alpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, bufferedOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedOutputStream != null) {
                try { bufferedOutputStream.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
        return file;
    }

    /**
     * 通过大小的方式进行压缩
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     * @param bitmap        被压缩的位图
     * @param alpha         是否是有透明度的图像
     * @param expectWidth   期望的宽
     * @param expectHeight  期望的高
     */
    public static Bitmap compressBitmapWithSize(Bitmap bitmap, boolean alpha, int expectWidth, int expectHeight) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inSampleSize = calculateInSampleSize(bitmap.getWidth(), bitmap.getHeight(), expectWidth, expectHeight);
        if (scaleOptions.inSampleSize == 1) {
            return bitmap;
        } else {
            return BitmapFactory.decodeStream(bitmapToInputStream(bitmap, alpha), null, scaleOptions);
        }
    }

    /**
     * 通过大小的方式进行压缩
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     * @param file          被压缩的位图
     * @param alpha         是否是有透明度的图像
     * @param expectWidth   期望的宽
     * @param expectHeight  期望的高
     */
    public static Bitmap compressBitmapWithSize(File file, boolean alpha, int expectWidth, int expectHeight) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), scaleOptions);
        int originalWidth = scaleOptions.outWidth;
        int originalHeight = scaleOptions.outHeight;
        scaleOptions.inSampleSize = calculateInSampleSize(originalWidth, originalHeight, expectWidth, expectHeight);
        scaleOptions.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), scaleOptions);
    }

    /**
     * 将位图转化为输入流
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     * @param alpha         是否是有透明度的图像
     */
    public static InputStream bitmapToInputStream(Bitmap bitmap, boolean alpha) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(alpha ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    /**
     * 计算缩放的倍数
     */
    private static int calculateInSampleSize(int originalWidth, int originalHeight, int expectWidth, int expectHeight) {
        int inSampleSize = 1;
        // 有任何一个宽高无效则直接返回默认缩放倍数
        if (originalWidth == 0 || originalHeight == 0 || expectWidth == 0 || expectHeight == 0) {
            return inSampleSize;
        }
        // 如果缩放后的大小大于实际的大小则提升缩放的倍数直到满足要求
        if (originalWidth > expectWidth || originalHeight > expectHeight) {
            while ((originalHeight / inSampleSize) > expectHeight && (originalWidth / inSampleSize) > expectWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}
