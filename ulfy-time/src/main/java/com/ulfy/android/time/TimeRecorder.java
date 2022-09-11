package com.ulfy.android.time;

/**
 * 时间记录器，时间的判定单位为毫秒
 *      通过一个字符串的Key来跟踪每个记录
 *      默认记录只有当天有效
 */
public final class TimeRecorder {
    public static final int RECORD_SCOPE_TODAY = 0;      // 记录当天有效
    public static final int RECORD_SCOPE_ALWAYS = 1;     // 记录永久有效

    /*
    ============================ 外部使用核心方法 =====================================
     */

    /**
     * 开启记录
     */
    public synchronized static void startRecord(String key) {
        TimeRecorderDriver driver = TimeRecorderDriverRepository.getInstance().findDriverByKey(key);
        if (driver != null) {
            if (driver.getOnDriveListener() == null) {
                driver.setOnDriveListener(new TimeRecorderDriver.OnDriveListener() {
                    @Override public void onDrive(String key) {
                        TimeRecorderItemDataRepository.getInstance().findItemByKey(key).increaseSecond(1);
                    }
                });
            }
            driver.startDrive();
        }
    }

    /**
     * 停止记录
     */
    public synchronized static void stopRecord(String key) {
        TimeRecorderDriver driver = TimeRecorderDriverRepository.getInstance().findDriverByKey(key);
        if (driver != null) {
            driver.stopDrive();
        }
    }

    /**
     * 设置记录过程中的监听事件
     *      回调运行在UI线程
     */
    public synchronized static void setOnTimeRecordListener(String key, OnTimeRecordListener onTimeRecordListener) {
        TimeRecorderDriver driver = TimeRecorderDriverRepository.getInstance().findDriverByKey(key);
        if (driver != null) {
            driver.setOnTimeRecordListener(onTimeRecordListener);
        }
    }

    /**
     * 获取记录的秒数
     */
    public synchronized static long getRecordSecond(String key) {
        TimeRecorderItemData itemData = TimeRecorderItemDataRepository.getInstance().findItemByKey(key);
        return itemData == null ? 0 : itemData.getRecordSecond();
    }

    /**
     * 是否到达了指定的秒时间
     */
    public synchronized static boolean isSecondTimeArrived(String key, long seconds) {
        TimeRecorderItemData itemData = TimeRecorderItemDataRepository.getInstance().findItemByKey(key);
        return itemData != null && itemData.isSecondTimeArrived(seconds);
    }

    /**
     * 是否到达了指定的分时间
     */
    public synchronized static boolean isMinuteTimeArrived(String key, long minutes) {
        TimeRecorderItemData itemData = TimeRecorderItemDataRepository.getInstance().findItemByKey(key);
        return itemData != null && itemData.isMinuteTimeArrived(minutes);
    }

    /**
     * 设置记录范围
     */
    public synchronized static void setRecordScope(String key, int recordScope) {
        TimeRecorderItemData itemData = TimeRecorderItemDataRepository.getInstance().findItemByKey(key);
        if (itemData != null) {
            itemData.setRecordScope(recordScope);
        }
    }

    /**
     * 重置当前时间记录
     */
    public synchronized static void resetTimeRecorder(String key) {
        TimeRecorderItemData itemData = TimeRecorderItemDataRepository.getInstance().findItemByKey(key);
        if (itemData != null) {
            itemData.initTimeRecord();
        }
    }

    /**
     * 重置所有时间记录
     */
    public synchronized static void resetTimeRecorder() {
        TimeRecorderItemDataRepository.getInstance().clear();
    }
}
