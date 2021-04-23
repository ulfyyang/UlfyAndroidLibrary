package com.ulfy.android.multi_domain_picker;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class MultiDomainPickerConfigTest {
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Before public void init() {
        MultiDomainPickerConfig.Config.domainTester = new PingDomainTester();
        MultiDomainPickerConfig.Config.domainConverter = new CopyDomainConverter();
        MultiDomainPickerConfig.Config.clear();
    }

    /**
     * 如果配置了入口正常执行
     */
    @Test public void testConfiguredEntrace() {
        MultiDomainPickerConfig.configured = false;
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        MultiDomainPickerConfig.init(context, Arrays.asList("", ""));
        MultiDomainPicker.getInstance().reset();
    }

    /**
     * 如果没有配置入口则调用任何方法都会抛出异常
     */
    @Test public void testNotConfiguredEntrace() {
        MultiDomainPickerConfig.configured = false;
        exception.expect(IllegalStateException.class);
        exception.expectMessage("MultiDomainPicker not configured in Application entrance, " +
                "please add MultiDomainPickerConfig.init(this); to Application");
        MultiDomainPicker.getInstance().reset();
    }

    /**
     * 在没有配置特定域名测试器的情况下将会获取默认的测试器
     */
    @Test public void testProvideDefaultDomainTester() {
        DomainTester tester = MultiDomainPickerConfig.Config.findDomainTesterByUrl("abc");
        DomainConverter converter = MultiDomainPickerConfig.Config.findDomainConverterByUrl("abc");
        assertEquals(tester, MultiDomainPickerConfig.Config.domainTester);
        assertEquals(converter, MultiDomainPickerConfig.Config.domainConverter);
    }

    /**
     * 空的url将会获取到空的测试器和转换器
     */
    @Test public void testProvideTesterConverterWithEmptyUrl() {
        assertNull(MultiDomainPickerConfig.Config.findDomainTesterByUrl(null));
        assertNull(MultiDomainPickerConfig.Config.findDomainTesterByUrl(""));
        assertNull(MultiDomainPickerConfig.Config.findDomainConverterByUrl(null));
        assertNull(MultiDomainPickerConfig.Config.findDomainConverterByUrl(""));
    }

    /**
     * 无法为空的url配置测试器和转换器
     */
    @Test public void congifTesterConverterWithEmptyUrl() {
        MultiDomainPickerConfig.Config.configUrlTester(null, new PingDomainTester());
        MultiDomainPickerConfig.Config.configUrlTester("", new PingDomainTester());
        MultiDomainPickerConfig.Config.configUrlConverter(null, new CopyDomainConverter());
        MultiDomainPickerConfig.Config.configUrlConverter("", new CopyDomainConverter());
        assertNull(MultiDomainPickerConfig.Config.findDomainTesterByUrl(null));
        assertNull(MultiDomainPickerConfig.Config.findDomainTesterByUrl(""));
        assertNull(MultiDomainPickerConfig.Config.findDomainConverterByUrl(null));
        assertNull(MultiDomainPickerConfig.Config.findDomainConverterByUrl(""));
    }

    /**
     * 在找不到测试器的情况下抛出异常
     */
    @Test public void testExceptionWithoutTester() {
        MultiDomainPickerConfig.Config.configUrlTester("def", new PingDomainTester());
        MultiDomainPickerConfig.Config.domainTester = null;
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Cant not find DomainTester for the specific url, " +
                "you must config it for url or retain the default tester");
        MultiDomainPickerConfig.Config.findDomainTesterByUrl("abc");
    }

    /**
     * 在找不到转换器的情况下抛出异常
     */
    @Test public void testExceptionWithoutConverter() {
        MultiDomainPickerConfig.Config.configUrlConverter("def", new CopyDomainConverter());
        MultiDomainPickerConfig.Config.domainConverter = null;
        exception.expect(IllegalStateException.class);
        exception.expectMessage("Cant not find DomainConverter for the specific url, " +
                "you must config it for url or retain the default converter");
        MultiDomainPickerConfig.Config.findDomainConverterByUrl("abc");
    }

    /**
     * 为特定域名配置测试器，转换器
     */
    @Test public void testSpecificUrlTesterConverter() {
        DomainTester tester = mock(DomainTester.class);
        DomainConverter converter = mock(DomainConverter.class);
        MultiDomainPickerConfig.Config.configUrlTester("abc", tester);
        MultiDomainPickerConfig.Config.configUrlConverter("abc", converter);
        assertEquals(tester, MultiDomainPickerConfig.Config.findDomainTesterByUrl("abc"));
        assertEquals(converter, MultiDomainPickerConfig.Config.findDomainConverterByUrl("abc"));
    }
}
