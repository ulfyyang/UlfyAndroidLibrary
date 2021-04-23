package com.ulfy.android.multi_domain_picker;

import java.util.ArrayList;
import java.util.List;

class SerialDomainFilter implements DomainFilter {

    // 该类为按照顺序对域名进行测试的类，因此无效的域名只保存有效域名之前的无效域名
    @Override public Result filter(List<Domain> domainList, DomainTesterConverterProvider provider) {
        Result result = new Result();
        if (domainList == null || domainList.isEmpty()) {
            return result;
        }
        for (Domain domain : domainList) {
            DomainTester tester = provider.tester(domain.getOriginalUrl());
            DomainConverter converter = provider.converter(domain.getOriginalUrl());
            if (domain.testThenConvertDomain(tester, converter)) {
                result.validDomain = domain;
                break;
            } else {
                if (result.invalidDomainList == null) {
                    result.invalidDomainList = new ArrayList<>();
                }
                result.invalidDomainList.add(domain);
            }
        }
        return result;
    }

}
