package com.ulfy.android.utils;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileUtilsTest {
    public int splitCount = 20, splitSize = 1024 * 1024;

    /**
     * 常规使用测试
     */
    @Test public void testSplitFile() throws Exception {
        File splitDir = new File(new File(".").getCanonicalPath(), "src/test/split");
        File splitFile = new File(new File(".").getCanonicalPath(), "src/test/test.mp4");

        List<File> splitList = FileUtils.splitFileByCount(splitFile, splitDir, false,
                false, splitCount, null);

        assertEquals(splitCount, splitList.size());
    }

    /**
     * 测试当文件切割完成后删除文件
     *      该测试建议以debug的方式查看文件的删除过程
     *      观察文件的变化要使用系统自带的文件浏览器，idea反应比较慢
     */
    @Test public void testSplitFileDelete1() throws Exception {
        File splitDir = new File(new File(".").getCanonicalPath(), "src/test/split_delete");
        File splitFile = new File(new File(".").getCanonicalPath(), "src/test/test.mp4");

        List<File> splitList = FileUtils.splitFileByCount(splitFile, splitDir, true,
                false, splitCount, null);

        assertEquals(0, splitList.size());
    }

    /**
     * 测试当文件切割完成后删除文件
     *      该测试建议以debug的方式查看文件的删除过程
     *      观察文件的变化要使用系统自带的文件浏览器，idea反应比较慢
     */
    @Test public void testSplitFileDelete2() throws Exception {
        File splitDir = new File(new File(".").getCanonicalPath(), "src/test/split_delete");
        File splitFile = new File(new File(".").getCanonicalPath(), "src/test/test.mp4");

        List<File> splitList = FileUtils.splitFileByCount(splitFile, splitDir, false,
                true, splitCount, null);

        assertEquals(0, splitList.size());
    }

    /**
     * 测试切片回调
     */
    @Test public void testSplitFileCallback() throws Exception {
        File splitDir = new File(new File(".").getCanonicalPath(), "src/test/split");
        File splitFile = new File(new File(".").getCanonicalPath(), "src/test/test.mp4");

        List<File> splitList = FileUtils.splitFileByCount(splitFile, splitDir, false,
                false, splitCount, new FileUtils.OnSplitFileListener() {
            @Override public void onSplitFile(File destFile, int count, int index) {
                assertTrue(destFile.exists());
            }
        });

        assertEquals(splitCount, splitList.size());
    }

    /**
     * 测试根据切片大小切割
     */
    @Test public void testSplitFileBySize() throws Exception {
        File splitDir = new File(new File(".").getCanonicalPath(), "src/test/split_size");
        File splitFile = new File(new File(".").getCanonicalPath(), "src/test/test.mp4");

        List<File> splitList = FileUtils.splitFileBySize(splitFile, splitDir, false,
                false, splitSize, null);

        assertEquals(splitFile.length() / splitSize, splitList.size());
    }

    /**
     * 测试文件合并
     */
    @Test public void testMergeSplitFile() throws Exception {
        File srcDir = new File(new File(".").getCanonicalPath(), "src/test/split");
        File destFile = new File(new File(".").getCanonicalPath(), "src/test/test_dest.mp4");

        FileUtils.mergeSplitFile(srcDir, destFile);

        assertTrue(destFile.exists());
    }
}