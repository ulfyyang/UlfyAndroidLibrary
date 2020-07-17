package com.ulfy.android.time;

/**
 * 时间记录的驱动器，用于推动时间记录的增长
 *      比如时间驱动器可以按照每隔固定时间后触发一次时间增长
 *      每次时间增长的驱动都会触发一次对应的回调，该逻辑子类要实现
 */
abstract class TimeRecorderDriver {
    private String key;                                 // 驱动器对应跟踪的key
    private OnDriveListener onDriveListener;            // 驱动器每次驱动的回调（框架捏使用）
    private OnTimeRecordListener onTimeRecordListener;  // 驱动器每次驱动的回调（外部使用）

    /**
     * 构造方法
     */
    TimeRecorderDriver(String key) {
        this.key = key;
    }

    /**
     * 当子类驱动器驱动的时候调用该方法
     *      该方法需要子类手动调用
     */
    synchronized void onDrive() {
        if (onDriveListener != null) {
            onDriveListener.onDrive(key);
        }
        if (onTimeRecordListener != null) {
            onTimeRecordListener.onTimeRecording(key);
        }
    }

    /**
     * 启动跟踪器
     */
    abstract void startDrive();

    /**
     * 停止跟踪器
     */
    abstract void stopDrive();

    OnDriveListener getOnDriveListener() {
        return onDriveListener;
    }

    void setOnDriveListener(OnDriveListener onDriveListener) {
        this.onDriveListener = onDriveListener;
    }

    void setOnTimeRecordListener(OnTimeRecordListener onTimeRecordListener) {
        this.onTimeRecordListener = onTimeRecordListener;
    }

    /**
     * 在驱动器每次驱动的时候都会调用的回调
     *      这个用于内部使用，用于连接记录的更新
     */
    interface OnDriveListener {
        void onDrive(String key);
    }
}
