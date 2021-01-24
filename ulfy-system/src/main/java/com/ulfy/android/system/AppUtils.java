package com.ulfy.android.system;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.ulfy.android.dialog.DialogUtils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * App工具类
 */
public final class AppUtils {
    private static final String SHARE_PREFERENCES_FILE = "share_preferences_file";
    private static final String SHARE_PREFERENCES_DEVICE_ID = "share_preferences_device_id";
    private static long mExitTime = 0;          // 记录上一次按下的时间，用于连续两次退出
    private static String deviceId;             // 记录设备的唯一id，通过静态量可以防止重复从底层获取

    ///////////////////////////////////////////////////////////////////////////
    // 基本信息获取
    //      通常信息统计：手机厂商、手机型号、手机当前系统语言、系统版本号、当前网络IP、App版本号
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取手机厂商
     *      例如：Huawei
     * @return  手机厂商
     */
    public static String getDeviceBrand() {
        return Build.BRAND;
    }

    /**
     * 获取手机型号
     *      例如：HUAWEI M!&-TL00
     * @return  手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取当前手机系统语言
     *      例如：zh
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getCurrentSystemLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取当前系统上的语言列表(Locale列表)
     * @return  语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *      例如：6.0
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取设备id，不需要申请特殊权限
     *      优先采用安卓id，如果获取失败则生成一个UUID
     *      安卓id示例：e670996975c4de5c
     *      UUID示例：b9f13d76-b67f-36c0-bff4-069e5d7eae8d
     */
    public static String getDevideId() {
        // 如果内存中存在则直接返回
        if (deviceId != null && deviceId.length() > 0) {
            return deviceId;
        }

        SharedPreferences sharedPreferences = SystemConfig.context.getSharedPreferences(SHARE_PREFERENCES_FILE, Context.MODE_PRIVATE);
        deviceId = sharedPreferences.getString(SHARE_PREFERENCES_DEVICE_ID, null);

        // 如果能从硬盘恢复则直接返回
        if (deviceId != null && deviceId.length() > 0) {
            return deviceId;
        }

        // 生成设备id并保存
        deviceId = getAndroidId();
        if ("9774d56d682e549c".equals(deviceId)) {      // 排除部分国产机型返回固定值
            deviceId = UUID.randomUUID().toString();
        } else {
            deviceId = UUID.nameUUIDFromBytes(deviceId.getBytes()).toString();
        }
        sharedPreferences.edit().putString(SHARE_PREFERENCES_DEVICE_ID, deviceId).commit();

        // 返回生成好的设备id
        return deviceId;
    }

