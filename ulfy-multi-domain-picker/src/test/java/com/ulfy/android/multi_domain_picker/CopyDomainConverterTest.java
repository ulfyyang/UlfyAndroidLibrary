package com.ulfy.android.multi_domain_picker;

import org.junit.Assert;
import org.junit.Test;

public class CopyDomainConverterTest {

    /**
     * 转换器的输入和输出相同
     */
    @Test public void testNormalUse() throws Exception {
        Assert.assertEquals("url", new CopyDomainConverter().convert("url"));
    }

}
