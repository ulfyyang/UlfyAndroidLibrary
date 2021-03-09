package com.ulfy.android.system.media_picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.ulfy.android.system.SystemConfig;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/*
核心算法：
    1. 准备一个虚拟的List负责对接外部的数据获取
    2. 首次准备30条数据，之后的数据都是空的
    3. 当滑动到剩余10条以后的时候开始加载第二个30条
    4. 由于不存在真实数据，因此对于目标Entity的访问要采用延迟访问的策略（需要对Entity做一层包装）
 */
public final class MediaRepository {
    public static final int SEARCH_TYPE_PICTURE = 1;    // 选择图片
    public static final int SEARCH_TYPE_VIDEO = 2;      // 选择视频
    public static final int SEARCH_TYPE_VOICE = 3;      // 选择音频
    private static MediaRepository instance = new MediaRepository();
    private List<MediaEntity> mediaEntityList;          // 存储真实数据的列表
    private FutureList futureEntityList;                // 模拟的未来数据列表

    /**
     * 私有化构造方法，提供单例，便于全局访问
     */
    private MediaRepository () { }

    /**
     * 获取对象单例，后续的使用必须先执行init方法
     */
    public static MediaRepository getInstance() {
        return instance;
    }

    /**
     * 对象需要先初始化之后才能使用
     * 改方法对所有数据进行重置
     */
    public void init(int type) {
        Cursor cursor = CursorColums.newInstance(type).createCursor(SystemConfig.context);
        mediaEntityList = new ArrayList<>();
        futureEntityList = new FutureList(SystemConfig.context, type, cursor.getCount(), mediaEntityList);
        futureEntityList.loadNextPageMediaEntity();
    }

    /**
     * 获取虚拟的总大小
     */
    public int size() {
        return futureEntityList.size();
    }

    /**
     * 根据索引获取实体，如果索引位置没有数据则返回null。该方法不会执行加载下一页。
     */
    public MediaEntity getReal(int index) {
        return index < mediaEntityList.size() ? mediaEntityList.get(index) : null;
    }

    /**
     * 根据索引获取实体，如果索引到达真实数据的尾部区域会自动加载下一页的数据
     */
    public MediaEntity get(int index) {
        return futureEntityList.get(index);
    }

    /**
     * 未来数据列表，只有当需要的时候才会延迟加载
     */
    private static class FutureList extends AbstractList<MediaEntity> {
        private Context context;
        private int type, size;
        private List<MediaEntity> mediaEntityList;

        private FutureList(Context context, int type, int size, List<MediaEntity> mediaEntityList) {
            this.context = context;
            this.type = type;
            this.size = size;
            this.mediaEntityList = mediaEntityList;
        }

        private void loadNextPageMediaEntity() {
            int start = mediaEntityList.size();
            int end = mediaEntityList.size() + 30;      // 每页数据不要太大，否则加载会占用太多时间，造成滑动过程中页面卡顿
            if (end > size) {
                end = size;
            }
            mediaEntityList.addAll(queryMediaEntity(context, type, start, end));
        }

        @Override public MediaEntity get(int index) {
            if (mediaEntityList.size() != size && index > mediaEntityList.size() - 10) {
                loadNextPageMediaEntity();
            }
            return index < mediaEntityList.size() ? mediaEntityList.get(index) : null;
        }

        @Override public int size() {
            return size;
        }
    }

    /**
     * 根据给定的参数查询集合中指定范围的数据列表
     * @param start     开始位置（包含）
     * @param end       结束位置（不包含）
     */
    private static List<MediaEntity> queryMediaEntity(Context context, int type, int start, int end) {
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
    private static class CursorColums {
        private Uri searchUri = null;
        private String searchData = null;

        private static CursorColums newInstance(int type) {
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

        private Cursor createCursor(Context context) {
            return context.getContentResolver().query(
                    searchUri, null, null, null, searchData);
        }
    }

    /**
     * 获取具体实体的字段
     */
    private static class MediaEntityColumns {
        private int searchIdIndex = 0;
        private int searchTitleIndex = 0;
        private int searchPathIndex = 0;
        private int searchDurationIndex = 0;       // 图片没有时长字段
        private int searchSizeIndex = 0;
        private int type = 0;

        // 读取 index 比较耗时，所以要提前读出来，这样查询的时候速度会很快
        private static MediaEntityColumns newInstance(int type, Cursor cursor) {
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

        private MediaEntity createMediaEntity(Cursor cursor) {
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
