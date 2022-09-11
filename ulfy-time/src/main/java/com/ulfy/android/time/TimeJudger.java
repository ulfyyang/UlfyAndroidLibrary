package com.ulfy.android.time;

/**
 * 时间判断其
 *      判断当前时间是否在指定天数内
 *      判断当前时间是否在指定小时内
 *      重置时间判断
 */
public final class TimeJudger {

    /**
     * 是否在指定的天数之内
     *      如果正好等于指定的天数则不表示在其内
     *      天数是按照日期计算的，只要日期不在同一天则表示不同的天数，计时不够24小时也会被算作一天
     */
    public static boolean isInDays(String key, int days) {
        TimeJudgerItemData itemData = TimeJudgerItemDataRepository.getInstance().findItemByKey(key);
        return itemData != null && itemData.isInDays(days);
    }

    /**
     * 是否在指定的小时数之内
     *      如果正好等于指定的小时数则不表示在其内
     *      小时是按照具体的小时个数计算的，如1天有24个小时
     */
    public static boolean isInHours(String key, int hours) {
        TimeJudgerItemData itemData = TimeJudgerItemDataRepository.getInstance().findItemByKey(key);
        return itemData != null && itemData.isInHours(hours);
    }

    /**
     * 重置当前时间记录（未初始化的表示任意时间，任意的时间表示不在任意的时间区间内）
     *      这里的重置会将时间重置为当前的时间
     */
    public synchronized static void initTimeJudger(String key) {
        TimeJudgerItemData itemData = TimeJudgerItemDataRepository.getInstance().findItemByKey(key);
        if (itemData != null) {
            itemData.initTimeJudger();
        }
    }

    /**
     * 清除所有时间记录
     *      这里的清除相当于清除所有的数据，下次使用又从未初始化为起点开始
     */
    public synchronized static void clearTimeJudger() {
        TimeJudgerItemDataRepository.getInstance().clear();
    }

}
