package com.ulfy.android.okhttp;

import android.app.Application;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class HttpConfig {
    static Application context;

    static void init(Application context) {
        HttpConfig.context = context;
    }

    public static final class Config {
        public static String LOG_TAG = "ulfy-log";                                      // 网络日志打印 tag
        public static boolean enableGetCache = false;                                   // 是否开启Get请求缓存
        public static boolean enablePostCache = false;                                  // 是否开启Post请求缓存
        public static boolean enableOnlineCacheCustomSetting = false;                   // 是否开启在线缓存自定义设置。该设置包含了针对每条域名的时间设置和公共设置。如果服务端响应头中有相关设置则优先使用服务端设置
        public static Map<String, Integer> onlineUrlExpirationTime = new HashMap<>();   // 针对每条访问路径的在线缓存过期时间。单位秒
        public static int onlineExpirationTime = 60;                                    // 在线缓存过期时间，默认一分钟。单位秒
        public static boolean enableOfflineCache = false;                               // 是否启用离线缓存，该设置生效的前提时启动了对应的get或post缓存
        public static int offlineExpirationTime = Integer.MAX_VALUE;                    // 离线缓存过期时间，默认无限长。单位秒
        public static int cacheSize = 100;                                              // 网络缓存的总大小（GET、POST 各占一半）。单位M
        public static RequestCacheKeyConverter requestCacheKeyConverter;                // 网络请求参数缓存唯一key算法转换器

        /**
         * 网络请求缓存时的key转换算法
         *      对于有些经常多变的参数可以人为的排除以确保其有效性
         */
        public interface RequestCacheKeyConverter {
            /**
             * 将请求的参数转化为具有唯一性的参数形式
             */
            public String convert(String params);
        }

        /**
         * 表单请求唯一Key转换器
         */
        public static class FormRequestCacheKeyConverter implements RequestCacheKeyConverter {
            private List<String> filterKeyList;

            public FormRequestCacheKeyConverter(String... filterKeys) {
                this(Arrays.asList(filterKeys));
            }

            public FormRequestCacheKeyConverter(List<String> filterKeyList) {
                this.filterKeyList = filterKeyList;
            }

            @Override public String convert(String params) {
                if (params == null || params.length() == 0) {
                    return params;
                } else {
                    List<String> keyValueParList = new CopyOnWriteArrayList<>(Splitter.on('&').omitEmptyStrings().splitToList(params));

                    for (String keyValuePar : keyValueParList) {
                        if (shouldRemoveKey(keyValuePar)) {
                            keyValueParList.remove(keyValuePar);
                        }
                    }

                    params = Joiner.on('&').join(keyValueParList);

                    return params;
                }
            }

            private boolean shouldRemoveKey(String keyValuePar) {
                if (filterKeyList != null && filterKeyList.size() > 0) {
                    for (String key : filterKeyList) {
                        if (keyValuePar.contains(key)) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * 获取网络请求缓存文件路径
     */
    static File getGetRequestCacheDir() {
        File dir = new File(context.getFilesDir(), "get_request_cache");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }


    /**
     * 获取网络请求缓存文件路径
     */
    static File getPostRequestCacheDir() {
        File dir = new File(context.getFilesDir(), "post_request_cache");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
