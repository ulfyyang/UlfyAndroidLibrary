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
import java.util.Collections;
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
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * http 工具类
 */
public final class HttpUtils {
    private static OkHttpClient defaultClient;              // 默认的客户端，是一个开箱即用的客户端
    private static Map<Object, Object> globalHeaders;       // 全局 header，在每个客户端的每次请求中都会添加

    /**
     * 获取一个默认配置好的网络访问客户端，开箱即用
     */
    public synchronized static OkHttpClient defaultClient() {
        if (defaultClient == null) {
            defaultClient = newClient();
        }
        return defaultClient;
    }

    /**
     * 新建一个配置好的网络客户端
     */
    public synchronized static OkHttpClient newClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // 创建 cookie 记录器，支持所有 cookie 持久化到本地
        CookieJar cookieJar = new PersistentCookieJarNoFilter(
                new SetCookieCache(), new SharedPrefsCookiePersistor(HttpConfig.context));
        builder.cookieJar(cookieJar);                               // cookie 持久化存储

        // 创建日志打印器
        HttpLoggingInterceptor httpLogger = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                LogUtils.log(message);
            }
        });
        httpLogger.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(httpLogger);                            // 日志打印

        // 其它常规配置
        builder.connectTimeout(60, TimeUnit.SECONDS);        // 60 秒连接超时时间
        builder.readTimeout(60, TimeUnit.SECONDS);          // 60 秒读超时时间
        builder.addInterceptor(new GlobalHeaderInterceptor());      // 全局 header 添加
        HttpUtils.enableUnValidHttpsCertificate(builder);           // 允许未认证的 https 网络连接
        builder.proxy(Proxy.NO_PROXY);                              // 不使用代理，防止应用抓包

        // 设置有网络时和无网络时的网络缓存处理策略
        if (HttpConfig.Config.enableGetCache || HttpConfig.Config.enablePostCache) {
            builder.addNetworkInterceptor(new OnlineCacheInterceptor());
            builder.addInterceptor(new OfflineCacheInterceptor());
        }

        // 设置 Get、Post 请求缓存的具体实现
        if (HttpConfig.Config.enableGetCache) {
            builder.cache(new Cache(HttpConfig.getGetRequestCacheDir(),
                    HttpConfig.Config.cacheSize / 2 * 1024 * 1024));
        }
        if (HttpConfig.Config.enablePostCache) {
            NetPostCache netPostCache = new NetPostCache(HttpConfig.getPostRequestCacheDir(),
                    HttpConfig.Config.cacheSize / 2 * 1024 * 1024);
            builder.addInterceptor(new PostCacheInterceptor(netPostCache));
        }

        builder.protocols(Collections.singletonList(Protocol.HTTP_1_1));

        return builder.build();
    }

    /**
     * 设置全局header键值对
     */
    public synchronized static void addGlobalHeader(String key, String value) {
        if (globalHeaders == null) {
            globalHeaders = new HashMap<>();
        }
        globalHeaders.put(key, value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // GET 请求参数生成相关方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 生成 Get 请求 Url（附带请求参数）
     */
    public static String generateGetUrl(String url, Map<Object, Object> params) {
        String queryString = generateQueryString(params);
        if (queryString == null || queryString.length() == 0) {
            return url;
        } else {
            return String.format("%s?%s", url, queryString);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // POST 请求参数生成相关方法
    ///////////////////////////////////////////////////////////////////////////

    /*
    常用媒体类型：
        text/html、text/plain、text/css、text/javascript
        application/x-www-form-urlencoded、application/json、application/xml
        multipart/form-data
     */

    /**
     * 生成表单请求体（不包含文件、二进制）
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
            return RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), generateQueryString(params));
        }
    }

    /**
     * 生成 Multipart 表单请求体（包含文件、二进制），如果传递的是文件数组的话，则可以自己按照命名要求生成KEY
     *      - 比如：imgs[0]、imgs[1]
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
     * 生成 Json 格式的请求体
     */
    public static RequestBody generateJsonBody(String jsonContent) {
        return RequestBody.create(MediaType.parse("application/json"), jsonContent);
    }

    ///////////////////////////////////////////////////////////////////////////
    // 基础方法
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 根据请求参数生成 url 的查询参数部分字符串
     */
    public static String generateQueryString(Map<Object, Object> params) {
        List<String> paramsStringList = new ArrayList<>();
        if (params != null && params.size() > 0) {
            for (Map.Entry<Object, Object> entry : params.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    paramsStringList.add(String.format("%s=%s",
                            entry.getKey().toString(), entry.getValue().toString()));
                }
            }
        }
        return Joiner.on("&").join(paramsStringList);
    }

    /**
     * 执行一个请求
     */
    public static String execute(OkHttpClient okHttpClient, Request request) throws Exception {
        try {
            Response response = okHttpClient.newCall(request).execute();
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


    ///////////////////////////////////////////////////////////////////////////
    // 内部支持实现
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 全局 header 拦截器，在每次请求执行前会添加全局设定的 header
     */
    private static final class GlobalHeaderInterceptor implements Interceptor {
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
        if (sslSocketFactory != null) {
            builder.sslSocketFactory(sslSocketFactory, xtm);
        }

        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        builder.hostnameVerifier(hostnameVerifier);
    }

    /**
     * 有网络时候的缓存处理
     */
    private static final class OnlineCacheInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            Request request = chain.request(); Response response = chain.proceed(request);

            // 如果服务端设置了缓存则直接使用服务端的设置，服务端必须设置 max-age 字段才能生效
            if (response.cacheControl() != null && !response.cacheControl().noStore()) {
                return response;
            }

            // 如果服务端没有相关设置则采用自定义设置
            if (HttpConfig.Config.enableOnlineCacheCustomSetting) {
                int expirationTime = HttpConfig.Config.onlineUrlExpirationTime
                        .containsKey(request.url().toString()) ?
                        HttpConfig.Config.onlineUrlExpirationTime.get(request.url().toString()) :
                        HttpConfig.Config.onlineExpirationTime;
                return response.newBuilder()
                        .header("Cache-Control", "public, max-age=" + expirationTime)
                        .removeHeader("Pragma").build();
            }

            return response;        // 如果没有特殊需求则直接按照原来的处理
        }
    }

    /**
     * 无网络时候的缓存处理
     */
    private static final class OfflineCacheInterceptor implements Interceptor {
        @Override public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (HttpConfig.Config.enableOfflineCache) {
                ConnectivityManager connectivityManager = (ConnectivityManager) HttpConfig.context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                // 没有打开网络连接开关 或 网络连接开关已经打开，但是无网络连接
                if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
                    request = request.newBuilder()
                            // 两种方式结果是一样的，写法不同
                            // .cacheControl(new CacheControl.Builder().maxStale(60,TimeUnit.SECONDS).onlyIfCached().build())
                            .header("Cache-Control", "public, only-if-cached, max-stale=" +
                                    HttpConfig.Config.offlineExpirationTime)
                            .build();
                }
            }

            return chain.proceed(request);
        }
    }
}
