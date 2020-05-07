package com.ulfy.android.multi_domain_picker;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 域名类测试
 */
public class DomainTest {
    public DomainTester successTester;
    public DomainTester failTester;
    public DomainTester exceptionTester;
    public DomainConverter successConverter;
    public DomainConverter exceptionConverter;
    public String url = "http://www.demo.com";

    public Domain domain;

    @Before public void initTesterAndConverter() throws Exception {
        successTester = mock(DomainTester.class);
        failTester = mock(DomainTester.class);
        exceptionTester = mock(DomainTester.class);

        when(successTester.test(url)).thenReturn(true);
        when(failTester.test(url)).thenReturn(false);
        when(exceptionTester.test(url)).thenThrow(new IllegalStateException());

        successConverter = mock(DomainConverter.class);
        exceptionConverter = mock(DomainConverter.class);

        when(successConverter.convert(url)).thenReturn(url);
        when(exceptionConverter.convert(url)).thenThrow(new IllegalStateException());

        domain = new Domain(url);
    }

    /**
     * 测试正常使用
     */
    @Test public void testNormalUse() {
        boolean valid = domain.testThenConvertDomain(successTester, successConverter);
        assertTrue(valid);
    }

    /**
     * 失败测试：空测试器，转换器为空
     */
    @Test public void testNullTesterNullConverter() {
        boolean valid = domain.testThenConvertDomain(null, null);
        assertFalse(valid);
    }

    /**
     * 失败测试：测试器失败，转换器为空
     */
    @Test public void testFailTesterNullConverter() {
        boolean valid = domain.testThenConvertDomain(failTester, null);
        assertFalse(valid);
    }

    /**
     * 失败测试：测试器异常，转换器为空
     */
    @Test public void testExceptionTesterNullConverter() {
        boolean valid = domain.testThenConvertDomain(exceptionTester, null);
        assertFalse(valid);
    }

    /**
     * 成功测试：测试正常，转换器为空
     */
    @Test public void testSuccessTesterNullConverter() {
        boolean valid = domain.testThenConvertDomain(successTester, null);
        assertTrue(valid);
        assertEquals(url, domain.getOriginalUrl());
        assertEquals(url, domain.getTargetUrl());
    }

    /**
     * 失败测试：测试正常，转换器异常
     */
    @Test public void testSuccessTesterExceptionConverter() {
        boolean valid = domain.testThenConvertDomain(successTester, exceptionConverter);
        assertFalse(valid);
    }
}
