package com.ulfy.android.multi_domain_picker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ulfy.android.cache.ICache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class DomainRepository implements Serializable {
    private static final long serialVersionUID = -4698594654416090799L;
    private transient List<String> originalDomainList;              // 原始的待测试域名，由外部传入
    private transient List<Domain> waitForTestDomainList;           // 真实的待测试域名，用于内部测试使用。在测试时会剔除失效的域名，当没有域名可测试时会重新从原始域名中恢复
    private Domain targetDomain;                                    // 最终使用的域名

    /**
     * 私有化构造方法
     */
    private DomainRepository() { }

    /**
     * 获取单例对象
     */
    synchronized static DomainRepository getInstance() {
        ICache cache = MultiDomainPickerConfig.cache;
        return cache.isCached(DomainRepository.class) ? cache.getCache(DomainRepository.class) : cache.cache(new DomainRepository());
    }

    /**
     * 初始化
     */
    synchronized void init(List<String> originalDomainList) {
        this.originalDomainList = originalDomainList;
    }

    /**
     * 获取目标域名
     * @param useCache  如果当前存在有效的域名缓存则直接获取缓存域名
     */
    synchronized Domain getTargetDomain(boolean useCache, DomainTesterConverterProvider provider) throws Exception {

        /*
            1. 如果存在有效的域名缓存，直接从缓存中获取
            2. 如果没有缓存域名则依次测试待测试域名并找到新的域名
            3. 当无法找到新域名时，尽量采用缓存的域名即使其已经失效
         */

        // 如果当前域名有效则直接获取当前域名
        if (useCache && targetDomain != null && targetDomain.isValid()) {
            return targetDomain;
        }

        // 无网络直接报无网络错误
        boolean isConnected = isNetworkConnected();
        if (!isConnected) {
            throw new IllegalStateException("当前无网络链接");
        }

        resumeWaitForTestDomainListIfNeed();

        // 依次测试可用域名，发现不可用的从待测域名中移除；发现可用更新缓存后直接使用
        Iterator<Domain> iterator = waitForTestDomainList.iterator();
        while (iterator.hasNext()) {
            Domain next = iterator.next();
            if (next.testThenConvertDomain(provider.tester(next.getOriginalUrl()), provider.converter(next.getOriginalUrl()))) {
                targetDomain = next;
                updateToCahe();
                return next;
            } else {
                iterator.remove();
            }
        }

        // 如果最终没有找到目标域名，有失效的缓存则采用失效缓存，连失效的缓存也没有则包网络错误
        if (useCache && targetDomain != null) {
            return targetDomain;
        } else {
            throw new Exception("网络链接失败");
        }
    }

    boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) MultiDomainPickerConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * 使当前正在使用的目标域名失效
     *      这个通常时客户端在访问接口的过程中发现了域名不可用，这时可主动使其失效
     */
    synchronized void invalidateTargetDomain() {
        if (targetDomain != null) {
            targetDomain.invalidate();

            // 从等待测试域名中移除失效的域名。由于不见得所有待测试域名都都有targetUrl，因此使用originalUrl进行比较
            if (waitForTestDomainList != null && waitForTestDomainList.size() > 0) {
                Iterator<Domain> iterator = waitForTestDomainList.iterator();
                while (iterator.hasNext()) {
                    Domain next = iterator.next();

                    if (targetDomain.getOriginalUrl().equals(next.getOriginalUrl())) {
                        iterator.remove();
                    }
                }
            }

            updateToCahe();
        }
    }

    /**
     * 重置域名选择器
     *      会重置已缓存域名
     *      会重置等待测试域名列表
     */
    synchronized void reset() {
        waitForTestDomainList = null;
        targetDomain = null;
        resumeWaitForTestDomainListIfNeed();
        updateToCahe();
    }

    /**
     * 如果待测域名没有了或用光了则重新恢复这些域名去测试
     */
    private void resumeWaitForTestDomainListIfNeed() {
        if (waitForTestDomainList == null) {
            waitForTestDomainList = new ArrayList<>();
        }
        if (waitForTestDomainList.size() == 0 && originalDomainList != null && originalDomainList.size() > 0) {
            for (String originalDomain : originalDomainList) {
                waitForTestDomainList.add(new Domain(originalDomain));
            }
        }
    }

    /**
     * 刷新到硬盘缓存
     */
    private void updateToCahe() {
        MultiDomainPickerConfig.cache.cache(this);
    }

    /**
     * 用于灵活的提供测试器和转换器而设计的类
     */
    interface DomainTesterConverterProvider {
        DomainTester tester(String url);
        DomainConverter converter(String url);
    }
}
