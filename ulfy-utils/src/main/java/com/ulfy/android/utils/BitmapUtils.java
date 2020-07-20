package com.ulfy.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
     * 获取视频第一帧缩略图
     */
    public static Bitmap videoToBitmap(File video) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(video.getAbsolutePath());
        return media.getFrameAtTime();
    }

    /**
     * 获取视频第一帧缩略图并保存到临时文件中
     */
    public static File videoToBitmapToTempFile(File video) {
        try {
            Bitmap bitmap = videoToBitmap(video);
            File file = File.createTempFile("video", ".jpg", UtilsConfig.context.getCacheDir());
            bitmapToFile(bitmap, file, false);
            return file;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param bitmaps       生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapBitmapToGif(String outputPath, int delay, boolean alpha, Bitmap... bitmaps) {
        return bitmapBitmapToGif(outputPath, delay, alpha, 0, 0, Arrays.asList(bitmaps));
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param bitmaps       生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapFileToGif(String outputPath, int delay, boolean alpha, File... bitmaps) {
        return bitmapFileToGif(outputPath, delay, alpha, 0, 0, Arrays.asList(bitmaps));
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param expectWidth   期望的宽度
     * @param expectHeight  期望的高度
     * @param bitmaps       生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapBitmapToGif(String outputPath, int delay, boolean alpha, int expectWidth, int expectHeight, Bitmap... bitmaps) {
        return bitmapBitmapToGif(outputPath, delay, alpha, expectWidth, expectHeight, Arrays.asList(bitmaps));
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param expectWidth   期望的宽度
     * @param expectHeight  期望的高度
     * @param bitmaps       生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapFileToGif(String outputPath, int delay, boolean alpha, int expectWidth, int expectHeight, File... bitmaps) {
        return bitmapFileToGif(outputPath, delay, alpha, expectWidth, expectHeight, Arrays.asList(bitmaps));
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param bitmapList    生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapBitmapToGif(String outputPath, final int delay, boolean alpha, List<Bitmap> bitmapList) {
        return bitmapBitmapToGif(outputPath, delay, alpha, 0, 0, bitmapList);
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param bitmapList    生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapFileToGif(String outputPath, final int delay, boolean alpha, List<File> bitmapList) {
        return bitmapFileToGif(outputPath, delay, alpha, 0, 0, bitmapList);
    }

    /**
     * Bitmap转GIF
     *      所有的位图必须宽高必须一样大
     *      该方法为耗时方法，期望大小越小越快，加工的图片数量越少速度越快（建议的图片在30张以内）
     *      压缩为有透明度的图形PNG时需要消耗大量的时间，因此如果可以知道不需要支持透明度的话就不要支持
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param expectWidth   期望的宽度
     * @param expectHeight  期望的高度
     * @param bitmapList    生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapBitmapToGif(String outputPath, final int delay, final boolean alpha, final int expectWidth, final int expectHeight, List<Bitmap> bitmapList) {
        try {
            // 进行图片大小压缩
            ExecutorService executorService = Executors.newCachedThreadPool();
            List<Future<Bitmap>> compressBitmapFutureList = new ArrayList<>();
            for (final Bitmap bitmap : bitmapList) {
                Future<Bitmap> compressBitmapFuture = executorService.submit(new Callable<Bitmap>() {
                    @Override public Bitmap call() throws Exception {
                        return compressBitmapWithSize(bitmap, alpha, expectWidth, expectHeight);
                    }
                });
                compressBitmapFutureList.add(compressBitmapFuture);
            }
            // 生成GIF图片
            GifEncoder gifEncoder = new GifEncoder();
            Bitmap firstFrameBitmap = compressBitmapFutureList.get(0).get();
            gifEncoder.init(firstFrameBitmap.getWidth(), firstFrameBitmap.getHeight(), outputPath);     // 宽和高必须和原图一样
            for (Future<Bitmap> compressBitmapFuture : compressBitmapFutureList) {
                gifEncoder.encodeFrame(compressBitmapFuture.get(), delay);
            }
            gifEncoder.close();
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
     *      针对录制表情包：期望大小200x200以内、每秒5帧比较合适，在生成速度和显示质量上可以达到较好的平衡。视频200毫秒取一帧，GIF播放间隔设置为100毫秒一帧
     * @param outputPath    输出位置
     * @param delay         每帧的延迟
     * @param alpha         是否是有透明度的图像
     * @param expectWidth   期望的宽度
     * @param expectHeight  期望的高度
     * @param bitmapList    生成GIF的Bitmap
     * @return 生成的路径
     */
    public static String bitmapFileToGif(String outputPath, final int delay, final boolean alpha, final int expectWidth, final int expectHeight, List<File> bitmapList) {
        try {
            // 进行图片大小压缩
            ExecutorService executorService = Executors.newCachedThreadPool();
            List<Future<Bitmap>> compressBitmapFutureList = new ArrayList<>();
            for (final File bitmap : new CopyOnWriteArrayList<>(bitmapList)) {
                Future<Bitmap> compressBitmapFuture = executorService.submit(new Callable<Bitmap>() {
                    @Override public Bitmap call() throws Exception {
                        return compressBitmapWithSize(bitmap, alpha, expectWidth, expectHeight);
                    }
                });
                compressBitmapFutureList.add(compressBitmapFuture);
            }
            // 生成GIF图片
            GifEncoder gifEncoder = new GifEncoder();
            Bitmap firstFrameBitmap = compressBitmapFutureList.get(0).get();
            gifEncoder.init(firstFrameBitmap.getWidth(), firstFrameBitmap.getHeight(), outputPath);     // 宽和高必须和原图一样
            for (Future<Bitmap> compressBitmapFuture : compressBitmapFutureList) {
                gifEncoder.encodeFrame(compressBitmapFuture.get(), delay);
            }
            gifEncoder.close();
            return outputPath;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
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
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
