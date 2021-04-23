package com.ulfy.android.multi_domain_picker;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 域名仓库测试
 */
public class DomainRepositoryTest {
    private DomainTester baiduSuccessTester, googleSuccessTester, failTester;
    private DomainConverter baiduConverter, googleConverter;
    private NetworkDetector successDetector, failDetector;
    private String urlBaidu = "http://www.baidu.com";
    private String urlGoogle = "http://www.google.com";
    private String[] urls = new String[]{urlBaidu, urlGoogle};

    @Rule public final ExpectedException expectedException = ExpectedException.none();

    @Before public void init() throws Exception {
        baiduSuccessTester = mock(DomainTester.class);
        googleSuccessTester = mock(DomainTester.class);
        failTester = mock(DomainTester.class);
        when(baiduSuccessTester.test(anyString())).thenReturn(true);
        when(googleSuccessTester.test(anyString())).thenReturn(true);
        when(failTester.test(anyString())).thenReturn(false);

        baiduConverter = mock(DomainConverter.class);
        googleConverter = mock(DomainConverter.class);
        when(baiduConverter.convert(anyString())).thenReturn(urlBaidu);
        when(googleConverter.convert(anyString())).thenReturn(urlGoogle);

        successDetector = mock(NetworkDetector.class);
        failDetector = mock(NetworkDetector.class);
        when(successDetector.isNetworkConnected()).thenReturn(true);
        when(failDetector.isNetworkConnected()).thenReturn(false);
    }

    /**
     * 测试当等待测试的域名列表为空时自动填充
     */
    @Test public void testInflateWaitForTestDomainListIfEmpty() {
        DomainRepository repository = new DomainRepository();
        repository.initOriginalDomainList(Arrays.asList(urls));
        repository.init(null, null, null);
        assertNull(repository.waitForTestDomainList);
        repository.inflateWaitForTestDomainListIfEmpty();
        assertNotNull(repository.waitForTestDomainList);
        assertEquals(2, repository.waitForTestDomainList.size());
        assertEquals(urlBaidu, repository.waitForTestDomainList.get(0).getOriginalUrl());
        assertEquals(urlGoogle, repository.waitForTestDomainList.get(1).getOriginalUrl());
        repository.waitForTestDomainList.remove(0);
        assertEquals(1, repository.waitForTestDomainList.size());
        repository.waitForTestDomainList.remove(0);
        assertEquals(0, repository.waitForTestDomainList.size());
        repository.inflateWaitForTestDomainListIfEmpty();
        assertEquals(2, repository.waitForTestDomainList.size());
    }

    /**
     * 使用缓存策略获取有缓存的有效域名
     */
    @Test public void testGetTargetDomain() throws Exception {
        Domain domain = mock(Domain.class);
        when(domain.isValid()).thenReturn(true);
        DomainRepository repository = new DomainRepository();
        repository.targetDomain = domain;
        Domain result = repository.getTargetDomain(true);
        assertEquals(domain, result);
    }

    /**
     * 当没有有效缓存时无网络会抛出异常
     */
    @Test public void testGetTargetDomainWithoutInternet() throws Exception {
        DomainRepository repository = new DomainRepository();
        repository.init(null, null, failDetector);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("当前无网络链接");
        repository.getTargetDomain(false);
    }

    /**
     * 如果没有配置域名或配置了空的测试域名要抛出异常
     */
    @Test public void testGetTargetDomainWithoutDomainList() throws Exception {
        DomainRepository repository = new DomainRepository();
        repository.init(null, null, successDetector);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("域名列表不能为空");
        repository.getTargetDomain(false);
    }

    /**
     * 在域名中获取一个有效域名
     */
    @Test public void testGetTargetDomainNormalUser() throws Exception {
        // 第一个域名会失败，第二个域名会成功
        DomainTesterConverterProvider domainProvider = mock(DomainTesterConverterProvider.class);
        when(domainProvider.tester(urlBaidu)).thenReturn(failTester);
        when(domainProvider.tester(urlGoogle)).thenReturn(googleSuccessTester);
        DomainRepository repository = spy(new DomainRepository());
        Mockito.doNothing().when(repository).updateToCahe();
        repository.initOriginalDomainList(Arrays.asList(urls));
        repository.init(new SerialDomainFilter(), domainProvider, successDetector);
        Domain result = repository.getTargetDomain(false);
        assertNotNull(result);
        assertEquals(urlGoogle, result.getOriginalUrl());
        assertEquals(urlGoogle, result.getTargetUrl());
        assertTrue(result.isValid());
        assertNotNull(repository.waitForTestDomainList);
        assertEquals(1, repository.waitForTestDomainList.size());
        assertEquals(urlGoogle, repository.waitForTestDomainList.get(0).getOriginalUrl());
        verify(repository, times(1)).updateToCahe();
    }

    /**
     * 如果没有有效域名则抛出异常
     */
    @Test public void testGetTargetDomainWithoutValidDomain() throws Exception {
        DomainTesterConverterProvider domainProvider = mock(DomainTesterConverterProvider.class);
        when(domainProvider.tester(anyString())).thenReturn(failTester);
        DomainRepository repository = spy(new DomainRepository());
        repository.initOriginalDomainList(Arrays.asList(urls));
        repository.init(new SerialDomainFilter(), domainProvider, successDetector);
        expectedException.expect(Exception.class);
        expectedException.expectMessage("网络链接失败");
        Domain result = repository.getTargetDomain(false);
        verify(repository, times(0)).updateToCahe();
    }

    /**
     * 当无法获取可用域名时，如果有缓存域名，无论该域名是否有效都是用
     */
    @Test public void testGetTargetDomainWithInvalidDomain() throws Exception {
        DomainTesterConverterProvider domainProvider = mock(DomainTesterConverterProvider.class);
        when(domainProvider.tester(anyString())).thenReturn(failTester);
        Domain domain = mock(Domain.class);
        when(domain.isValid()).thenReturn(false);
        DomainRepository repository = spy(new DomainRepository());
        repository.targetDomain = domain;
        repository.initOriginalDomainList(Arrays.asList(urls));
        repository.init(new SerialDomainFilter(), domainProvider, successDetector);
        Domain result = repository.getTargetDomain(true);
        assertNotNull(result);
        assertEquals(domain, result);
        verify(repository, times(0)).updateToCahe();
    }
}
