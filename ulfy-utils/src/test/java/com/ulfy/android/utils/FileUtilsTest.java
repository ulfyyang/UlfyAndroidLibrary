package com.ulfy.android.utils;

import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileUtilsTest {
    public int splitCount = 20, splitSize = 1024 * 1024;

    /**
     * 测试获取文件MD5
     */
    @Test public void testGetMD5() throws Exception {
        File file = new File(new File(".").getCanonicalPath(), "src/test/app-release.apk");
        String md5 = FileUtils.getMD5(file);
        System.out.println(md5);
    }

    /**
     * 测试获取文件扩展名
     */
    @Test public void testGetExtension() throws Exception {
        File file = new File(new File(".").getCanonicalPath(), "src/test/app-release.apk");
        String extension = FileUtils.getExtension(file.getCanonicalPath());
        System.out.println(extension);
    }

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

    /*
    文件下载测试为手工测试，需要肉眼观测控制台输出结果
     */

    private String urlNormal = "https://media.w3.org/2010/05/sintel/trailer.mp4";     // 正常地址
    private String urlWrong = "https://media.w3.org/2010/05/sintel/none.mp4";         // 错误地址
    private FileUtils.OnDownloadListener onDownloadListener = new FileUtils.OnDownloadListener() {
        @Override public void started() {
            System.out.println("回调了：started --> 任务启动了");
        }
        @Override public void progress(long currentOffset, long totalLength) {
            System.out.println(String.format("回调了：progress --> 总长度：%d，当前进度：%d", totalLength, currentOffset));
        }
        @Override public void success(File file) {
            System.out.println(String.format("回调了：success --> 下载成功了，文件位置：%s", file.getAbsoluteFile()));
        }
        @Override public void error(Exception e) {
            System.out.println(String.format("回调了：error --> 下载失败：%s", e.getMessage()));
            e.printStackTrace();
        }
    };
    private File target = new File("/Users/a123/Downloads/outputs/test.mp4");

    /**
     * 测试文件下载。观察控制台输出：任务启动 --> 进度从0开始到总长度 --> 文件下载成功并打印文件位置
     * --> 检查指定位置是否存在文件（目录自动创建）
     */
    @Test public void testDownload() throws Exception {
        FileUtils.download(urlNormal, target, onDownloadListener);
    }

    /**
     * 测试文件下载。观察控制台：任务启动 --> 打印下载失败回调输出 --> 打印错误异常日志
     */
    @Test public void testDownloadWithExceptionAndDownloadListener() throws Exception {
        FileUtils.download(urlWrong, target, onDownloadListener);
    }

    /**
     * 测试文件下载。观察控制台：直接打印错误异常日志
     */
    @Test public void testDownloadWithExceptionWithoutDownloadListener() throws Exception {
        FileUtils.download(urlWrong, target, null);
    }
}