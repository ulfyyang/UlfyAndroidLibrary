package com.ulfy.android.multi_domain_picker;

/**
 * 域名转换器实现
 *      直接将原始域名复制到目标域名
 */
public final class CopyDomainConverter implements DomainConverter {

    @Override public String convert(String originalUrl) throws Exception {
        return originalUrl;
    }

}
