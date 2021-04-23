package com.ulfy.android.multi_domain_picker;

import java.util.List;

interface DomainFilter {
    // 返回结果的值对象
    final class Result {
        public List<Domain> invalidDomainList;     // 测试过程中无效域名列表
        public Domain validDomain;                 // 有效的一个域名
    }

    /**
     * 从多个域名中过滤出一个可用的域名和不可用域名
     * @param domainList    等待过滤的域名
     * @param provider      用于提供测试器和转换器
     * @return  无效的域名列表、有效的域名（如果没有则Result中的内容返回null，但是Result不能为null）
     */
    Result filter(List<Domain> domainList, DomainTesterConverterProvider provider);
}
