package com.ulfy.android.multi_domain_picker;

public final class MultiDomainPicker {
    private static final MultiDomainPicker instance = new MultiDomainPicker();        // 单例对象

    /**
     * 私有化构造方法
     */
    private MultiDomainPicker() { }

    /**
     * 获取单例对象
     */
    public static MultiDomainPicker getInstance() {
        return instance;
    }

    /**
     * 获取目标域名Url
     * @param useCache      是否使用缓存
     */
    public final String getTargetDomainUrl(boolean useCache) throws Exception {
        MultiDomainPickerConfig.throwExceptionIfConfigNotConfigured();
        return DomainRepository.getInstance().getTargetDomain(useCache, new DomainRepository.DomainTesterConverterProvider() {
            @Override public DomainTester tester(String url) {
                return MultiDomainPickerConfig.Config.findDomainTesterByUrl(url);
            }
            @Override public DomainConverter converter(String url) {
                return MultiDomainPickerConfig.Config.findDomainConverterByUrl(url);
            }
        }).getTargetUrl();
    }

    /**
     * 使当前正在使用的目标域名失效
     *      这个通常时客户端在访问接口的过程中发现了域名不可用，这时可主动使其失效
     */
    public final void invalidateDomain() {
        MultiDomainPickerConfig.throwExceptionIfConfigNotConfigured();
        DomainRepository.getInstance().invalidateTargetDomain();
    }

    /**
     * 重置域名选择器
     *      会重置已缓存域名
     *      会重置等待测试域名列表
     */
    public final void reset() {
        MultiDomainPickerConfig.throwExceptionIfConfigNotConfigured();
        DomainRepository.getInstance().reset();
    }
}
