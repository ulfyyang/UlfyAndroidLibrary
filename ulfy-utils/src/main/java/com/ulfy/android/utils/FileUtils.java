package com.ulfy.android.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * File工具类
 */
public final class FileUtils {

    /**
     * 读取文件为字符串
     */
    public static String read(File file) throws IOException {
        InputStream is = null;
        String text = null;
        try {
            is = new FileInputStream(file);
            text = read(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return text;
    }

    /**
     * 读取输入流为字符串,最常见的是网络请求
     */
    public static String read(InputStream is) throws IOException {
        StringBuffer strbuffer = new StringBuffer();
        String line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                strbuffer.append(line).append("\n");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return strbuffer.toString();
    }

    /**
     * 把字符串写入到文件中
     */
    public static void write(File file, String str) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(file));
            out.write(str.getBytes());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 当文件切割的时候的回调
     *      切割每部分的时候都会回调该方法
     */
    public interface OnSplitFileListener {
        /**
         * 当文件被切片后的回调
         * @param destFile  切片文件
         * @param count     总切片数
         * @param index     当前切片位置（从0开始）
         */
        void onSplitFile(File destFile, int count, int index) throws Exception;
    }

    /**
     * 分割一个文件为多个文件，函数执行完之后会返回切割好的文件数组
     *      关于设置切割完成之后删除的原因：通常切割文件只是用一次，因此可以一边切割一边在回调里进行切割后的操作，切割完成后删除就不再用了，这样可以大大的节省磁盘空间
     * @param srcFile 需要被切割的原始文件
     * @param destDir 切割文件放置的目录
     * @param deleteAfterEverySplit 每次切割完成以后是否删除掉切割文件（这个是一遍切割一边删除，硬盘占用不会太大，始终稳定）
     * @param deleteAfterAllSplit 全部切割完成以后是否删除掉切割的文件（这个是切割完成之后一次性删除，磁盘占用会不断加大，最终一次性清除）
     * @param size 每个切割文件的期望大小
     * @param onSplitFileListener 每次切割后的回调
     */
    public static List<File> splitFileBySize(File srcFile, File destDir, boolean deleteAfterEverySplit,
                                             boolean deleteAfterAllSplit, int size, OnSplitFileListener onSplitFileListener) throws Exception {
        if (srcFile == null || srcFile.length() == 0) {
            return new ArrayList<>();
        }
        if (size < 1) {
            size = 1;
        }

        int count = (int) (srcFile.length() / size);          // 总共需要切割多少块
        return splitFileByCount(srcFile, destDir, deleteAfterEverySplit, deleteAfterAllSplit, count, onSplitFileListener);
    }

    /**
     * 分割一个文件为多个文件，函数执行完之后会返回切割好的文件数组
     *      关于设置切割完成之后删除的原因：通常切割文件只是用一次，因此可以一边切割一边在回调里进行切割后的操作，切割完成后删除就不再用了，这样可以大大的节省磁盘空间
     * @param srcFile 需要被切割的原始文件
     * @param destDir 切割文件放置的目录
     * @param deleteAfterEverySplit 每次切割完成以后是否删除掉切割文件（这个是一遍切割一边删除，硬盘占用不会太大，始终稳定）
     * @param deleteAfterAllSplit 全部切割完成以后是否删除掉切割的文件（这个是切割完成之后一次性删除，磁盘占用会不断加大，最终一次性清除）
     * @param count 期望的总切割数量
     * @param onSplitFileListener 每次切割后的回调
     */
    public static List<File> splitFileByCount(File srcFile, File destDir, boolean deleteAfterEverySplit,
                                              boolean deleteAfterAllSplit, int count, OnSplitFileListener onSplitFileListener) throws Exception {
        List<File> splitList = new ArrayList<>();   // 保存切割后的文件路径

        if (srcFile == null || srcFile.length() == 0 || destDir == null) {
            return splitList;
        }
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        if (count < 1) {
            count = 1;
        }
        if (count == 1) {
            if (onSplitFileListener != null) {
                onSplitFileListener.onSplitFile(srcFile, 1, 0);
            }
            splitList.add(srcFile);
            return splitList;
        }

        long length = srcFile.length();             // 文件的总大小
        long size = length / count;                 // 每个切片的期望大小
        if (size < 1) {
            size = 1;
        }

        long offset = 0L;

        for (int i = 0; i < count - 1; i++) {       // 最后一片单独处理
            File destFile = new File(destDir, String.format("%s_%d.tmp", srcFile.getName().split("\\.")[0], i));
            offset = splitFile(srcFile, destFile, offset, (i + 1) * size);

            if (onSplitFileListener != null) {
                onSplitFileListener.onSplitFile(destFile, count, i);
            }

            if (!deleteAfterEverySplit && !deleteAfterAllSplit) {
                splitList.add(destFile);
            }
            if (deleteAfterEverySplit && destFile.exists()) {
                destFile.delete();
            }
        }

        if (length - offset > 0) {                  // 处理最后一个
            File destFile = new File(destDir, String.format("%s_%d.tmp", srcFile.getName().split("\\.")[0], count - 1));
            splitFile(srcFile, destFile, offset, length);

            if (onSplitFileListener != null) {
                onSplitFileListener.onSplitFile(destFile, count, count - 1);
            }

            if (!deleteAfterEverySplit && !deleteAfterAllSplit) {
                splitList.add(destFile);
            }
            if (deleteAfterEverySplit && destFile.exists()) {
                destFile.delete();
            }
        }

        if (deleteAfterAllSplit) {
            delete(destDir);
        }

        return splitList;
    }

    /**
     * 分割源文件的第index个切片，该方法会生成对应index下标的.tmp临时文件
     */
    private static long splitFile(File srcFile, File destFile, long begin, long end) {
        long endPointer = 0L;
        RandomAccessFile srcRandomSccessFile = null;
        RandomAccessFile destRandomAccessFile = null;

        try {
            srcRandomSccessFile = new RandomAccessFile(srcFile, "r");
            destRandomAccessFile = new RandomAccessFile(destFile, "rw");

            srcRandomSccessFile.seek(begin);

            byte[] buffer = new byte[1024];
            int length = 0;
            while (srcRandomSccessFile.getFilePointer() <= end && (length = srcRandomSccessFile.read(buffer)) != -1) {
                destRandomAccessFile.write(buffer, 0, length);
            }

            endPointer = srcRandomSccessFile.getFilePointer();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (srcRandomSccessFile != null) {
                    srcRandomSccessFile.close();
                }
                if (destRandomAccessFile != null) {
                    destRandomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return endPointer;
    }

    /**
     * 文件合并，该方法会将一个文件夹下的文件合并为一个文件
     * @param srcDir    等待被合并的文件夹
     * @param destFile  合并成功后的目标文件
     */
    public static void mergeSplitFile(File srcDir, File destFile) {
        if (srcDir == null || srcDir.listFiles().length == 0 || destFile == null) {
            return;
        }

        RandomAccessFile writer = null;
        RandomAccessFile reader = null;
        try {
            writer = new RandomAccessFile(destFile, "rw");

            // 按名称中的数字编号排序
            List<File> srcList = Arrays.asList(srcDir.listFiles());
            Collections.sort(srcList, new Comparator<File>() {
                @Override public int compare(File o1, File o2) {
                    String number1 = o1.getName().split("\\.")[0].split("_")[1];
                    String number2 = o2.getName().split("\\.")[0].split("_")[1];
                    return Integer.valueOf(number1).compareTo(Integer.valueOf(number2));
                }
            });

            for (File srcFile : srcList) {
                reader = new RandomAccessFile(srcFile, "r");

                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, length);
                }

                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解压zip文件到指定目录
     */
    public static void unzip(String zipFilePath, String destPath) throws IOException {
        File destFile = new File(destPath);
        if (!destFile.exists()) {
            destFile.mkdirs();
        }
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry zipEntry;
        String zipEntryName;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            zipEntryName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                File folder = new File(destPath + File.separator + zipEntryName);
                folder.mkdirs();
            } else {
                File file = new File(destPath + File.separator + zipEntryName);
                if (file != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = zipInputStream.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        zipInputStream.close();
    }

    /**
     * 删除文件或者文件夹，默认保留根目录
     */
    public static void delete(File directory) {
        delete(directory, true);
    }

    /**
     * 删除文件或者文件夹
     */
    public static void delete(File directory, boolean keepRoot) {
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
