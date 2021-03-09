package com.ulfy.android.system;

import android.app.Application;
import android.os.Environment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class SystemConfig {
    public static Application context;

    /**
     * 初始化系统模块
     */
    public static void init(Application context) {
        SystemConfig.context = context;
        NetStateListener.listenNetStateChanged(context);
        BackgroundRunningDetector.init(context);
    }

    public static final class Config {
        /*
        三方包记录
         */
        public static final String THIRD_APP_PACKAGE_NAME_QQ = "com.tencent.mobileqq";
        public static final String THIRD_APP_PACKAGE_NAME_WECHAT = "com.tencent.mm";
        public static final String THIRD_APP_PACKAGE_NAME_TAOBAO = "com.taobao.taobao";
        public static final String THIRD_APP_PACKAGE_NAME_JD = "com.jingdong.app.mall";
        public static final String THIRD_APP_PACKAGE_NAME_TM = "com.tmall.wireless";
        public static final String THIRD_APP_PACKAGE_NAME_BAIDUNETDISK = "com.baidu.netdisk";
        public static final String THIRD_APP_PACKAGE_NAME_XUNLEI = "com.xunlei.downloadprovider";
        public static final String THIRD_APP_PACKAGE_NAME_QCLOUD = "com.qq.qcloud";
        private static final Map<String, String> packageNameClazzMap = new HashMap<>();

        static  {
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_QQ, "com.tencent.mobileqq.activity.SplashActivity");
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_WECHAT, "com.tencent.mm.ui.LauncherUI");
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_TAOBAO, "com.taobao.tao.welcome.Welcome");
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_JD, "com.jingdong.app.mall.main.MainActivity");
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_TM, "com.tmall.wireless.splash.TMSplashActivity");
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_BAIDUNETDISK, "com.baidu.netdisk.ui.MainActivity");
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_XUNLEI, "com.xunlei.downloadprovider.frame.MainTabActivity");
            packageNameClazzMap.put(THIRD_APP_PACKAGE_NAME_QCLOUD, "com.qq.qcloud.global.ui.MainFrameActivity");
        }

        public static String getLaunchClazzByPackageName(String packageName) {
            return packageNameClazzMap.get(packageName);
        }

        public static long EXIT_TWICE_INTERVAL = 2000;          // 第一次和第二次的退出间隔时间基准
        public static boolean killForExit = false;              // 是否使用kill的方式退出app，用在两次返回退出
    }

    /**
     * 获得拍照图片缓存目录
     */
    static File getTakePhotoPictureCacheDir() {
        File dir = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            dir = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
        }else {
            dir = new File(Environment.getDataDirectory(), "camera");
        }
        if(!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 获取用于下载文件的目录
     */
    static File getDownloadFilePath() {
        File dir = new File(SystemConfig.context.getExternalCacheDir(), "system_download");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
