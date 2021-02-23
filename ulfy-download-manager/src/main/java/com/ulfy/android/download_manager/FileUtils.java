package com.ulfy.android.download_manager;

import java.io.File;

/**
 * File工具类
 */
class FileUtils {

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
