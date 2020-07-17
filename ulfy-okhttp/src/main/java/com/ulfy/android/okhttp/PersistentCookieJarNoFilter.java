package com.ulfy.android.okhttp;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.cache.CookieCache;
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * 可持久化 Session Cookies 和 Persistent Cookies
 *      1）Session Cookies 是临时的 cookie文件，在你关闭浏览器之后就会失效并被删除掉。
 *          在安卓中，杀死app就相当于关闭了浏览器，cookie就会失效。
 *      2）Persistent Cookies 会被保存在一个浏览器的一个子文件夹中，除非手动删除或者浏
 *          览器定期清理，否则会一直存在。在安卓中，会持久存储在本地。
 * 这里做了特殊处理，对所有类型的 cookie 都做持久化处理 {@link com.franmontiel.persistentcookiejar.PersistentCookieJar#saveFromResponse(HttpUrl, List)}
 */
final class PersistentCookieJarNoFilter implements ClearableCookieJar {
    private CookieCache cache;              // 内存级别存储
    private CookiePersistor persistor;      // 持久化级别存储

    PersistentCookieJarNoFilter(CookieCache cache, CookiePersistor persistor) {
        this.cache = cache;
        this.persistor = persistor;
        this.cache.addAll(persistor.loadAll());     // 无论 cookie 是否是持久化 cookie，都进行持久化
    }

    @Override synchronized public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies);
        persistor.saveAll(cookies);     // 无论 cookie 是否是持久化 cookie，都进行持久化
    }

    @Override synchronized public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookiesToRemove = new ArrayList<>();
        List<Cookie> validCookies = new ArrayList<>();

        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();

            if (isCookieExpired(currentCookie)) {
                cookiesToRemove.add(currentCookie);
                it.remove();

            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

        persistor.removeAll(cookiesToRemove);

        return validCookies;
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    @Override synchronized public void clearSession() {
        cache.clear();
        cache.addAll(persistor.loadAll());
    }

    @Override synchronized public void clear() {
        cache.clear();
        persistor.clear();
    }
}
