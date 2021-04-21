package com.ulfy.android.multi_domain_picker;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 域名测试：ping方式测试，ping是安卓环境中的功能，所以该测试必须在安卓环境中执行
 */
public class PingDomainTesterTest {
    private PingDomainTester tester;

    @Before public void initTester() {
        tester = new PingDomainTester();
    }

    /**
     * 空的字符串不是域名，所以肯定无法访问
     */
    @Test public void testEmptyUrl() throws Exception {
        assertFalse(tester.test(null));
        assertFalse(tester.test(""));
    }

    /**
     * 测试http域名
     */
    @Test public void testHttpUrl() throws Exception {
        assertTrue(tester.test("http://www.baidu.com"));
        assertEquals("www.baidu.com", tester.getHost());
    }

    /**
     * 测试https域名
     */
    @Test public void testHttpsUrl() throws Exception {
        assertTrue(tester.test("https://www.baidu.com"));
        assertEquals("www.baidu.com", tester.getHost());
    }

    /**
     * 测试不带schema的域名
     */
    @Test public void testUrl() throws Exception {
        assertTrue(tester.test("www.baidu.com"));
        assertEquals("www.baidu.com", tester.getHost());
    }

    /**
     * 测试ip是否可以ping通
     */
    @Test public void testIp() throws Exception {
        assertTrue(tester.test("45.113.192.101"));
        assertEquals("45.113.192.101", tester.getHost());
    }

    /**
     * 测试带有路径域名
     */
    @Test public void testUrlPath() throws Exception {
        assertTrue(tester.test("www.baidu.com/"));
        assertEquals("www.baidu.com", tester.getHost());
        assertTrue(tester.test("www.baidu.com/news"));
        assertEquals("www.baidu.com", tester.getHost());
    }

    /**
     * 测试无效的域名
     */
    @Test public void testBadUrl() throws Exception {
        assertFalse(tester.test("www.fafafafafafafafafa.com"));
        assertEquals("www.fafafafafafafafafa.com", tester.getHost());
    }
}
