package com.ulfy.android.multi_domain_picker;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 域名测试：ping方式测试
 */
@RunWith(AndroidJUnit4.class)
public class PingDomainTesterTest extends BaseAndroidTest {
    public DomainTester tester;

    @Before public void initTester() {
        tester = new PingDomainTester();
    }

    @Test public void testEmptyUrl() throws Exception {
        assertFalse(tester.test(null));
        assertFalse(tester.test(""));
    }

    @Test public void testHttpUrl() throws Exception {
        String url = "http://www.baidu.com";
        boolean valid = tester.test(url);
        assertTrue(valid);
    }

    @Test public void testHttpsUrl() throws Exception {
        String url = "https://www.baidu.com";
        boolean valid = tester.test(url);
        assertTrue(valid);
    }

    @Test public void testUrl() throws Exception {
        String url = "www.baidu.com";
        boolean valid = tester.test(url);
        assertTrue(valid);
    }

    @Test public void testIp() throws Exception {
        String url = "45.113.192.101";
        boolean valid = tester.test(url);
        assertTrue(valid);
    }

    @Test public void testUrlPath1() throws Exception {
        String url = "www.baidu.com/";
        boolean valid = tester.test(url);
        assertTrue(valid);
    }

    @Test public void testUrlPath2() throws Exception {
        String url = "www.baidu.com/news";
        boolean valid = tester.test(url);
        assertTrue(valid);
    }

    @Test public void testBadUrl() throws Exception {
        String url = "www.fafafafafafafafafa.com";
        boolean valid = tester.test(url);
        assertFalse(valid);
    }
}
