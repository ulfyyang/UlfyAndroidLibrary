package com.ulfy.android.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 透明状态栏
 */
public final class StatusBarUtils {
    private static final int TAG_KEY_HAVE_SET_OFFSET = -123;

    /**
     * 状态栏显示和隐藏
     *     该方法会让状态栏彻底消失，不占用屏幕空间
     */
    public static void visiable(Activity activity, boolean visiable) {
        if (visiable) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    /*
        尽量在设置完布局文件后再进行相关设置
            以下步骤可以根据需要省略
            通常的设置顺序：设置全屏（搭配使用白天、夜间模式） --> 改变颜色（搭配使用白天、夜间模式）
     */

    /**
     * 透明状态栏（安卓4.4支持）
     *      4.4 -- 背景透明    6.0及以后 -- 背景全透明
     *      触发全屏
     */
    public static void translucent(Activity activity) {
        // 该方案支持安卓5.0全透明，但是由于安卓5.0无法修改通知栏图表字体颜色，为了更好的兼容性，将要求放宽到6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            //去除效果不佳的半透明状态栏(如果设置了的话)
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //这里的原理是直接设置了完全透明的状态栏，并且保留了内容延伸的效果
            //全屏显示设置新的状态栏:延伸内容到状态栏
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        // 安卓4.4以上
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置为白天模式：黑色图标（安卓6.0支持）
     *      仅替换通知栏图标，通知来保持原来颜色（默认黑色）
     *      不触发全屏，也不会清除全屏属性
     */
    public static void lightMode(Activity activity) {
        setMIUIStatusBarDarkIcon(activity, true);
        setMeizuStatusBarDarkIcon(activity, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            activity.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    /**
     * 设置为夜间模式：白色图标（安卓6.0支持）
     *      仅替换通知栏图标，通知来保持原来颜色（默认黑色）
     *      不触发全屏，也不会清除全屏属性
     */
    public static void darkMode(Activity activity) {
        setMIUIStatusBarDarkIcon(activity, false);
        setMeizuStatusBarDarkIcon(activity, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int systemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            activity.getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    /**
     * 修改通知栏颜色（安卓6.0支持）
     *      不触发全屏，并且会清除全屏属性
     */
    public static void changeColor(Activity activity, int color) {
        // 只有6.0及以上支持修改颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }



    ///////////////////////////////////////////////////////////////////////////
    // 工具方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 根据状态栏的高度自动位移目标View
     *      只会增加paddingTop，其它方向的保持不变
     */
    public static void offsetForStatusBar(Context context, View needOffsetView) {
        Object haveSetOffset = needOffsetView.getTag(TAG_KEY_HAVE_SET_OFFSET);
        if (haveSetOffset == null || !(Boolean) haveSetOffset) {
            needOffsetView.setPadding(needOffsetView.getPaddingLeft(), needOffsetView.getPaddingTop() + getStatusBarHeight(context),
                    needOffsetView.getPaddingRight(), needOffsetView.getPaddingBottom());
            needOffsetView.setTag(TAG_KEY_HAVE_SET_OFFSET, true);
        }
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    /**
     * 获取导航栏高度
     */
    public static int getNavigationBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }




    ///////////////////////////////////////////////////////////////////////////
    // 支持方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 修改 MIUI V6  以上状态栏颜色
     */
    private static void setMIUIStatusBarDarkIcon(Activity activity, boolean darkIcon) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkIcon ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) { }
    }

    /**
     * 修改魅族状态栏字体颜色 Flyme 4.0
     */
    private static void setMeizuStatusBarDarkIcon(Activity activity, boolean darkIcon) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (darkIcon) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
        } catch (Exception e) { }
    }
}