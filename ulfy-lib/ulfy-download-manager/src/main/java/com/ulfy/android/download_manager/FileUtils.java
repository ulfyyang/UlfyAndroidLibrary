package com.ulfy.android.download_manager;

import java.io.File;

/**
 * File工具类
 */
class FileUtils {
    /**
     * 删除文件或者文件夹，默认保留根目录
     */
    static void delete(File directory) {
        delete(directory, true);
    }

    /**
     * 删除文件或者文件夹
     */
    static void delete(File directory, boolean keepRoot) {
        if (directory != null && directory.exists()) {
            if (directory.isDirectory()) {
                for (File subDirectory : directory.listFiles()) {
                    delete(subDirectory, false);
                }
            }
            if (!keepRoot) {
                directory.delete();
            }
        }
    }
}
