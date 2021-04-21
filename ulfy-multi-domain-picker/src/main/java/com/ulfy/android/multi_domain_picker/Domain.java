package com.ulfy.android.multi_domain_picker;

import java.io.Serializable;

/**
 * 表示一个域名，其中包括一下信息：
 *      原始的域名，从原始域名转换过来的目标域名，这条域名是否可用
 */
class Domain implements Serializable {
    private static final long serialVersionUID = 1019829555372302434L;
    private String originalUrl;     // 原始域名。目标域名可能会通过原始域名获得
    private String targetUrl;       // 目标域名。最终使用的域名
    private boolean valid;          // 当前域名是否有效

    Domain(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    boolean testThenConvertDomain(DomainTester tester, DomainConverter converter) {
        try {
            boolean testSuccess = tester != null && tester.test(originalUrl);
            if (testSuccess) {
                targetUrl = converter == null ? originalUrl : converter.convert(originalUrl);
            }
            valid = testSuccess;
        } catch (Exception e) {
            valid = false;
        }
        return valid;
    }

    String getOriginalUrl() {
        return originalUrl;
    }
    String getTargetUrl() {
        return targetUrl;
    }
    void invalidate() {
        valid = false;
    }
    boolean isValid() {
        return valid;
    }
}
