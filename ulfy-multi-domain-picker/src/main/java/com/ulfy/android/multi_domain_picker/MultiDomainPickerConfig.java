package com.ulfy.android.multi_domain_picker;

import android.content.Context;

import com.ulfy.android.cache.CacheConfig;
import com.ulfy.android.cache.ICache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MultiDomainPickerConfig {
    static Context context;
    static ICache cache;        // 用于管理持久化状态的缓存对象

    static void init(Context context) {
        MultiDomainPickerConfig.context = context;
        MultiDomainPickerConfig.cache = CacheConfig.newMemoryDiskCache(context, Config.recordInfoCacheDirName);
    }

    /**
     * 初始化
     */
    public static void init(List<String> originalDomainList) {
        init(null, originalDomainList);
    }

    /**
     * 初始化
     * @param key           跟踪的场景KEY
     */
    public static void init(String key, List<String> originalDomainList) {
        DomainRepository.getInstance(key).initOriginalDomainList(originalDomainList);
    }

    static class DomainTesterConverterProviderInner implements DomainTesterConverterProvider {
        @Override public DomainTester tester(String url) {
            return Config.findDomainTesterByUrl(url);
        }
        @Override public DomainConverter converter(String url) {
            return Config.findDomainConverterByUrl(url);
        }
    }

    /*
    ===================================== 全局配置 ================================================
     */
    public static final class Config {
        public static DomainTester domainTester = new PingDomainTester();                               // 域名测试器
        private static final Map<String, DomainTester> urlDomainTesterMap = new HashMap<>();            // 针对特定域名的特殊测试器
        public static DomainConverter domainConverter = new CopyDomainConverter();                      // 域名转换器（默认域名转换器）
        private static final Map<String, DomainConverter> urlDomainConverterMap = new HashMap<>();      // 针对特定域名的的特殊转换器配置
        public static String recordInfoCacheDirName = "multi_domain_picker_cache";                      // 用于跟踪下载信息的缓存目录

        /**
         * 配置特殊域名的测试器
         */
        public static void configUrlTester(String url, DomainTester tester) {
            if (url != null && url.length() > 0) {
                urlDomainTesterMap.put(url, tester);
            }
        }

        /**
         * 配置特定域名的转换器
         */
        public static void configUrlConverter(String url, DomainConverter converter) {
            if (url != null && url.length() > 0) {
                urlDomainConverterMap.put(url, converter);
            }
        }

        static DomainTester findDomainTesterByUrl(String url) {
            if (url == null || url.length() == 0) {
                return null;
            }
            DomainTester tester = urlDomainTesterMap.containsKey(url) ? urlDomainTesterMap.get(url) : domainTester;
            if (tester == null) {
                throw new IllegalStateException("Cant not find DomainTester for the specific url, " +
                        "you must config it for url or retain the default tester");
            }
            return tester;
        }

        static DomainConverter findDomainConverterByUrl(String url) {
            if (url == null || url.length() == 0) {
                return null;
            }
            DomainConverter converter = urlDomainConverterMap.containsKey(url) ? urlDomainConverterMap.get(url) : domainConverter;
            if (converter == null) {
                throw new IllegalStateException("Cant not find DomainConverter for the specific url, " +
                        "you must config it for url or retain the default converter");
            }
            return converter;
        }

        static void clear() {
            urlDomainTesterMap.clear();
            urlDomainConverterMap.clear();
        }
    }
}
