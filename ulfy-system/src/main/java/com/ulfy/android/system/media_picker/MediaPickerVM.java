package com.ulfy.android.system.media_picker;

import com.ulfy.android.mvvm.IViewModel;

import java.util.ArrayList;
import java.util.List;

class MediaPickerVM {
    public int searchType = MediaRepository.SEARCH_TYPE_PICTURE;    // 搜索的媒体类型，定义在MediaRepository中。默认是图片
    public int maxCount = 0;                                        // 最多可选选择的数量：0表示无限制
    private List<MediaPickWrapper>  wrapperList;                    // 存放用于显示的数据列表
    public List<MediaEntity> selectMultiMediaEntities = new ArrayList<>();  // 被选中的文件列表
    public List<IViewModel> mediaCMList = new ArrayList<>();        // 存放用于显示的数据模型

    /**
     * 初始化状态，需要在Activity接收到数据后调用
     */
    public void init(int searchType, int maxCount, List<MediaEntity> selectMultiMediaEntities) {
        if (searchType == 0) {
            searchType = MediaRepository.SEARCH_TYPE_PICTURE;
        }
        if (maxCount < 0) {
            maxCount = 0;
        }
        if (selectMultiMediaEntities == null) {
            selectMultiMediaEntities = new ArrayList<>();
        }
        this.searchType = searchType; this.maxCount = maxCount;
        this.selectMultiMediaEntities = selectMultiMediaEntities;
    }

    /**
     * 初始化媒体数据，Activity首次进入时调用
     */
    public void initMediaCMListData() {
        wrapperList = newWrapperList();
        mediaCMList.clear();
        // 如果是选图，则添加一个拍照选项
        if (searchType == MediaRepository.SEARCH_TYPE_PICTURE) {
            mediaCMList.add(new MediaPickPictureAddPictureCM());
        }
        // 循环将数据添加到模型列表中
        for (MediaPickWrapper wrapper : wrapperList) {
            IViewModel model = convertWrapperToModel(wrapper);
            if (model != null) {
                mediaCMList.add(model);
            }
        }
    }

    private List<MediaPickWrapper> newWrapperList() {
        List<MediaPickWrapper> wrapperList = new ArrayList<>();
        MediaRepository.getInstance().init(searchType);
        for (int index = 0; index < MediaRepository.getInstance().size(); index++) {
            wrapperList.add(new MediaPickWrapper(selectMultiMediaEntities, index));
        }
        return wrapperList;
    }

    private IViewModel convertWrapperToModel(MediaPickWrapper wrapper) {
        switch (searchType) {
            case MediaRepository.SEARCH_TYPE_VIDEO:
                return new MediaPickerVideoCM(wrapper);
            case MediaRepository.SEARCH_TYPE_PICTURE:
                return new MediaPickerPictureCM(wrapper);
            default:
                return null;
        }
    }

    /**
     * 点击具体的一项时执行的操作
     * @return  如果选择数量达到了最大值返回false，否则发挥true
     */
    public boolean clickItem(int index) {
        if (searchType == MediaRepository.SEARCH_TYPE_PICTURE) {
            index = index - 1;
        }
        if (!canPickMore() && !wrapperList.get(index).isSelect()) {
            return false;
        } else {
            MediaEntity mediaEntity = wrapperList.get(index).getMediaEntity();
            if (wrapperList.get(index).isSelect()) {
                selectMultiMediaEntities.remove(mediaEntity);
            } else {
                selectMultiMediaEntities.add(mediaEntity);
            }
            return true;
        }
    }

    /**
     * 是否可以继续选择，如果没有超出最大选择数量就可以
     */
    public boolean canPickMore() {
        return maxCount <= 0 || selectMultiMediaEntities.size() < maxCount;
    }

    /**
     * 获取标题
     */
    public String getActivityTitile() {
        switch (searchType) {
            case MediaRepository.SEARCH_TYPE_PICTURE:
                return "相册";
            case MediaRepository.SEARCH_TYPE_VIDEO:
                return "视频";
            case MediaRepository.SEARCH_TYPE_VOICE:
                return "声音";
            default:
                return "无法识别的类型";
        }
    }

    /**
     * 获取完成按钮文本
     */
    public String getCompleteText() {
        if (maxCount == 0) {
            return String.format("完成(%d)", selectMultiMediaEntities.size());
        } else {
            return String.format("完成(%d/%d)", selectMultiMediaEntities.size(), maxCount);
        }
    }
}
