package com.ulfy.android.multi_domain_picker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CopyDomainConverterTest {
    public DomainConverter converter;

    @Before public void initConverter() {
        converter = new CopyDomainConverter();
    }

    @Test public void testNormalUse() throws Exception {
        Assert.assertEquals("url", converter.convert("url"));
    }

    @Test public void testEmpty() throws Exception {
        Assert.assertNull(converter.convert(null));
        Assert.assertEquals("", converter.convert(""));
    }
}
