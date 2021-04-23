package com.ulfy.android.multi_domain_picker;

public final class MultiDomainPicker {
    private static final MultiDomainPicker instance = new MultiDomainPicker();        // 单例对象
    private MultiDomainPicker() { }
    public static MultiDomainPicker getInstance() {
        return instance;
    }

    /**
     * 获取目标域名Url
     * @param useCache      是否使用缓存
     */
    public final String getTargetDomainUrl(boolean useCache) throws Exception {
        return getTargetDomainUrl(null, useCache);
    }

    /**
     * 获取目标域名Url
     * @param key           跟踪的场景KEY
     * @param useCache      是否使用缓存
     */
    public final String getTargetDomainUrl(String key, boolean useCache) throws Exception {
        MultiDomainPickerConfig.throwExceptionIfConfigNotConfigured();
        DomainRepository domainRepository = DomainRepository.getInstance(key);
        domainRepository.init(new SerialDomainFilter(), new MultiDomainPickerConfig.DomainTesterConverterProviderInner(),
                new AndroidNetworkDetector(MultiDomainPickerConfig.context));
        return domainRepository.getTargetDomain(useCache).getTargetUrl();
    }

    /**
     * 使当前正在使用的目标域名失效
     *      这个通常时客户端在访问接口的过程中发现了域名不可用，这时可主动使其失效
     */
    public final void invalidateDomain() {
        invalidateDomain(null);
    }

    /**
     * 使当前正在使用的目标域名失效
     *      这个通常时客户端在访问接口的过程中发现了域名不可用，这时可主动使其失效
     * @param key           跟踪的场景KEY
     */
    public final void invalidateDomain(String key) {
        MultiDomainPickerConfig.throwExceptionIfConfigNotConfigured();
        DomainRepository.getInstance(key).invalidateTargetDomain();
    }

    /**
     * 重置域名选择器
     *      会重置已缓存域名
     *      会重置等待测试域名列表
     */
    public final void reset() {
        reset(null);
    }

    /**
     * 重置域名选择器
     *      会重置已缓存域名
     *      会重置等待测试域名列表
     * @param key           跟踪的场景KEY
     */
    public final void reset(String key) {
        MultiDomainPickerConfig.throwExceptionIfConfigNotConfigured();
        DomainRepository.getInstance(key).reset();
    }
}
