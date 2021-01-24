package com.ulfy.android.download_manager;


import android.app.Application;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 正在下载中任务测试
 */
@RunWith(AndroidJUnit4.class)
public class DownloadingTaskRepositoryTest extends BaseAndroidTest {
    public File downloadingDir;
    public DownloadingTaskRepository repository;

    @Before public void init() {
        DownloadManagerConfig.init((Application) InstrumentationRegistry.getContext().getApplicationContext());

        downloadingDir = DownloadManagerConfig.Config.directoryConfig.getDownloadingDirectory(InstrumentationRegistry.getContext());
        if (!downloadingDir.exists()) {
            downloadingDir.mkdirs();
        }

        repository = DownloadingTaskRepository.getInstance();
    }

    /**
     * 当没有下载记录的时候清空正在下载中的文件
     */
    @Test public void testClearDownloadFileIfNoDownloadRecord() throws Exception {
        File file = new File(downloadingDir, "demo");
        file.createNewFile();

        assertEquals(1, downloadingDir.listFiles().length);

        repository.clearDownloadingFileIfNoDownloadRecord();

        assertEquals(0, downloadingDir.listFiles().length);
    }

    /**
     * 当记录对应的文件无效后清除这条记录
     */
    @Test public void testClearInvalidDownloadRecord() {
        DownloadTask validTask = mock(DownloadTask.class);
        when(validTask.provideUniquelyIdentifies()).thenReturn("validTask");
        when(validTask.isValid()).thenReturn(true);
        DownloadTask invalidTask = mock(DownloadTask.class);
        when(invalidTask.provideUniquelyIdentifies()).thenReturn("invalidTask");
        when(invalidTask.isValid()).thenReturn(false);

        repository.addDownloadTask(validTask);
        repository.addDownloadTask(invalidTask);

        assertEquals(2, repository.getDownloadTaskCount());

        repository.clearInvalidDownloadRecord();

        assertEquals(1, repository.getDownloadTaskCount());
    }
}
