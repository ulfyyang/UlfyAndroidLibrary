package com.ulfy.android.download_manager;

/**
 * 下载任务需要的信息，用于对接外部业务实体
 */
public interface DownloadTaskInfo {
    /**
     * 提供唯一表示，通常情况下可以使用下载连接代替
     */
    public String provideUniquelyIdentifies();
    /**
     * 提供下载文件连接
     */
    public String provideDownloadFileLink();
    /**
     * 提供下载文件映射名
     */
    public String provideDownloadFileName();
}
