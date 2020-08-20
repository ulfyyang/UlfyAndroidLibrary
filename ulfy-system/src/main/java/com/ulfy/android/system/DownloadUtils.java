package com.ulfy.android.system;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

class DownloadUtils {
    private static Handler uiHandler = new Handler(Looper.getMainLooper());

    /**
     * 下载监听
     */
    interface OnDownloadListener {
        void started();
        void progress(long currentOffset, long totalLength);
        void success(File file);
        void error(Exception e);
    }

    /**
     * @param url 下载连接
     * @param file 文件的下载位置
     * @param onDownloadListener 下载监听
     */
    static void download(final String url, final File file, final OnDownloadListener onDownloadListener) {
        callbackStarted(onDownloadListener);

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override public void onResponse(Call call, Response response) throws IOException {
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;

                try {
                    inputStream = response.body().byteStream();
                    fileOutputStream = new FileOutputStream(file);
                    long totalLength = response.body().contentLength(), currentOffset = 0;

                    callbackProgress(onDownloadListener, currentOffset, totalLength);

                    int length = 0; byte[] buffer = new byte[2048];
                    while ((length = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, length);
                        currentOffset += length;

                        callbackProgress(onDownloadListener, currentOffset, totalLength);
                    }

                    fileOutputStream.flush();
                    callbackSuccess(onDownloadListener, file);
                } catch (Exception e) {
                    e.printStackTrace();
                    callbackError(onDownloadListener, e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }

            @Override public void onFailure(Call call, IOException e) {
                callbackError(onDownloadListener, e);
            }
        });
    }

    private static void callbackStarted(final OnDownloadListener onDownloadListener) {
        uiHandler.post(new Runnable() {
            @Override public void run() {
                onDownloadListener.started();
            }
        });
    }

    private static void callbackProgress(final OnDownloadListener onDownloadListener, final long currentOffset, final long totalLength) {
        uiHandler.post(new Runnable() {
            @Override public void run() {
                onDownloadListener.progress(currentOffset, totalLength);
            }
        });
    }

    private static void callbackSuccess(final OnDownloadListener onDownloadListener, final File file) {
        uiHandler.post(new Runnable() {
            @Override public void run() {
                onDownloadListener.success(file);
            }
        });
    }

    private static void callbackError(final OnDownloadListener onDownloadListener, final Exception e) {
        uiHandler.post(new Runnable() {
            @Override public void run() {
                onDownloadListener.error(e);
            }
        });
    }
}