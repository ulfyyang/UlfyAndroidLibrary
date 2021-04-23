package com.ulfy.android.multi_domain_picker;

/**
 * 用于灵活的提供测试器和转换器而设计的类，用户可以根据url来提供不同的测试器和转换器
 */
interface DomainTesterConverterProvider {
    DomainTester tester(String url);
    DomainConverter converter(String url);
}
