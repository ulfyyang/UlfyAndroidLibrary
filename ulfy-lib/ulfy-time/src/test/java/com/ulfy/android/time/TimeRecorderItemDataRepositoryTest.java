package com.ulfy.android.time;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TimeRecorderItemDataRepositoryTest {

    /**
     * 初始化时间记录器的缓存配置
     */
    @Before public void initTimeRecorderCache() {
        TimeConfig.init(RuntimeEnvironment.application);
    }

    /**
     * 常规的使用测试
     */
    @Test public void testTimeRecorderData() {
        assertNull(TimeRecorderItemDataRepository.getInstance().findItemByKey(null));
        assertNull(TimeRecorderItemDataRepository.getInstance().findItemByKey(""));
        assertNotNull(TimeRecorderItemDataRepository.getInstance().findItemByKey("key"));
    }

    /**
     * 模拟在内存回收后的正常使用
     */
    @Test public void testTimeRecorderWithGc() {
        String key = "key";

        TimeRecorderItemData item = TimeRecorderItemDataRepository.getInstance().findItemByKey(key);
        item.increaseSecond(1);
        assertEquals(1, item.getRecordSecond());

        System.gc();

        item = TimeRecorderItemDataRepository.getInstance().findItemByKey(key);
        item.increaseSecond(1);

        assertEquals(2, item.getRecordSecond());
        assertTrue(item.isSecondTimeArrived(1));
    }

}