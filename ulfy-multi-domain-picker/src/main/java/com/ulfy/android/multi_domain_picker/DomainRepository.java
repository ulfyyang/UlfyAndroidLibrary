package com.ulfy.android.multi_domain_picker;

import com.ulfy.android.cache.ICache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class DomainRepository implements Serializable {
    private static final long serialVersionUID = -4698594654416090799L;
    transient DomainFilter domainFilter;                        // 域名过滤器
    transient DomainTesterConverterProvider domainProvider;     // 域名测试器转换器提供者
    transient NetworkDetector networkDetector;                  // 网络状态探测器
    transient List<String> originalDomainList;                  // 原始的待测试域名，由外部传入
    transient List<Domain> waitForTestDomainList;               // 真实的待测试域名，用于内部测试使用。在测试时会剔除失效的域名，当没有域名可测试时会重新从原始域名中恢复
    private String key;                                         // 用于跟踪每个场景的KEY，如果没有则为全局
    Domain targetDomain;                                        // 最终使用的域名

    // 存储KEY->域名仓库键值对
    static class RepositoryMap implements Serializable {
        private static final long serialVersionUID = -8323003321257337001L;
        private Map<String, DomainRepository> domainRepositoryMap = new HashMap<>();
    }

    /**
     * 获取单例对象
     */
    static DomainRepository getInstance(String key) {
        ICache cache = MultiDomainPickerConfig.cache;
        if (key == null || key.length() == 0) {
            return cache.isCached(DomainRepository.class) ? cache.getCache(DomainRepository.class) :
                    cache.cache(new DomainRepository());
        } else {
            RepositoryMap repositoryMap = cache.isCached(RepositoryMap.class) ? cache.getCache(RepositoryMap.class):
                    cache.cache(new RepositoryMap());
            DomainRepository domainRepository = repositoryMap.domainRepositoryMap.get(key);
            if (domainRepository == null) {
                domainRepository = new DomainRepository();
                domainRepository.key = key;
                repositoryMap.domainRepositoryMap.put(key, domainRepository);
                cache.cache(repositoryMap);
            }
            return domainRepository;
        }
    }

    /**
     * 刷新到硬盘缓存
     */
    void updateToCahe() {
        ICache cache = MultiDomainPickerConfig.cache;
        if (key == null || key.length() == 0) {
            cache.cache(this);
        } else {
            RepositoryMap repositoryMap = cache.getCache(RepositoryMap.class);
            repositoryMap.domainRepositoryMap.put(key, this);
            cache.cache(repositoryMap);
        }
    }

    void initOriginalDomainList(List<String> originalDomainList) {
        this.originalDomainList = originalDomainList;
    }

    /**
     * 初始化，为了避免在还原序列化时出现的null属性，获取DomainRepository实例后要调用该方法
     */
    void init(DomainFilter domainFilter, DomainTesterConverterProvider domainProvider, NetworkDetector networkDetector) {
        this.domainFilter = domainFilter;
        this.domainProvider = domainProvider;
        this.networkDetector = networkDetector;
    }

    /**
     * 如果待测域名没有了或用光了则重新恢复这些域名去测试
     */
    void inflateWaitForTestDomainListIfEmpty() {
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
     * 获取目标域名（添加线程同步，如果获取一次之后下一次可直接采用缓存）
     * @param useCache  如果当前存在有效的域名缓存则直接获取缓存域名，无论是否使用缓存最终的结构都会保存到缓存中
     */
    synchronized Domain getTargetDomain(boolean useCache) throws Exception {

        /*
            1. 如果存在有效的域名缓存，直接从缓存中获取
            2. 如果没有缓存域名则测试待测试域名并找到新的域名
            3. 当无法找到新域名时，尽量采用缓存的域名即使其已经失效
         */

        // 如果当前域名有效则直接获取当前域名
        if (useCache && targetDomain != null && targetDomain.isValid()) {
            return targetDomain;
        }

        // 无网络直接报无网络错误
        boolean isConnected = networkDetector.isNetworkConnected();
        if (!isConnected) {
            throw new IllegalStateException("当前无网络链接");
        }

        // 如果待测试的域名列表不存在，则填充
        inflateWaitForTestDomainListIfEmpty();
        // 如果填充后待测试域名列表为空则说明没有域名
        if (waitForTestDomainList == null || waitForTestDomainList.isEmpty()) {
            throw new IllegalStateException("域名列表不能为空");
        }

        // 对域名列表进行测试、过滤、转换
        DomainFilter.Result result = domainFilter.filter(waitForTestDomainList, domainProvider);

        // 从待测试域名中移除这些无效的域名
        if (result.invalidDomainList != null && !result.invalidDomainList.isEmpty()) {
            waitForTestDomainList.removeAll(result.invalidDomainList);
        }

        // 如果找到了有效的域名，则保存改域名并返回
        if (result.validDomain != null) {
            targetDomain = result.validDomain;
            updateToCahe();
            return targetDomain;
        }

        // 如果最终没有找到目标域名，有失效的缓存则采用失效缓存，连失效的缓存也没有则包网络错误
        if (useCache && targetDomain != null) {
            return targetDomain;
        } else {
            throw new Exception("网络链接失败");
        }
    }

    /**
     * 使当前正在使用的目标域名失效
     *      这个通常时客户端在访问接口的过程中发现了域名不可用，这时可主动使其失效
     */
    void invalidateTargetDomain() {
        if (targetDomain == null) {
            return;
        }
        targetDomain.invalidate();
        updateToCahe();
        if (waitForTestDomainList == null || waitForTestDomainList.isEmpty()) {
            return;
        }
        Iterator<Domain> iterator = waitForTestDomainList.iterator();
        while (iterator.hasNext()) {
            Domain next = iterator.next();
            if (targetDomain.getOriginalUrl().equals(next.getOriginalUrl())) {
                iterator.remove();
                break;      // 因为只有一个targetDomain，所以删除一个就可以了
            }
        }
    }

    /**
     * 重置域名选择器
     *      会重置已缓存域名
     *      会重置等待测试域名列表
     */
    void reset() {
        waitForTestDomainList = null; targetDomain = null;
        updateToCahe();
        inflateWaitForTestDomainListIfEmpty();
    }
}
