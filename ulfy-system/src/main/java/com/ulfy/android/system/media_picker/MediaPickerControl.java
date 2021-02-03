package com.ulfy.android.system.media_picker;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class MediaPickerControl {
    private MediaRepository mediaRepository;
    private List<MediaPickWrapper> mediaPickWrapperList;  // 操作的容器
    private int maxSelectCount = 0;     // 可选的最大数量，0 表示无限制
    private int searchType;             // 搜索类型
    private List<MediaEntity> mMediaEntityList = new ArrayList<>();

    MediaPickerControl(int maxSelectCount, int searchType) {
        mediaRepository = MediaRepository.getInstance();
        this.mediaPickWrapperList = new LinkedList<>();
        this.maxSelectCount = maxSelectCount < 0 ? 0 : maxSelectCount;
        this.searchType = searchType;
    }

    List<MediaPickWrapper> searchMultiMediaPickWrappers(Context context, List<MediaEntity> selectEntities) {
        mediaPickWrapperList.clear();
        mediaPickWrapperList.addAll(searchMultiMediaPickWrappersInside(context, selectEntities));
        return mediaPickWrapperList;
    }

    List<MediaPickWrapper> reloadMultiMediaPickWrappers(Context context) {
        List<MediaPickWrapper> mediaPickWrappers = searchMultiMediaPickWrappersInside(context, getSelectMultiMediaList());
        mediaPickWrapperList.clear();
        mediaPickWrapperList.addAll(mediaPickWrappers);
        return mediaPickWrapperList;
    }

    private List<MediaPickWrapper> searchMultiMediaPickWrappersInside(Context context, List<MediaEntity> selectEntities) {
        List<MediaPickWrapper> mediaPickWrapperList = new LinkedList<>();
        List<MediaEntity> multiMediaEntities = MediaRepository.getInstance().init(context, searchType);
        for (int i = 0; i < multiMediaEntities.size(); i++) {
            boolean isEquals = false;
            if (selectEntities != null && selectEntities.size() >= 0) {
                for (MediaEntity haveEntity : selectEntities) {
                    if (haveEntity.isSame(i)) {
                        isEquals = true;
                        break;
                    }
                }
            }
            mediaPickWrapperList.add(new MediaPickWrapper(multiMediaEntities, i, isEquals));
        }
        return mediaPickWrapperList;
    }

    void selectOrCancelMultiMedia(int index) throws OverMaxSelectMediaCountException {
        if (isSelect(index)) {
            cancelSelectMultiMedia(index);
        } else {
            selectMultiMedia(index);
        }
    }

    void selectMultiMedia(int index) throws OverMaxSelectMediaCountException {
        if (canPickMultiMedia()) {
            mediaPickWrapperList.get(index).setSelect(true);
            mMediaEntityList.add(mediaPickWrapperList.get(index).getMediaEntity());
        } else {
            throw new OverMaxSelectMediaCountException();
        }
    }

    void cancelSelectMultiMedia(int index) {
        mediaPickWrapperList.get(index).setSelect(false);
        mMediaEntityList.remove(mediaPickWrapperList.get(index).getMediaEntity());
    }

    boolean canPickMultiMedia() {
        return maxSelectCount <= 0 || getSelectMultiMediaCount() < maxSelectCount;
    }

    boolean isSelect(int index) {
        return mediaPickWrapperList.get(index).isSelect();
    }

    List<MediaEntity> getSelectMultiMediaList() {
        return mMediaEntityList;
    }

    int getSelectMultiMediaCount() {
        return mMediaEntityList.size();
    }

    int getAllMultiMediaCount() {
        return mediaPickWrapperList.size();
    }

    int getMaxSelectCount() {
        return maxSelectCount;
    }

    int getSearchType() {
        return searchType;
    }
}
