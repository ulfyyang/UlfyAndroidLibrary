package com.ulfy.android.download_manager;

/**
 * 下载任务需要的信息，用于对接外部业务实体
 */
public interface DownloadTaskInfo {
    /**
     * 提供唯一标识，通常情况下可以使用下载连接代替
     */
    public String getUniquelyIdentifies();
    /**
     * 提供下载文件连接
     */
    public String provideDownloadFileLink();
    /**
     * 提供下载文件映射名（保存文件的名字）
     */
    public String provideDownloadFileName();
}
