package com.ulfy.android.system.media_picker;

import java.util.List;

class MediaPickWrapper {
    private List<MediaEntity> selectMultiMediaEntities;     // 已经选中的文件列表，用来比对当前文件是否已经选中
    private int index;                                      // 位置：用该参数来获取真实的实体

    public MediaPickWrapper(List<MediaEntity> selectMultiMediaEntities, int index) {
        this.selectMultiMediaEntities = selectMultiMediaEntities;
        this.index = index;
    }

    public MediaEntity getMediaEntity() {
        return MediaRepository.getInstance().get(index);
    }

    /**
     * 当数据没有填充到虚拟List中时选中状态是false，当填充进来之后才可以判断是否真的选中
     *      1. 不过这不影响最终返回的结果，最终返回的结果是依据被选中的列表返回的
     *      2. Cell的勾选、取消勾选操作只是操作的已经选中的列表
     */
    public boolean isSelect() {
        MediaEntity entity = MediaRepository.getInstance().get(index);
        if (entity != null) {
            for (MediaEntity mediaEntity : selectMultiMediaEntities) {
                if (mediaEntity.equals(entity)) {
                    return true;
                }
            }
        }
        return false;
    }
}
