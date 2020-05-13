package com.ulfy.android.okhttp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.common.base.Joiner;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * http工具累
 */
public final class HttpUtils {
    private static OkHttpClient okHttpClient;
    private static Map<Object, Object> globalHeaders;

    /**
     * 设置全局header键值对
     */
    public static void globalHeader(String key, String value) {
        if (globalHeaders == null) {
            globalHeaders = new HashMap<>();
        }
        globalHeaders.put(key, value);
    }

    /**
     * 避免app被意外回收后恢复时okHttpClient空指针
     */
    public static OkHttpClient getClient() {
        if (okHttpClient == null) {
            // 创建cookie记录器
            CookieJar cookieJar = new PersistentCookieJarNoFilter(new SetCookieCache(), new SharedPrefsCookiePersistor(HttpConfig.context));
            // 创建日志打印器
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                public void log(String message) {
                    LogUtils.log(message);
                }
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            // 开始进行配置
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.cookieJar(cookieJar);                               // cookie 持久化存储
            builder.connectTimeout(5, TimeUnit.SECONDS);        // 5 秒连接超时时间
            builder.readTimeout(10, TimeUnit.SECONDS);          // 10 秒超时时间
            builder.addInterceptor(logging);                            // 日志打印
            builder.addInterceptor(new GlobalHeaderInterceptor());      // 全局 header 添加
            enableUnValidHttpsCertificate(builder);                     // 允许未认证的 https 网络连接
            // 设置有网络时和无网络时的网络缓存处理策略
            if (HttpConfig.Config.enableGetCache || HttpConfig.Config.enablePostCache) {
                builder.addNetworkInterceptor(new OnlineCacheInterceptor());
                builder.addInterceptor(new OfflineCacheInterceptor());
            }
            // 设置允许Get缓存
            if (HttpConfig.Config.enableGetCache) {
                builder.cache(new Cache(HttpConfig.getGetRequestCacheDir(), HttpConfig.Config.cacheSize / 2 * 1024 * 1024));
            }
            // 设置允许Post缓存
            if (HttpConfig.Config.enablePostCache) {
                NetPostCache netPostCache = new NetPostCache(HttpConfig.getPostRequestCacheDir(), HttpConfig.Config.cacheSize / 2 * 1024 * 1024);
                builder.addInterceptor(new PostCacheInterceptor(netPostCache));
            }
            builder.proxy(Proxy.NO_PROXY);                               // 手动添加代理防止代理工具抓包
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    /**
     * 允许未认证的 https 网络连接
     */
    private static void enableUnValidHttpsCertificate(OkHttpClient.Builder builder) {
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
            builder.sslSocketFactory(sslSocketFactory, xtm);
        }
        builder.hostnameVerifier(hostnameVerifier);
    }

    /**
     * 有网络时候的缓存处理
     */
    public static class OnlineCacheInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);
            // 如果服务端设置了缓存则直接使用服务端的设置。服务端必须设置max-age字段才能生效
            if (response.cacheControl() != null && !response.cacheControl().noStore()) {
                return response;
            }
            // 如果服务端没有相关设置则采用自定义设置
            if (HttpConfig.Config.enableOnlineCacheCustomSetting) {
                int expirationTime = HttpConfig.Config.onlineUrlExpirationTime.containsKey(request.url().toString()) ?
                        HttpConfig.Config.onlineUrlExpirationTime.get(request.url().toString()) : HttpConfig.Config.onlineExpirationTime;
                return response.newBuilder()
                        .header("Cache-Control", "public, max-age=" + expirationTime)
                        .removeHeader("Pragma").build();
            }
            // 如果没有特殊需求则直接按照原来的处理
            return response;
        }
    }

    /**
     * 无网络时候的缓存处理
     */
    public static class OfflineCacheInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (HttpConfig.Config.enableOfflineCache) {
                ConnectivityManager connectivityManager = (ConnectivityManager) HttpConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                // 没有打开网络连接开关 或 网络连接开关已经打开，但是无网络连接
                if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
                    request = request.newBuilder()
                            // 两种方式结果是一样的，写法不同
                            // .cacheControl(new CacheControl.Builder().maxStale(60,TimeUnit.SECONDS).onlyIfCached().build())
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + HttpConfig.Config.offlineExpirationTime)
                            .build();
                }
            }
            return chain.proceed(request);
        }
    }

    public static class GlobalHeaderInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            Request.Builder requestBuilder = chain.request().newBuilder();
            if (globalHeaders != null && globalHeaders.size() > 0) {
                for (Map.Entry<Object, Object> entry : globalHeaders.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        requestBuilder.header(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
            return chain.proceed(requestBuilder.build());
        }
    }

    /**
     * 生成Get请求的字符串格式参数
     */
    public static String generateGetBody(Map<Object, Object> params) {
        List<String> paramsStringList = new ArrayList<>();
        if (params != null && params.size() > 0) {
            for (Map.Entry<Object, Object> entry : params.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    paramsStringList.add(String.format("%s=%s", entry.getKey().toString(), entry.getValue().toString()));
                }
            }
        }
        return Joiner.on("&").join(paramsStringList);
    }

    /**
     * 生成Get请求Url
     */
    public static String generateGetUrl(String url, Map<Object, Object> params) {
        String requestBody = generateGetBody(params);
        if (requestBody.length() == 0) {
            return url;
        } else {
            return String.format("%s?%s", url, requestBody);
        }
    }

    /*
    1.text/html
    2.text/plain
    3.text/css
    4.text/javascript
    5.application/x-www-form-urlencoded
    6.multipart/form-data
    7.application/json
    8.application/xml
     */

    /**
     * 生成一个已字符串为内容的请求体
     * @param mediaType 内容的类型
     * @param content   内容
     */
    public static RequestBody generateRequestBody(MediaType mediaType, String content) {
        return RequestBody.create(mediaType, content);
    }

    /**
     * 生成表单请求体
     */
    public static RequestBody generateFormBody(Map<Object, Object> params, boolean urlEncode) {
        if (urlEncode) {
            FormBody.Builder builder = new FormBody.Builder();
            if (params != null && params.size() > 0) {
                for (Map.Entry<Object, Object> entry : params.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        builder.add(entry.getKey().toString(), entry.getValue().toString());
                    }
                }
            }
            return builder.build();
        } else {
            return RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), generateGetBody(params));
        }
    }

    /**
     * 生成Multipart表单请求体
     */
    public static RequestBody generateMultipartFormBody(Map<Object, Object> params, Map<Object, File> files) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (files != null && files.size() > 0) {
            for (Map.Entry<Object, File> entry : files.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    RequestBody body = RequestBody.create(MediaType.parse("*/*"), entry.getValue());
                    builder.addFormDataPart(entry.getKey().toString(), entry.getValue().getName(), body);      // 参数分别为：请求key、文件名称、RequestBody
                }
            }
        }
        if (params != null && params.size() > 0) {
            for (Map.Entry<Object, Object> entry : params.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    builder.addFormDataPart(entry.getKey().toString(), entry.getValue().toString());           // map 里面是请求中所需要的 key 和 value
                }
            }
        }
        return builder.build();
    }

    /**
     * 执行一个Get请求
     * @param url 需要是完整的地址
     */
    public static String get(String url) throws Exception {
        return execute(new Request.Builder().url(url).get().build());
    }

    /**
     * 执行一个post请求
     */
    public static String post(String url, RequestBody requestBody) throws Exception {
        return execute(new Request.Builder().url(url).post(requestBody).build());
    }

    /**
     * 执行一个请求
     */
    public static String execute(Request request) throws Exception {
        try {
            Response response = getClient().newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IllegalStateException(String.format("网络连接失败，错误码:%d", response.code()));
            }
        } catch (ConnectException e) {
            throw new IllegalStateException("网络链接失败", e);
        } catch (SocketTimeoutException e) {
            throw new IllegalStateException("网络链接超时", e);
        } catch (Exception e) {
            throw new IllegalStateException("网络错误", e);
        }
    }

}
