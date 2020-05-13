package com.ulfy.android.time;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TimeJudgerItemDataRepositoryTest {

    /**
     * 初始化时间判定器的缓存配置
     */
    @Before public void initTimeJudgerCache() {
        TimeConfig.init(RuntimeEnvironment.application);
    }

    /**
     * 常规的使用测试
     */
    @Test public void testTimeJudgerData() {
        assertNull(TimeJudgerItemDataRepository.getInstance().findItemByKey(null));
        assertNull(TimeJudgerItemDataRepository.getInstance().findItemByKey(""));
        assertNotNull(TimeJudgerItemDataRepository.getInstance().findItemByKey("key"));
    }

    /**
     * 模拟在内存回收后的正常使用
     */
    @Test public void testTimeJudgerWithGc() {
        String key = "key";

        TimeJudgerItemData item = TimeJudgerItemDataRepository.getInstance().findItemByKey(key);
        assertFalse(item.isInHours(1));

        item.initTimeJudger();
        System.gc();

        item = TimeJudgerItemDataRepository.getInstance().findItemByKey(key);
        assertTrue(item.isInHours(1));
    }

}
