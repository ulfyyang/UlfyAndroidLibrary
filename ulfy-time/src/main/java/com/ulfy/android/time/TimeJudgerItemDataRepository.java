package com.ulfy.android.time;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class TimeJudgerItemDataRepository implements Serializable {
    private static final long serialVersionUID = -4423105209158998161L;
    private Map<String, TimeJudgerItemData> timeJudgerDataMap = new HashMap<>();

    /**
     * 获取实例
     */
    synchronized static TimeJudgerItemDataRepository getInstance() {
        return TimeConfig.cache.isCached(TimeJudgerItemDataRepository.class) ?
                TimeConfig.cache.getCache(TimeJudgerItemDataRepository.class) :
                TimeConfig.cache.cache(new TimeJudgerItemDataRepository());
    }

    /**
     * 根据Key获取具体记录项
     */
    synchronized TimeJudgerItemData findItemByKey(String key) {
        if (key == null || key.length() == 0) {
            return null;
        } else {
            TimeJudgerItemData item = timeJudgerDataMap.get(key);
            if (item == null) {
                item = new TimeJudgerItemData(this);
                timeJudgerDataMap.put(key, item);
                updateToCache();
            }
            return item;
        }
    }

    /**
     * 更新实例
     */
    synchronized void updateToCache() {
        TimeConfig.cache.cache(this);
    }

    /**
     * 清空判定记录
     */
    synchronized TimeJudgerItemDataRepository clear() {
        timeJudgerDataMap.clear();
        TimeConfig.cache.deleteCache(TimeJudgerItemDataRepository.class);
        return this;
    }
}
