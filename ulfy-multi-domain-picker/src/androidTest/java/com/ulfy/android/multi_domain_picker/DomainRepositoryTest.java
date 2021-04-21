package com.ulfy.android.multi_domain_picker;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 域名仓库测试
 */
public class DomainRepositoryTest extends BaseAndroidTest {
    private DomainTester baiduSuccessTester, googleSuccessTester, failTester;
    private DomainConverter baiduConverter, googleConverter;

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

        MultiDomainPickerConfig.init(InstrumentationRegistry.getInstrumentation().getContext(), Arrays.asList(urls));
        MultiDomainPicker.getInstance().reset();
    }

    /**
     * 测试获取域名的正常使用
     */
    @Test public void testNormalUse() throws Exception {
        Domain targetDomain = DomainRepository.getInstance()
                .getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return baiduSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return baiduConverter;
                    }
                });
        String targetUrl = targetDomain.getTargetUrl();
        assertEquals(urlBaidu, targetUrl);
    }

    /**
     * 测试在有可用的缓存域名的情况下获取域名
     */
    @Test public void testGetValidCachedTargetDomain() throws Exception {
        Domain targetDomain = DomainRepository.getInstance()
                .getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return baiduSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return baiduConverter;
                    }
                });
        String targetUrl = targetDomain.getTargetUrl();

        assertEquals(urlBaidu, targetUrl);
        verify(baiduSuccessTester, times(1)).test(urlBaidu);
        verify(baiduConverter, times(1)).convert(urlBaidu);

        Domain cacheTargetDomain = DomainRepository.getInstance()
                .getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return baiduSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return baiduConverter;
                    }
                });
        String cacheTargetUrl = cacheTargetDomain.getTargetUrl();

        assertEquals(urlBaidu, cacheTargetUrl);
        verify(baiduSuccessTester, times(1)).test(urlBaidu);
        verify(baiduConverter, times(1)).convert(urlBaidu);
    }

    /**
     * 测试不使用缓存的情况下获取域名
     */
    @Test public void testGetTargetDomainWithoutCache() throws Exception {
        Domain targetDomain = DomainRepository.getInstance()
                .getTargetDomain(false, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return baiduSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return baiduConverter;
                    }
                });
        String targetUrl = targetDomain.getTargetUrl();

        assertEquals(urlBaidu, targetUrl);
        verify(baiduSuccessTester, times(1)).test(urlBaidu);
        verify(baiduConverter, times(1)).convert(urlBaidu);

        Domain noCacheTargetDomain = DomainRepository.getInstance()
                .getTargetDomain(false, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return baiduSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return baiduConverter;
                    }
                });
        String noCacheTargetUrl = noCacheTargetDomain.getTargetUrl();

        assertEquals(urlBaidu, noCacheTargetUrl);
        verify(baiduSuccessTester, times(2)).test(urlBaidu);
        verify(baiduConverter, times(2)).convert(urlBaidu);
    }

    /**
     * 测试主动使缓存失效的情况下获取域名
     */
    @Test public void testInvalidThenGetTargetDomain() throws Exception {
        Domain targetDomain = DomainRepository.getInstance()
                .getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return baiduSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return baiduConverter;
                    }
                });
        String targetUrl = targetDomain.getTargetUrl();

        assertEquals(urlBaidu, targetUrl);
        verify(baiduSuccessTester, times(1)).test(urlBaidu);
        verify(baiduConverter, times(1)).convert(urlBaidu);

        DomainRepository.getInstance().invalidateTargetDomain();

        Domain invalidThenGetTargetDomain = DomainRepository.getInstance()
                .getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return googleSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return googleConverter;
                    }
                });
        String invalidThenGetTargetUrl = invalidThenGetTargetDomain.getTargetUrl();

        assertEquals(urlGoogle, invalidThenGetTargetUrl);
        verify(googleSuccessTester, times(1)).test(urlGoogle);
        verify(googleConverter, times(1)).convert(urlGoogle);
    }

    /**
     * 测试不使用缓存且无网络的情况下获取域名
     */
    @Test public void testGetTargetDomainWithoutInternet() throws Exception {
        DomainRepository domainRepository = spy(DomainRepository.getInstance());
        when(domainRepository.isNetworkConnected()).thenReturn(false);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("当前无网络链接");

        domainRepository.getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
            @Override public DomainTester tester(String url) {
                return googleSuccessTester;
            }
            @Override public DomainConverter converter(String url) {
                return googleConverter;
            }
        });
    }

    /**
     * 第一次获取成功后失效该域名，然后后续的域名都不可用的情况下应该继续使用这个失效的域名
     */
    @Test public void testUseInvalidCacheDomainWithoutAvailableDomain() throws Exception {
        Domain targetDomain = DomainRepository.getInstance()
                .getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return baiduSuccessTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return baiduConverter;
                    }
                });
        String targetUrl = targetDomain.getTargetUrl();

        assertEquals(urlBaidu, targetUrl);
        verify(baiduSuccessTester, times(1)).test(urlBaidu);
        verify(baiduConverter, times(1)).convert(urlBaidu);

        DomainRepository.getInstance().invalidateTargetDomain();

        Domain invalideCacheDomain = DomainRepository.getInstance()
                .getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
                    @Override public DomainTester tester(String url) {
                        return failTester;
                    }
                    @Override public DomainConverter converter(String url) {
                        return googleConverter;
                    }
                });
        String invalideCacheUrl = invalideCacheDomain.getTargetUrl();

        assertEquals(urlBaidu, invalideCacheUrl);
        verify(failTester, times(1)).test(urlGoogle);
        verify(googleConverter, times(0)).convert(urlGoogle);
    }

    /**
     * 如果找不到一个可以用的域名则会报错
     */
    @Test public void testExceptionWithoutAvailableDomain() throws Exception {
        expectedException.expect(Exception.class);
        expectedException.expectMessage("网络链接失败");
        DomainRepository.getInstance().getTargetDomain(true, new DomainRepository.DomainTesterConverterProvider() {
            @Override public DomainTester tester(String url) {
                return failTester;
            }
            @Override public DomainConverter converter(String url) {
                return baiduConverter;
            }
        });
    }
}
