package com.ulfy.android.image;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.ielse.imagewatcher.ImageWatcher;

import java.io.File;
import java.util.List;

/**
 * 图片工具类
 */
public final class ImageUtils {

    /*
        注意：如果在一个页面上加载大量的相同url图片，则同时刷新的时候回造成加载闪烁。这里猜测是Glide对同一个url的图片缓存是有大小限制的，当同时加载一个
             url时，会导致该图片的缓存不够用从而引发重新使用占位图在硬盘缓存加载从而引发闪烁问题
     */

    ///////////////////////////////////////////////////////////////////////////
    // url 版
    ///////////////////////////////////////////////////////////////////////////

    public static void loadImage(String url, ImageView imageView) {
        loadImage(url, -1, -1, imageView, null);
    }

    public static void loadImage(String url, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        loadImage(url, -1, -1, imageView, bitmapProcessNode);
    }

    public static void loadImage(String url, int placeholder, ImageView imageView) {
        loadImage(url, placeholder, -1, imageView, null);
    }

    public static void loadImage(String url, int placeholder, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        loadImage(url, placeholder, -1, imageView, bitmapProcessNode);
    }

    public static void loadImage(String url, int placeholder, int errorholder, ImageView imageView) {
        loadImage(url, placeholder, errorholder, imageView, null);
    }

    public static void loadImage(String url, int placeholder, int errorholder, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        GlideWrapper.getInstance().loadImage(url, placeholder, errorholder, imageView, bitmapProcessNode);
    }

    public static void preload(String url) {
        GlideWrapper.getInstance().preload(url);
    }

    public static File download(String url) throws Exception {
        return GlideWrapper.getInstance().download(url);
    }

    ///////////////////////////////////////////////////////////////////////////
    // file 版
    ///////////////////////////////////////////////////////////////////////////

    public static void loadImage(File file, ImageView imageView) {
        loadImage(file, -1, -1, imageView,null);
    }

    public static void loadImage(File file, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        loadImage(file, -1, -1, imageView, bitmapProcessNode);
    }

    public static void loadImage(File file, int placeholder, ImageView imageView) {
        loadImage(file, placeholder, -1, imageView, null);
    }

    public static void loadImage(File file, int placeholder, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        loadImage(file, placeholder, -1, imageView, bitmapProcessNode);
    }

    public static void loadImage(File file, int placeholder, int errorholder, ImageView imageView) {
        loadImage(file, placeholder, errorholder, imageView, null);
    }

    public static void loadImage(File file, int placeholder, int errorholder, ImageView imageView, BitmapProcessNode bitmapProcessNode) {
        GlideWrapper.getInstance().loadImage(file.getPath(), placeholder, errorholder, imageView, bitmapProcessNode);
    }

    public static void preload(File file) {
        GlideWrapper.getInstance().preload(file.getAbsolutePath());
    }

    /**
     * 获取缓存大小
     */
    public static long getCacheSize() {
        return GlideWrapper.getInstance().getCacheSize();
    }

    /**
     * 清除内存缓存
     *      只能在主线程执行
     */
    public static void clearMemoryCache() {
        GlideWrapper.getInstance().clearImageMemoryCache();
    }

    /**
     * 清除硬盘缓存
     *      只能在后台线程运行
     */
    public static void clearDiskCache() {
        GlideWrapper.getInstance().clearImageDiskCache();
    }


    ///////////////////////////////////////////////////////////////////////////
    // 预览多个图片
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 大图预览
     * @param context       当前上下文
     * @param imagePathList 预览的图片集合
     */
    public static ImageWatcher preview(Context context, List<String> imagePathList) {
        return ImageWatcherWrapper.getInstance().preview(context, imagePathList, 0);
    }

    /**
     * 大图预览
     * @param context       当前上下文
     * @param imagePathList 预览的图片集合
     * @param showPosition  默认显示的位置
     */
    public static ImageWatcher preview(Context context, List<String> imagePathList, int showPosition) {
        return ImageWatcherWrapper.getInstance().preview(context, imagePathList, showPosition);
    }

    /**
     * 大图预览
     * @param viewGroup     包含图片的ImageView容器，这些图片最好同级
     * @param imagePathList 预览的图片集合
     */
    public static ImageWatcher preview(ViewGroup viewGroup, List<String> imagePathList) {
        return ImageWatcherWrapper.getInstance().preview(viewGroup, imagePathList, 0);
    }

    /**
     * 大图预览
     * @param viewGroup     包含图片的ImageView容器，这些图片最好同级
     * @param imagePathList 预览的图片集合
     * @param showPosition  默认的显示位置
     */
    public static ImageWatcher preview(ViewGroup viewGroup, List<String> imagePathList, int showPosition) {
        return ImageWatcherWrapper.getInstance().preview(viewGroup, imagePathList, showPosition);
    }

    /**
     * 在使用该功能的Activity中onBackPressed方法中添加
     *
     *      if (!ImageUtils.handlePreviewBackPressed(this)) {
     *          super.onBackPressed();
     *      }
     */
    public static boolean handlePreviewBackPressed(Activity activity) {
        return ImageWatcherWrapper.getInstance().handlePreviewBackPressed(activity);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 辅助方法
    ///////////////////////////////////////////////////////////////////////////

    static final long GB_UNIT = 1024 * 1024 * 1024;
    static final long MB_UNIT = 1024 * 1024;
    static final long KB_UNIT = 1024;

    /**
     * 转换字节为人类可读单位
     */
    public static float convertFileSizeToHumanReadable(long size) {
        if (size > GB_UNIT) {                   // GB
            return size * 1.0f / GB_UNIT;
        }
        if (size > MB_UNIT) {                   // MB
            return size * 1.0f / MB_UNIT;
        }
        if (size > KB_UNIT) {                   // KB
            return size * KB_UNIT;
        }
        return size * 1.0f;                     // B
    }

    /**
     * 转换字节为人类可读单位
     *      并追加单位字符串
     */
    public static String convertFileSizeToHumanReadableString(long size) {
        if (size > GB_UNIT) {                           // GB
            return String.format("%.1fGB", size * 1.0f / GB_UNIT);
        }
        if (size > MB_UNIT) {                           // MB
            return String.format("%.1fMB", size * 1.0f / MB_UNIT);
        }
        if (size > KB_UNIT) {                           // KB
            return String.format("%.1fKB", size * 1.0f / KB_UNIT);
        }
        return String.format("%.1fB", size * 1.0f);     // B
    }
}
