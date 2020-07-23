package com.ulfy.android.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 和视频相关的工具类
 */
public final class VideoUtils {

    /**
     * 获取视频第一帧缩略图
     */
    public static Bitmap videoToBitmap(File video) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(video.getAbsolutePath());
        Bitmap bitmap = media.getFrameAtTime();
        media.release();
        return bitmap;
    }

    /**
     * 获取视频第一帧缩略图并保存到临时文件中
     */
    public static File videoToBitmapToTempFile(File video) {
        try {
            Bitmap bitmap = videoToBitmap(video);
            if (bitmap != null) {
                File file = File.createTempFile("video", ".jpg", UtilsConfig.context.getCacheDir());
                BitmapUtils.bitmapToFile(bitmap, file, false);
                return file;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 当生成视频缩略图时的回调
     *      没生成一个缩略图都会回调该方法
     */
    public interface OnVideoToBitmapListener {
        /**
         * 当视频中的其中一帧缩略图生成后的回调
         * @param destFile  缩略图文件
         * @param count     总缩略图数量
         * @param index     当前缩略图位置（从0开始）
         */
        void onVideoToBitmap(File destFile, int count, int index);
    }

    /**
     * 获取视频期望数量的缩略图并保存到临时文件中，期望数量是因为有可能会出现部分关键帧获取失败的情况
     */
    public static List<File> videoToBitmapListToTempFile(File video, int expectCount, OnVideoToBitmapListener onVideoToBitmapListener) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(video.getAbsolutePath());
        String d = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = StringUtils.isEmpty(d) ? -1 : Long.valueOf(d);
        List<File> fileList = new ArrayList<>();
        try {
            for (int i = 0; i < expectCount; i++) {
                Bitmap bitmap = media.getFrameAtTime(duration / expectCount * i * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (bitmap != null) {
                    File file = File.createTempFile("video", ".jpg", UtilsConfig.context.getCacheDir());
                    BitmapUtils.bitmapToFile(bitmap, file, false);
                    if (onVideoToBitmapListener != null) {
                        onVideoToBitmapListener.onVideoToBitmap(file, expectCount, i);
                    }
                    fileList.add(file);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            media.release();
        }
        return fileList;
    }

    /**
     * 获取视频宽高，获取失败其宽高值为 -1
     */
    public static Size getVideoSize(File video) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(video.getAbsolutePath());
        String w = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String h = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        media.release();
        int width = StringUtils.isEmpty(w) ? -1 : Integer.valueOf(w);
        int height = StringUtils.isEmpty(h) ? -1 : Integer.valueOf(h);
        return new Size(width, height);
    }

    /**
     * 获取视频时长，单位毫秒
     */
    public static long getVideoDuration(File video) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(video.getAbsolutePath());
        String d = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        media.release();
        return StringUtils.isEmpty(d) ? -1 : Long.valueOf(d);
    }

    /**
     * 获取视频旋转角度
     */
    public static int getVideoRotation(File video) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(video.getAbsolutePath());
        String r = media.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        media.release();
        return StringUtils.isEmpty(r) ? -1 : Integer.valueOf(r);
    }

    public static final class Size {
        private final int width, height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
