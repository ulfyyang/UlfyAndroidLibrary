package com.ulfy.android.time;

/**
 * 时间记录过程中触发的回调
 */
public interface OnTimeRecordListener {
    /**
     * 当时间正在触发时的回调
     */
    void onTimeRecording(String key);
}