    /**
     * 获得imei设备码，可以作为唯一标识
     *      该方法需要在调用之前通过运行时动态获取读取手机状体权限 Manifest.permission.READ_PHONE_STATE
     *      在已经授权的情况下：安卓9返回""， 安卓10会抛出异常。在安卓8以后的版本该方法已经无法使用了
     * @return 如果能成功取到则返回15位的数字字符串
     */
    @SuppressLint("MissingPermission")
    public static String getImei() {
        String imei = "";
        try {
            TelephonyManager manager = (TelephonyManager) SystemConfig.context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(SystemConfig.context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                imei = manager.getDeviceId();
            }
            if (imei == null) {
                imei = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    /**
     * 获取安卓id，在有些国产机上会返回固定的9774d56d682e549c
     */
    public static String getAndroidId() {
        return Settings.Secure.getString(SystemConfig.context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取手机无线网卡的mac地址
     *      对于有些手机如小米在打开和关闭网络的情况下获取到的mac地址不同
     *      有网返回真实地址，无网返回时固定的02:00:00:00:00:00
     *      如果用户关闭了wifi而是使用数据网络则会获取固定的02:00:00:00:00:00
     */
    public static String getMacAddress() {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacFromFile();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }


    /**
     * 获取当前网络的IP地址
     *      该方法获取的ip并不靠谱，如果设备连接到路由器的局域网中则返回的会是路由器分配的ip；对于流量来说可以获得真实ip
     *      例如：192.168.2.1（路由器内）10.97.94.3（移动流量）
     * @return 当前网络的IP地址
     */
    public static String getIpAddress() {
        NetworkInfo networkInfo = ((ConnectivityManager) SystemConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {             // 3/4g网络
                try {
                    for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();) {
                        NetworkInterface networkInterface = enumeration.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {        //  wifi网络
                WifiManager wifiManager = (WifiManager) SystemConfig.context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ip = wifiInfo.getIpAddress();
                return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
            }  else if (networkInfo.getType() == ConnectivityManager.TYPE_ETHERNET){    // 有限网络
                try {
                    for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements();) {
                        NetworkInterface networkInterface = enumeration.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException ex) { }
                return "0.0.0.0";
            }
        }
        return null;
    }

    /**
     * 是否为模拟器
     *      常规方法：通过检测常见模拟器的特征以及是否可以打开拨号盘判定
     *      通常情况下使用该方法即可
     */
    public static boolean isEmulator() {
        Intent intent = new Intent();
        intent.setData(Uri.parse("tel:" + "123456"));
        intent.setAction(Intent.ACTION_DIAL);
        // 是否可以处理跳转到拨号的 Intent
        boolean canResolveIntent = intent.resolveActivity(SystemConfig.context.getPackageManager()) != null;
        return Build.FINGERPRINT.startsWith("generic") || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys") || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator") || Build.SERIAL.equalsIgnoreCase("unknown")
                || Build.SERIAL.equalsIgnoreCase("android") || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion") || "google_sdk".equals(Build.PRODUCT)
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || ((TelephonyManager) SystemConfig.context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName().toLowerCase().equals("android")
                || !canResolveIntent;
    }

    /**
     * 是否为模拟器
     *      EasyProtector方法：摘抄自EasyProtector项目，并降低了判断点
     */
    public static boolean isEmulatorFromEasyProtector() {
        int suspectCount = 0;
        String baseBandVersion = getProperty("gsm.version.baseband");
        if (null == baseBandVersion || baseBandVersion.contains("1.0.0.0"))
            ++suspectCount;
        String buildFlavor = getProperty("ro.build.flavor");
        if (null == buildFlavor || buildFlavor.contains("vbox") || buildFlavor.contains("sdk_gphone"))
            ++suspectCount;
        String productBoard = getProperty("ro.product.board");
        if (null == productBoard || productBoard.contains("android") | productBoard.contains("goldfish"))
            ++suspectCount;
        String boardPlatform = getProperty("ro.board.platform");
        if (null == boardPlatform || boardPlatform.contains("android"))
            ++suspectCount;
        String hardWare = getProperty("ro.hardware");
        if (null == hardWare) ++suspectCount;
        else if (hardWare.toLowerCase().contains("ttvm")) suspectCount += 10;
        else if (hardWare.toLowerCase().contains("nox")) suspectCount += 10;
        boolean isSupportCameraFlash = SystemConfig.context.getPackageManager().hasSystemFeature("android.hardware.camera.flash");
        if (!isSupportCameraFlash) ++suspectCount;
        SensorManager sm = (SensorManager) SystemConfig.context.getSystemService(Context.SENSOR_SERVICE);
        int sensorSize = sm.getSensorList(Sensor.TYPE_ALL).size();
        if (sensorSize < 7) ++suspectCount;
        String userApps = CommandUtil.exec("pm list package -3");
        int userAppSize = getUserAppNum(userApps);
        if (userAppSize < 5) ++suspectCount;
        String filter = CommandUtil.exec("cat /proc/self/cgroup");
        if (null == filter) ++suspectCount;
        return suspectCount > 2;
    }

    /**
     * 是否在后台运行
     *      当所有页面都不可见时认为是后台运行
     */
    public static boolean isBackgroundRunning() {
        return BackgroundRunningDetector.isBackgroundRunning();
    }

    /**
     * 网络是否连接了
     */
    public static boolean isInternetConnected() {
        return NetStateListener.connected;
    }

    /**
     * 获取app的版本名versionName
     *      例如：1.0
     */
    public static String getVersionName() {
        try {
            PackageInfo packageInfo = SystemConfig.context.getPackageManager().getPackageInfo(SystemConfig.context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 获取app的版本数versionCode
     *      例如：38
     */
    public static int getVersionCode() {
        try {
            PackageInfo packageInfo = SystemConfig.context.getPackageManager().getPackageInfo(SystemConfig.context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 获取app的名称
     *      如：腾讯视频
     */
    public static String getAppName() {
        try {
            PackageManager packageManager = SystemConfig.context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(SystemConfig.context.getPackageName(), 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            throw new AssertionError(e);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 获取App包名
     */
    public static String getPackageName() {
        return SystemConfig.context.getPackageName();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 系统相关交互
    ///////////////////////////////////////////////////////////////////////////

    /**
     * HttpUrlConnection 对于未认证的 https 证书会无法连接网络
     *      通过该方法可以全局忽略 https 证书认证
     */
    public static void enableUnValidHttpsCertificate() {
        X509TrustManager xtm = new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String authType) { }
            @Override public void checkServerTrusted(X509Certificate[] chain, String authType) { }
            @Override public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLSocketFactory sslSocketFactory = null;

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        if (sslSocketFactory != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    /**
     * 请求权限的回调
     */
    public static class OnRequestPermissionListener {
        public void onSuccess() { }
        public void onFail() { }
        public void onFinish() { }
    }

    /**
     * 请求权限
     *      上下文必须使用Activity上下文
     */
    public static void requestPermission(final OnRequestPermissionListener onRequestPermissionListener, String... permissions) {
        if (onRequestPermissionListener != null) {
            AndPermission.with(SystemConfig.context).requestCode(new Random().nextInt()).permission(permissions)
                    .rationale(new RationaleListener() {
                        public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                            AndPermission.rationaleDialog(ActivityUtils.getTopActivity(), rationale).show();
                        }
                    }).callback(new PermissionListener() {
                public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                    onRequestPermissionListener.onSuccess();
                    onRequestPermissionListener.onFinish();
                }
                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                    onRequestPermissionListener.onFail();
                    onRequestPermissionListener.onFinish();
                }
            }).start();
        }
    }

    /**
     * 跳转第三方应用市场详情页
     */
    public static void goToAppMarketDetails() {
        try {
            Activity topActivity = ActivityUtils.getTopActivity();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + topActivity.getPackageName()));
            topActivity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(SystemConfig.context, "未安装应用市场", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * App升级：下载apk并安装
     *      如果url以apk结尾则直接下载安装（下载过程中会弹出一个全局的进度弹窗）
     *      如果url是个常规网址则跳转到系统浏览器
     */
    public static void upgrade(String url) {
        if (url == null || url.length() == 0) {
            return;
        } else {
            url = complementUrl(url);
            if (url.endsWith(".apk")) {
                downloadApkThenInstall(url, true);
            } else {
                launchSystemBrowser(url);
            }
        }
    }

    /**
     * 调用系统浏览器打开网址
     *      如果url以apk结尾直接下载安装（下载开始时会提示开始下载，但是不会弹出进度窗口）
     */
    public static void launchSystemBrowser(String url) {
        if (url == null || url.length() == 0) {
            return;
        } else {
            url = complementUrl(url);
            if (url.endsWith(".apk")) {
                downloadApkThenInstall(url, false);
            } else {
                Activity topActivity = ActivityUtils.getTopActivity();
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(url));
                topActivity.startActivity(intent);
            }
        }
    }

    /**
     * 调用系统安装页面安装本地apk文件
     */
    public static void installApk(File file) {
        if (file != null && file.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                installApk8(file);
            } else {
                installApk7(file);
            }
        } else {
            Toast.makeText(SystemConfig.context, "安装失败，文件不存在", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 是否安装了三方app
     * @param packageName app包名，配置在SystemUtils.Config中
     */
    public static boolean isThirdAppInstall(String packageName) {
        List<PackageInfo> packageInfoList = SystemConfig.context.getPackageManager().getInstalledPackages(0);
        if (packageInfoList != null && packageInfoList.size() > 0) {
            for (PackageInfo packageInfo : packageInfoList) {
                if (packageInfo.packageName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 启动三方app
     * @param packageName app包名，配置在SystemUtils.Config中
     */
    public static void launchThirdApp(String packageName) {
        if (isThirdAppInstall(packageName)) {
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(packageName, SystemConfig.Config.getLaunchClazzByPackageName(packageName));
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(componentName);
            ActivityUtils.getTopActivity().startActivity(intent);
        } else {
            switch (packageName) {
                case SystemConfig.Config.THIRD_APP_PACKAGE_NAME_QQ:
                    Toast.makeText(SystemConfig.context, "请安装QQ", Toast.LENGTH_LONG).show();
                    break;
                case SystemConfig.Config.THIRD_APP_PACKAGE_NAME_WECHAT:
                    Toast.makeText(SystemConfig.context, "请安装微信", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    /**
     * 将cookie同步到WebView
     * @return true 同步cookie成功，false同步cookie失败
     */
    public static boolean syncWebViewCookie(String url, String cookie) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, cookie);
        String newCookie = cookieManager.getCookie(url);
        return !TextUtils.isEmpty(newCookie);
    }

    /**
     * 使设备震动
     */
    public static void viber() {
        Vibrator vibrator = (Vibrator) SystemConfig.context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }

    /**
     * 重新启动应用，该方式不会杀死进程，缓存的数据还可以使用
     */
    public static void restartApp() {
        Intent intent = SystemConfig.context.getPackageManager().getLaunchIntentForPackage(SystemConfig.context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityUtils.getTopActivity().startActivity(intent);
    }

    /**
     * 通知系统扫描目标媒体文件
     *      1) 当执行完毕后会将目标文件的信息增加到相册、视频等系统记录中
     *      2) 该操作是一个比较耗时的操作，建议尽量不要在UI线程中执行
     *      3) 该操作的目标文件必须要在外部存储上，否则该方法会失效
     *      4) 如果是自己生成的媒体文件建议使用Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)获取对应的媒体文件目录并放入其中
     */
    public static void notifySystemScanMediaFile(File file) {
        SystemConfig.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
    }

    /**
     * 保存图片到本地
     *      file尽量是不会被系统扫描的地方。否则会在相册中生成两张文件
     *      如果是调用系统相机拍照产生的图片，建议直接发布广播通知
     */
    public static void insertPictureToSystem(final File file, final String title, final String description) {
        AppUtils.requestPermission(new OnRequestPermissionListener() {
            @Override public void onSuccess() {
                try {
                    MediaStore.Images.Media.insertImage(SystemConfig.context.getContentResolver(), file.getAbsolutePath(), title, description);
                    SystemConfig.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file.getAbsolutePath())));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(SystemConfig.context, "文件保存失败", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFail() {
                Toast.makeText(SystemConfig.context, "未授予访问系统存储空间权限", Toast.LENGTH_LONG).show();
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 保存图片到本地
     *      由于直接采用了Bitmap对象，因此不会留下痕迹
     */
    public static void insertPictureToSystem(final Bitmap bitmap, final String title, final String description) {
        AppUtils.requestPermission(new OnRequestPermissionListener() {
            @Override public void onSuccess() {
                String url = MediaStore.Images.Media.insertImage(SystemConfig.context.getContentResolver(), bitmap, title, description);
                SystemConfig.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(url)));
            }
            @Override public void onFail() {
                Toast.makeText(SystemConfig.context, "未授予访问系统存储空间权限", Toast.LENGTH_LONG).show();
            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 输入法相关
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 键盘变化的回调
     */
    public interface OnSoftKeyBoardChangeListener {
        void keyBoardShow(int height);
        void keyBoardHide(int height);
    }

    /**
     * 监听软键盘显式隐藏和高度
     *      该方法是根据界面根视图的高度变化来监听的，并不是安卓原生的监听
     *      因此若是由其它原因导致的根视图变化也有可能引发
     */
    public static void setOnSoftKeyBoardChangeListener(Activity activity, OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        SoftKeyBoardListener softKeyBoardListener = new SoftKeyBoardListener(activity);
        softKeyBoardListener.setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
    }

    /**
     * 弹出输入法
     */
    public static void showSoftInput(EditText et) {
        et.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) SystemConfig.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(et, 0);
    }

    /**
     * 隐藏输入法
     */
    public static void hideSoftInput() {
        View focusView = ActivityUtils.getTopActivity().getCurrentFocus();
        if (focusView != null && focusView.getWindowToken() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) SystemConfig.context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    /**
     * 隐藏输入法
     */
    public static void hideSoftInput(EditText et) {
        InputMethodManager inputMethodManager = (InputMethodManager) SystemConfig.context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    /**
     * 如果点击 view 的外部则隐藏输入法
     * 注意：该方法要应该在 dispatchTouchEvent 方法中调用，以为在 onTouch 中有些 view 会消费掉触摸事件
     * 导致有些组件收不到触摸事件而无法进行相应的处理
     */
    public static void hideSoftInputIfTouchViewOutside(MotionEvent event, View targetView) {
        List<View> viewList = new ArrayList<>();
        viewList.add(targetView);
        hideSoftInputIfTouchViewOutside(event, viewList);
    }

    /**
     * 如果点击 view 的外部则隐藏输入法
     * 注意：该方法要应该在 dispatchTouchEvent 方法中调用，以为在 onTouch 中有些 view 会消费掉触摸事件
     * 导致有些组件收不到触摸事件而无法进行相应的处理
     */
    public static void hideSoftInputIfTouchViewOutside(MotionEvent event, View... views) {
        List<View> viewList = new ArrayList<>();
        Collections.addAll(viewList, views);
        hideSoftInputIfTouchViewOutside(event, viewList);
    }

    /**
     * 如果点击 view 的外部则隐藏输入法
     * 注意：该方法要应该在 dispatchTouchEvent 方法中调用，以为在 onTouch 中有些 view 会消费掉触摸事件
     * 导致有些组件收不到触摸事件而无法进行相应的处理
     */
    public static void hideSoftInputIfTouchViewOutside(MotionEvent event, List<View> viewList) {
        if (event.getAction() != MotionEvent.ACTION_DOWN) {
            return;
        }
        for (View view : viewList) {
            int[] location = {0, 0};
            view.getLocationInWindow(location);
            int left = location[0], top = location[1],
                    bottom = top + view.getHeight(), right = left + view.getWidth();
            if (event.getRawX() > left && event.getRawX() < right
                    && event.getRawY() > top && event.getRawY() < bottom) {
                return;
            }
        }
        hideSoftInput();
    }

    ///////////////////////////////////////////////////////////////////////////
    // 其它相关操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取Uri方式
     */
    public static Uri getUriFromFile(File file) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return Uri.fromFile(file);
        } else {
            return FileProvider.getUriForFile(SystemConfig.context, SystemConfig.context.getPackageName() + ".provider", file);
        }
    }

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *      4.4之前和之后的Uri是不同的，因此要使用不同的方式进行解析
     */
    public static File getFileFromUri(Uri imageUri) {
        if (imageUri == null)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(SystemConfig.context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return new File(android.os.Environment.getExternalStorageDirectory() + "/" + split[1]);
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId( Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return new File(getDataColumn(SystemConfig.context, contentUri, null, null));
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[] { split[1] };
                return new File(getDataColumn(SystemConfig.context, contentUri, selection, selectionArgs));
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return new File(imageUri.getLastPathSegment());
            return new File(getDataColumn(SystemConfig.context, imageUri, null, null));
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return new File(imageUri.getPath());
        }
        return null;
    }

    /**
     * 获取视频缩略图，截取第一帧
     */
    public static Bitmap getVideoThumb(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        return mediaMetadataRetriever.getFrameAtTime();
    }

    /**
     * 第二次按退出则关闭app，否则弹出提示
     */
    public static void exitTwice(String message) {
        if (exitTwice()) {
            if (SystemConfig.Config.killForExit) {
                // 友盟统计总默认app下次启动上报信息，为了准确性退出的时候直接退出进程
                System.exit(0);
            } else {
                ActivityUtils.closeAllActivity();
            }
        } else {
            Toast.makeText(SystemConfig.context, message, Toast.LENGTH_LONG).show();
        }
    }








    ///////////////////////////////////////////////////////////////////////////
    // 辅助实现
    ///////////////////////////////////////////////////////////////////////////


    /**
     * Android  6.0 之前（不包括6.0）
     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     */
    private static String getMacDefault() {
        String mac = "02:00:00:00:00:00";
        WifiManager wifi = (WifiManager) SystemConfig.context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) { }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0（包括） - Android 7.0（不包括）
     */
    private static String getMacFromFile() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }


    private static int getUserAppNum(String userApps) {
        String[] result = userApps.split("package:");
        return result.length;
    }

    private static String getProperty(String propName) {
        String property = CommandUtil.getProperty(propName);
        return TextUtils.isEmpty(property) ? null : property;
    }

    private static boolean exitTwice() {
        long newExitTime = System.currentTimeMillis();
        if (newExitTime - mExitTime > SystemConfig.Config.EXIT_TWICE_INTERVAL) {
            mExitTime = newExitTime;
            return false;
        } else {
            return true;
        }
    }

    private static void downloadApkThenInstall(String url, boolean upgrade) {
        final File fileDirectory = SystemConfig.getDownloadFilePath();
        final String fileName = String.format("%s.apk", Base64.encodeToString(url.getBytes(), Base64.NO_WRAP));
        DownloadUtils.download(url, new File(fileDirectory, fileName), upgrade ? new UpgradeDownloadListener() : new InstallDownloadListener());
    }

    private static class UpgradeDownloadListener implements DownloadUtils.OnDownloadListener {
        @Override public void started() {
            DialogUtils.showProgressDialog(ActivityUtils.getTopActivity(), "正在下载新版本", (int) 100, (int) 0);
        }
        @Override public void progress(long currentOffset, long totalLength) {
            DialogUtils.showProgressDialog(ActivityUtils.getTopActivity(), "正在下载新版本", (int) totalLength, (int) currentOffset);
        }
        @Override public void success(File file) {
            AppUtils.installApk(file);
            DialogUtils.dismissProgressDialog();
        }
        @Override public void error(Exception e) {
            Toast.makeText(SystemConfig.context, "下载失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
            DialogUtils.dismissProgressDialog();
        }
    }

    private static class InstallDownloadListener implements DownloadUtils.OnDownloadListener {
        @Override public void started() {
            Toast.makeText(SystemConfig.context, "开始下载...", Toast.LENGTH_LONG).show();
        }
        @Override public void progress(long currentOffset, long totalLength) { }
        @Override public void success(File file) {
            AppUtils.installApk(file);
        }
        @Override public void error(Exception e) {
            Toast.makeText(SystemConfig.context, "下载失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String complementUrl(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        return url;
    }

    /**
     * 8.0安装apk
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void installApk8(File file) {
        if (!SystemConfig.context.getPackageManager().canRequestPackageInstalls()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + SystemConfig.context.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityUtils.getTopActivity().startActivity(intent);
        }
        installApk7(file);
    }

    /**
     * 7.0安装apk
     */
    private static void installApk7(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(getUriFromFile(file), "application/vnd.android.package-archive");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        ActivityUtils.getTopActivity().startActivity(intent);
    }

    private static void delete(File directory, boolean keepRoot) {
        if (directory != null && directory.exists()) {
            if (directory.isDirectory()) {
                for (File subDirectory : directory.listFiles()) {
                    delete(subDirectory, false);
                }
            }
            if (!keepRoot) {
                directory.delete();
            }
        }
    }

    private static class SoftKeyBoardListener {
        private View rootView;               // activity的根视图
        private int rootViewVisibleHeight;          // 记录根视图的显示高度
        private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

        SoftKeyBoardListener(Activity activity) {
            //获取activity的根视图
            rootView = activity.getWindow().getDecorView();

            //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                    //获取当前根视图在屏幕上显示的大小
                    Rect r = new Rect();
                    rootView.getWindowVisibleDisplayFrame(r);
                    int visibleHeight = r.height();

                    if (rootViewVisibleHeight == 0) {
                        rootViewVisibleHeight = visibleHeight;
                        return;
                    }

                    //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
                    if (rootViewVisibleHeight == visibleHeight) {
                        return;
                    }

                    //根视图显示高度变小超过200，可以看作软键盘显示了
                    if (rootViewVisibleHeight - visibleHeight > 200) {
                        if (onSoftKeyBoardChangeListener != null) {
                            onSoftKeyBoardChangeListener.keyBoardShow(rootViewVisibleHeight - visibleHeight);
                        }
                        rootViewVisibleHeight = visibleHeight;
                        return;
                    }

                    //根视图显示高度变大超过200，可以看作软键盘隐藏了
                    if (visibleHeight - rootViewVisibleHeight > 200) {
                        if (onSoftKeyBoardChangeListener != null) {
                            onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - rootViewVisibleHeight);
                        }
                        rootViewVisibleHeight = visibleHeight;
                        return;
                    }
                }
            });
        }

        void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
            this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
        }
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
