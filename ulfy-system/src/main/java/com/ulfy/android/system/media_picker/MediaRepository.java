package com.ulfy.android.system.media_picker;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public final class MediaRepository {
    public static final int SEARCH_TYPE_PICTURE = 1;    // 选择图片
    public static final int SEARCH_TYPE_VIDEO = 2;      // 选择视频
    public static final int SEARCH_TYPE_VOICE = 3;      // 选择音频

    public static List<MediaEntity> searchMultiMediaEntityByType(Context context, int type) {

        List<MediaEntity> mediaEntityList = new ArrayList<>();// 多媒体容器

        ContentResolver contentResolver = context.getContentResolver();

        // 获取 cursor 用的参数
        Uri searchUri = null;
        String searchData = null;

        // 获得具体媒体数据用的参数，其中图片没有长度
        String searchId = null;
        String searchTitle = null;
        String searchPath = null;
        String searchDuration = null;
        String searchSize = null;

        switch (type) {
            case SEARCH_TYPE_PICTURE:
                searchUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                searchData = MediaStore.Images.Media.DATE_ADDED;
                searchId = MediaStore.Images.Media._ID;
                searchTitle = MediaStore.Images.Media.TITLE;
                searchPath = MediaStore.Images.Media.DATA;
                searchSize = MediaStore.Images.Media.SIZE;
                break;
            case SEARCH_TYPE_VIDEO:
                searchUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                searchData = MediaStore.Video.Media.DATE_ADDED;
                searchId = MediaStore.Video.Media._ID;
                searchTitle = MediaStore.Video.Media.TITLE;
                searchPath = MediaStore.Video.Media.DATA;
                searchDuration = MediaStore.Video.Media.DURATION;
                searchSize = MediaStore.Video.Media.SIZE;
                break;
            case SEARCH_TYPE_VOICE:
                searchUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                searchData = MediaStore.Audio.Media.DATE_ADDED;
                searchId = MediaStore.Audio.Media._ID;
                searchTitle = MediaStore.Audio.Media.TITLE;
                searchPath = MediaStore.Audio.Media.DATA;
                searchDuration = MediaStore.Audio.Media.DURATION;
                searchSize = MediaStore.Audio.Media.SIZE;
                break;
        }

        Cursor cursor = contentResolver.query(searchUri, null, null, null, searchData);

        // 应该在第一个位置插入，否则最新的会在最后一个
        if (cursor != null && cursor.moveToFirst()) {
            do {
                MediaEntity entity = null;
                switch (type) {
                    case SEARCH_TYPE_PICTURE:
                        entity = new PictureEntity(
                                cursor.getInt(cursor.getColumnIndexOrThrow(searchId)),
                                cursor.getString(cursor.getColumnIndexOrThrow(searchTitle)),
                                cursor.getString(cursor.getColumnIndexOrThrow(searchPath)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(searchSize))
                        );
                        break;
                    case SEARCH_TYPE_VIDEO:
                        entity = new VideoEntity(
                                cursor.getInt(cursor.getColumnIndexOrThrow(searchId)),
                                cursor.getString(cursor.getColumnIndexOrThrow(searchTitle)),
                                cursor.getString(cursor.getColumnIndexOrThrow(searchPath)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(searchSize)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(searchDuration))
                        );
                        break;
                    case SEARCH_TYPE_VOICE:
                        entity = new VoiceEntity(
                                cursor.getInt(cursor.getColumnIndexOrThrow(searchId)),
                                cursor.getString(cursor.getColumnIndexOrThrow(searchTitle)),
                                cursor.getString(cursor.getColumnIndexOrThrow(searchPath)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(searchSize)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(searchDuration))
                        );
                        break;
                }
                if (entity == null || !entity.exists()) {
                    continue;
                }
                mediaEntityList.add(0, entity);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return mediaEntityList;
    }
}
