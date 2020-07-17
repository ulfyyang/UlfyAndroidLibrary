package com.ulfy.android.cache;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static org.junit.Assert.*;

/**
 * 硬盘缓存测试
 */
@RunWith(RobolectricTestRunner.class)
public class DiskCacheTest {
    private ICache cache;

    @Before public void initCache() {
        cache = new DiskCache(RuntimeEnvironment.application, "local_entity");
    }

    @Test public void testDiskCacheDirStatus() {
        File cacheDir = new File(RuntimeEnvironment.application.getFilesDir(), "local_entity");
        assertTrue(cacheDir.exists());
    }

    @Test public void testIsCached() {
        assertFalse(cache.isCached(TestEntity1.class));

        TestEntity1 testEntity1 = new TestEntity1();
        cache.cache(testEntity1);

        assertTrue(cache.isCached(TestEntity1.class));
    }

    @Test public void testCache() {
        assertFalse(cache.isCached(TestEntity1.class));

        TestEntity1 originalEntity = new TestEntity1();
        originalEntity.attr1 = 1;
        originalEntity.attr2 = "1";

        cache.cache(originalEntity);

        TestEntity1 cacheEntity = cache.getCache(TestEntity1.class);

        assertEquals(originalEntity.attr1, cacheEntity.attr1);
        assertEquals(originalEntity.attr2, cacheEntity.attr2);
    }


    @Test public void testDeleteCache() {
        assertFalse(cache.isCached(TestEntity1.class));

        TestEntity1 testEntity1 = new TestEntity1();
        cache.cache(testEntity1);

        assertTrue(cache.isCached(TestEntity1.class));

        cache.deleteCache(TestEntity1.class);

        assertFalse(cache.isCached(TestEntity1.class));
    }

    @Test public void testDeleteAllCacle() {
        assertFalse(cache.isCached(TestEntity1.class));
        assertFalse(cache.isCached(TestEntity2.class));

        TestEntity1 testEntity1 = new TestEntity1();
        cache.cache(testEntity1);
        TestEntity2 testEntity2 = new TestEntity2();
        cache.cache(testEntity2);

        assertTrue(cache.isCached(TestEntity1.class));
        assertTrue(cache.isCached(TestEntity2.class));

        cache.deleteAllCache();

        assertFalse(cache.isCached(TestEntity1.class));
        assertFalse(cache.isCached(TestEntity2.class));
    }

}
