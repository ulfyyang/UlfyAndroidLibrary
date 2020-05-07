package com.sy.comment;

import android.support.multidex.MultiDexApplication;

import com.sy.comment.ui.activity.MainActivity;
import com.ulfy.android.bus.BusConfig;
import com.ulfy.android.cache.CacheConfig;
import com.ulfy.android.dialog.DialogConfig;
import com.ulfy.android.download_manager.DownloadManagerConfig;
import com.ulfy.android.image.ImageConfig;
import com.ulfy.android.okhttp.HttpConfig;
import com.ulfy.android.system.AppUtils;
import com.ulfy.android.system.SystemConfig;
import com.ulfy.android.task.TaskConfig;
import com.ulfy.android.task_transponder.TaskTransponderConfig;
import com.ulfy.android.time.TimeConfig;
import com.ulfy.android.utils.UtilsConfig;

public class MainApplication extends MultiDexApplication {

    public static MainApplication application;              // 对外提供全局的上下文访问，使用时要注意避免内存泄漏

    @Override public void onCreate() {
        super.onCreate();
        application = this;

        AppUtils.enableUnValidHttpsCertificate();   // 允许访问未认证证书的 https 网络
        CacheConfig.initDefaultCache(this);         // 配置app用的默认缓存
        BusConfig.init(this);                       // 初始化事件总线
        TaskConfig.init(this);                      // 配置任务引擎
        TaskTransponderConfig.init(this);           // 配置任务响应器
        TimeConfig.init(this);                      // 配置时间跟踪
        DownloadManagerConfig.init(this);           // 配置下载管理器
        DialogConfig.init(this);                    // 配置弹出框
        ImageConfig.init(this);                     // 配置图片处理
        SystemConfig.init(this);                    // 初始化系统模块
        HttpConfig.init(this);                      // 配置网络层
        UtilsConfig.init(this);                     // 工具模块配置
        // 配置多域名选择器（按需选择）
//        MultiDomainPickerConfig.init(this, Arrays.asList(BuildConfig.HTTP_BASES));

        // 其它三方库初始化
    }

}