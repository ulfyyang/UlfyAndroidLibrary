package com.ulfy.android.download_manager;

import android.app.Application;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 已下载任务仓库测试
 */
@RunWith(AndroidJUnit4.class)
public class DownloadedTaskRepositoryTest extends BaseAndroidTest {
    public File downloadedDir;
    public DownloadedTaskRepository repository;

    @Before public void init() {
        DownloadManagerConfig.init((Application) InstrumentationRegistry.getContext().getApplicationContext());

        downloadedDir = DownloadManagerConfig.Config.directoryConfig.getDownloadedDirectory(InstrumentationRegistry.getContext());
        if (!downloadedDir.exists()) {
            downloadedDir.mkdirs();
        }

        repository = DownloadedTaskRepository.getInstance();
    }

    /**
     * 当没有下载记录的时候清空已经下载完成的文件
     */
    @Test public void testClearDownloadFileIfNoDownloadRecord() throws IOException {
        File file = new File(downloadedDir, "demo");
        file.createNewFile();

        assertEquals(1, downloadedDir.listFiles().length);

        repository.clearDownloadedFileIfNoDownloadRecord();

        assertEquals(0, downloadedDir.listFiles().length);
    }

    /**
     * 当记录对应的文件无效后要清除这条记录
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
