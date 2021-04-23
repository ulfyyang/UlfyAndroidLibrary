package com.ulfy.android.multi_domain_picker;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SerialDomainFilterTest {
    private List<Domain> domainList = Arrays.asList(
            new Domain("aaa.com"), new Domain("bbb.com"),
            new Domain("ccc.com"), new Domain("ddd.com"),
            new Domain("eee.com")
    );
    private SerialDomainFilter filter;
    private DomainTesterConverterProvider provider;

    @Before public void initFilter() {
        filter = new SerialDomainFilter();
        provider = mock(DomainTesterConverterProvider.class);
    }

    /**
     * 空输入得到空输出
     */
    @Test public void testNulOrEmptylInputNullOutput() {
        DomainFilter.Result result = filter.filter(null, null);
        assertNotNull(result);
        assertNull(result.invalidDomainList);
        assertNull(result.validDomain);
        result = filter.filter(new ArrayList<>(), null);
        assertNotNull(result);
        assertNull(result.invalidDomainList);
        assertNull(result.validDomain);
    }

    /**
     * 第一个域名就是有效域名，则无效域名列表为空
     */
    @Test public void testFirstValidDomainFilter() {
        String validDomain = "aaa.com";
        when(provider.tester(anyString())).thenReturn(validDomain::equals);
        DomainFilter.Result result = filter.filter(domainList, provider);
        assertNull(result.invalidDomainList);
        assertNotNull(result.validDomain);
        assertTrue(result.validDomain.isValid());
        assertEquals(validDomain, result.validDomain.getOriginalUrl());
        assertEquals(validDomain, result.validDomain.getTargetUrl());
    }

    /**
     * 存在一个有效的域名
     */
    @Test public void testExistValidDomainFilter() {
        String validDomain = "ccc.com";
        when(provider.tester(anyString())).thenReturn(validDomain::equals);
        DomainFilter.Result result = filter.filter(domainList, provider);
        assertNotNull(result.invalidDomainList);
        assertEquals(2, result.invalidDomainList.size());
        assertEquals("aaa.com", result.invalidDomainList.get(0).getOriginalUrl());
        assertEquals("bbb.com", result.invalidDomainList.get(1).getOriginalUrl());
        assertNotNull(result.validDomain);
        assertTrue(result.validDomain.isValid());
        assertEquals(validDomain, result.validDomain.getOriginalUrl());
        assertEquals(validDomain, result.validDomain.getTargetUrl());
    }

    /**
     * 不存在有效的域名，则无效域名为全部域名
     */
    @Test public void testNonValidDomainFilter() {
        String validDomain = "zzz.com";
        when(provider.tester(anyString())).thenReturn(validDomain::equals);
        DomainFilter.Result result = this.filter.filter(domainList, provider);
        assertNotNull(result.invalidDomainList);
        assertEquals(5, result.invalidDomainList.size());
        assertEquals("aaa.com", result.invalidDomainList.get(0).getOriginalUrl());
        assertEquals("bbb.com", result.invalidDomainList.get(1).getOriginalUrl());
        assertEquals("ccc.com", result.invalidDomainList.get(2).getOriginalUrl());
        assertEquals("ddd.com", result.invalidDomainList.get(3).getOriginalUrl());
        assertEquals("eee.com", result.invalidDomainList.get(4).getOriginalUrl());
        assertNull(result.validDomain);
    }
}