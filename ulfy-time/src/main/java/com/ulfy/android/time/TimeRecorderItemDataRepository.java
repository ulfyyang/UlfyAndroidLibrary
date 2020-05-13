package com.ulfy.android.time;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时记录器维护的数据仓库
 *      按照一个字符串的KEY一条记录数据的方式组织数据
 */
class TimeRecorderItemDataRepository implements Serializable {
    private static final long serialVersionUID = -2856616445459094917L;
    private Map<String, TimeRecorderItemData> timeRecordDataMap = new HashMap<>();

    /**
     * 获取实例
     */
    synchronized static TimeRecorderItemDataRepository getInstance() {
        return TimeConfig.cache.isCached(TimeRecorderItemDataRepository.class) ?
                TimeConfig.cache.getCache(TimeRecorderItemDataRepository.class) :
                TimeConfig.cache.cache(new TimeRecorderItemDataRepository());
    }

    /**
     * 根据Key获取具体记录项
     */
    synchronized TimeRecorderItemData findItemByKey(String key) {
        if (key == null || key.length() == 0) {
            return null;
        } else {
            TimeRecorderItemData item = timeRecordDataMap.get(key);
            if (item == null) {
                item = new TimeRecorderItemData(this);
                timeRecordDataMap.put(key, item);
                updateToCache();
            }
            return item;
        }
    }

    synchronized void updateToCache() {
        TimeConfig.cache.cache(this);
    }

    /**
     * 清空时间记录
     */
    synchronized TimeRecorderItemDataRepository clear() {
        timeRecordDataMap.clear();
        TimeConfig.cache.deleteCache(TimeRecorderItemDataRepository.class);
        return this;
    }
}