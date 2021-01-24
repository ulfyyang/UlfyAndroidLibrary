package com.ulfy.android.download_manager;

import android.app.Application;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 下载任务测试
 */
@RunWith(AndroidJUnit4.class)
public class DownloadTaskTest extends BaseAndroidTest {
    public DownloadTask downloadTask;

    @Before public void init() {
        DownloadManagerConfig.init((Application) InstrumentationRegistry.getContext().getApplicationContext());

        DownloadTaskInfo info = mock(DownloadTaskInfo.class);
        when(info.provideUniquelyIdentifies()).thenReturn("abc");
        when(info.provideDownloadFileName()).thenReturn("abc");
        downloadTask = new DownloadTask(info);
        downloadTask = spy(downloadTask);

        doNothing().when(downloadTask).startDownloadInner();
        doNothing().when(downloadTask).stopDownloadInner();
        doNothing().when(downloadTask).initDownloadTaskIfNeedInner();
    }

    /**
     * 任务启动测试
     */
    @Test public void testStart() {
        assertFalse(downloadTask.isStart());
        assertTrue(downloadTask.start());
        verify(downloadTask, times(1)).startDownloadInner();
        assertTrue(downloadTask.isStart());
        assertFalse(downloadTask.start());
        verify(downloadTask, times(1)).startDownloadInner();
    }

    /**
     * 任务停止测试
     */
    @Test public void testStop() {
        assertFalse(downloadTask.isStart());
        assertFalse(downloadTask.stop());
        verify(downloadTask, times(0)).stopDownloadInner();
        assertTrue(downloadTask.start());
        assertTrue(downloadTask.stop());
        verify(downloadTask, times(1)).stopDownloadInner();
    }

    /**
     * 任务等待测试
     */
    @Test public void testWaiting() {
        assertFalse(downloadTask.isStart());
        assertTrue(downloadTask.waiting());
        assertTrue(downloadTask.start());
        assertFalse(downloadTask.waiting());
    }

    /**
     * 任务重启测试
     */
    @Test public void testRestart() {
        assertTrue(downloadTask.restart());
        verify(downloadTask, times(1)).startDownloadInner();
        verify(downloadTask, times(1)).stopDownloadInner();
        assertTrue(downloadTask.restart());
        verify(downloadTask, times(2)).startDownloadInner();
        verify(downloadTask, times(2)).stopDownloadInner();
    }

    /**
     * 测试任务完成后的文件转移
     */
    @Test public void testTranslateToDownloadedDirWhenComplete() throws IOException {
        downloadTask.getTargetFile().createNewFile();
        assertTrue(downloadTask.translateToDownloadedDirWhenComplete());
        assertFalse(downloadTask.translateToDownloadedDirWhenComplete());
    }
}
