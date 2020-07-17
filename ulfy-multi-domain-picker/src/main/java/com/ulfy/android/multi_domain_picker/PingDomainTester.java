package com.ulfy.android.multi_domain_picker;

import java.net.URL;

/**
 * Ping域名测试器(Dns测试由于是查找的本地缓存，不准确，因此没有提供对应的实现）
 *      Ping测试的准确性比较高，因为是需要能Ping通才行
 *      通过对目标域名进行ping测试来判断域名是否可以访问
 */
public final class PingDomainTester implements DomainTester {

    // 获取一行ping的结果，deadline是5秒钟
    @Override public boolean test(String originalUrl) throws Exception {
        if (originalUrl == null || originalUrl.length() == 0) {
            return false;
        }

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "http://" + originalUrl;
        }

        String host = new URL(originalUrl).getHost();
        Process process = Runtime.getRuntime().exec("ping -c 1 -w 5 " + host);
        int exitCode = process.waitFor();
        boolean isSuccess = exitCode == 0;

        return isSuccess;
    }

}
