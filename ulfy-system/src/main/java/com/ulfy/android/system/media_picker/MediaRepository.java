package com.ulfy.android.system.media_picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MediaRepository {
    public static final int SEARCH_TYPE_PICTURE = 1;    // 选择图片
    public static final int SEARCH_TYPE_VIDEO = 2;      // 选择视频
    public static final int SEARCH_TYPE_VOICE = 3;      // 选择音频

    List<MediaEntity> searchMultiMediaEntityByType(final Context context, final int type) {
        List<MediaEntity> mediaEntityList = new ArrayList<>();
        Cursor cursor = CursorColums.newInstance(type).createCursor(context);

        if (cursor != null && cursor.getCount() > 0) {
            int totalCount = cursor.getCount();
            int groupCount = 8; int groupSize = totalCount / groupCount;

            if (groupSize > 10) {               // 一共分m组，当每组大n个的时候才执行多线程操作
                List<Future<List<MediaEntity>>> futureList = new ArrayList<>();
                ExecutorService executor = Executors.newCachedThreadPool();

                for (int i = 0; i < groupCount; i++) {
                    final int start = i * groupSize;
                    final int end = start + groupSize > totalCount ? totalCount : start + groupSize;

                    Future<List<MediaEntity>> future = executor.submit(new Callable<List<MediaEntity>>() {
                        @Override public List<MediaEntity> call() throws Exception {
                            return queryMediaEntity(context, type, start, end);
                        }
                    });
                    futureList.add(future);
                }

                for (Future<List<MediaEntity>> future : futureList) {
                    try {
                        mediaEntityList.addAll(future.get());
                    } catch (Exception e) { e.printStackTrace(); }
                }
            } else {
                mediaEntityList.addAll(queryMediaEntity(context, type, 0, totalCount));
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return mediaEntityList;
    }

    private List<MediaEntity> queryMediaEntity(Context context, int type, int start, int end) {
        Cursor cursor = CursorColums.newInstance(type).createCursor(context);
        MediaEntityColumns columns = MediaEntityColumns.newInstance(type, cursor);

        List<MediaEntity> mediaEntityList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            cursor.moveToPosition(i);
            MediaEntity entity = columns.createMediaEntity(cursor);
            if (entity != null && entity.exists()) {
                mediaEntityList.add(entity);
            }
        }

        cursor.close();
        return mediaEntityList;
    }

    /**
     * 获取查询游标的字段
     */
    static class CursorColums {
        private Uri searchUri = null;
        private String searchData = null;

        static CursorColums newInstance(int type) {
            CursorColums columns = new CursorColums();
            switch (type) {
                case SEARCH_TYPE_PICTURE:
                    columns.searchUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    columns.searchData = MediaStore.Images.Media.DATE_ADDED + " DESC";
                    break;
                case SEARCH_TYPE_VIDEO:
                    columns.searchUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    columns.searchData = MediaStore.Video.Media.DATE_ADDED + " DESC";
                    break;
                case SEARCH_TYPE_VOICE:
                    columns.searchUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    columns.searchData = MediaStore.Audio.Media.DATE_ADDED + " DESC";
                    break;
            }
            return columns;
        }

        Cursor createCursor(Context context) {
            return context.getContentResolver().query(
                    searchUri, null, null, null, searchData);
        }
    }

    /**
     * 获取具体实体的字段
     */
    static class MediaEntityColumns {
        private int searchIdIndex = 0;
        private int searchTitleIndex = 0;
        private int searchPathIndex = 0;
        private int searchDurationIndex = 0;       // 图片没有时长字段
        private int searchSizeIndex = 0;
        private int type = 0;

        // 读取 index 比较耗时，所以要提前读出来，这样查询的时候速度会很快
        static MediaEntityColumns newInstance(int type, Cursor cursor) {
            MediaEntityColumns columns = new MediaEntityColumns();
            columns.type = type;
            switch (type) {
                case SEARCH_TYPE_PICTURE:
                    columns.searchIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    columns.searchTitleIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                    columns.searchPathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    columns.searchSizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    break;
                case SEARCH_TYPE_VIDEO:
                    columns.searchIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    columns.searchTitleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                    columns.searchPathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    columns.searchDurationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    columns.searchSizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    break;
                case SEARCH_TYPE_VOICE:
                    columns.searchIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    columns.searchTitleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    columns.searchPathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    columns.searchDurationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                    columns.searchSizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE);
                    break;
            }
            return columns;
        }

        MediaEntity createMediaEntity(Cursor cursor) {
            MediaEntity entity = null;
            switch (type) {
                case SEARCH_TYPE_PICTURE:
                    entity = new PictureEntity(
                            cursor.getInt(searchIdIndex), cursor.getString(searchTitleIndex),
                            cursor.getString(searchPathIndex), cursor.getLong(searchSizeIndex)
                    );
                    break;
                case SEARCH_TYPE_VIDEO:
                    entity = new VideoEntity(
                            cursor.getInt(searchIdIndex), cursor.getString(searchTitleIndex),
                            cursor.getString(searchPathIndex), cursor.getLong(searchSizeIndex),
                            cursor.getLong(searchDurationIndex)
                    );
                    break;
                case SEARCH_TYPE_VOICE:
                    entity = new VoiceEntity(
                            cursor.getInt(searchIdIndex), cursor.getString(searchTitleIndex),
                            cursor.getString(searchPathIndex), cursor.getLong(searchSizeIndex),
                            cursor.getLong(searchDurationIndex)
                    );
                    break;
            }
            return entity;
        }
    }

}
