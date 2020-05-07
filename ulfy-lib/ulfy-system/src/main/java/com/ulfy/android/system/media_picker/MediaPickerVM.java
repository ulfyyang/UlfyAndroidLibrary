package com.ulfy.android.system.media_picker;

import android.annotation.SuppressLint;
import android.content.Context;

import com.ulfy.android.mvvm.IViewModel;

import java.util.LinkedList;
import java.util.List;

class MediaPickerVM {
    private MediaPickerControl mediaPickerControl;
    List<Object> cmList = new LinkedList<>();
    private int searchType;

    MediaPickerVM(int maxCount, int searchType) {
        mediaPickerControl = new MediaPickerControl(maxCount, searchType);
        this.searchType = searchType;
    }

    void loadMultiMedias(Context context, List<MediaEntity> selectMultiMediaEntities) {
        List<MediaPickWrapper> mediaPickWrappers = mediaPickerControl.searchMultiMediaPickWrappers(context, selectMultiMediaEntities);

        cmList.clear();
        // 如果是选图，则添加一个拍照选项
        if (searchType == MediaRepository.SEARCH_TYPE_PICTURE) {
            cmList.add(new MediaPickPictureAddPictureCM());
        }

        for (MediaPickWrapper wrapper : mediaPickWrappers) {

            IViewModel model = null;

            switch (searchType) {
                case MediaRepository.SEARCH_TYPE_VIDEO:
                    MediaPickerVideoCM videoCM = new MediaPickerVideoCM();
                    videoCM.wrapper = wrapper;
                    model = videoCM;
                    break;
                case MediaRepository.SEARCH_TYPE_PICTURE:
                    MediaPickerPictureCM pictureCM = new MediaPickerPictureCM();
                    pictureCM.wrapper = wrapper;
                    model = pictureCM;
                    break;
            }

            cmList.add(model);
        }
    }

    void reloadMultiMedias(Context context) {
        List<MediaPickWrapper> mediaPickWrappers = mediaPickerControl.reloadMultiMediaPickWrappers(context);

        cmList.clear();
        // 如果是选图，则添加一个拍照选项
        if (searchType == MediaRepository.SEARCH_TYPE_PICTURE) {
            cmList.add(new MediaPickPictureAddPictureCM());
        }
        for (MediaPickWrapper wrapper : mediaPickWrappers) {

            IViewModel model = null;

            switch (searchType) {
                case MediaRepository.SEARCH_TYPE_VIDEO:
                    MediaPickerVideoCM videoCM = new MediaPickerVideoCM();
                    videoCM.wrapper = wrapper;
                    model = videoCM;
                    break;
                case MediaRepository.SEARCH_TYPE_PICTURE:
                    MediaPickerPictureCM pictureCM = new MediaPickerPictureCM();
                    pictureCM.wrapper = wrapper;
                    model = pictureCM;
                    break;
            }

            cmList.add(model);
        }
    }

    void clickItem(int index) throws OverMaxSelectMediaCountException {
        mediaPickerControl.selectOrCancelMultiMedia(index);
    }

    boolean canPickMultiMedia() {
        return mediaPickerControl.canPickMultiMedia();
    }

    @SuppressLint("DefaultLocale")
    String getCompleteText() {
        if (mediaPickerControl.getMaxSelectCount() == 0) {
            return String.format("完成(%d)", mediaPickerControl.getSelectMultiMediaCount());
        } else {
            return String.format("完成(%d/%d)", mediaPickerControl.getSelectMultiMediaCount(),
                    mediaPickerControl.getMaxSelectCount());
        }
    }

    List<MediaEntity> getSelectEntities() {
        return mediaPickerControl.getSelectMultiMediaList();
    }

    MediaPickerControl getMediaPickerControl() {
        return mediaPickerControl;
    }
}
