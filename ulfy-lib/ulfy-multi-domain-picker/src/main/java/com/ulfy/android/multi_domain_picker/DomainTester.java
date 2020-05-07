package com.ulfy.android.multi_domain_picker;

/**
 * 域名测试器。测试原始域名是否可用
 */
public interface DomainTester {
    public boolean test(String originalUrl) throws Exception;
}
