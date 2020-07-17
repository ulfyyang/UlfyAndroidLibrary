package com.ulfy.android.multi_domain_picker;

/**
 * 域名转换器。将原始域名转换为目标域名
 */
public interface DomainConverter {
    public String convert(String originalUrl) throws Exception;
}