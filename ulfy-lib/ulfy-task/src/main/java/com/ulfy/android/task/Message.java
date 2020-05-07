package com.ulfy.android.task;

public final class Message {
    public static final int TYPE_NO_NET_CONNECTION = 0;     // 没有网络连接
    public static final int TYPE_NET_ERROR = 1;              // 网络错误
    public static final int TYPE_START = 2;                   // 任务开始
    public static final int TYPE_SUCCESS = 3;                 // 任务成功
    public static final int TYPE_FAIL = 4;                    // 任务失败
    public static final int TYPE_UPDATE = 5;                  // 更新任务
    public static final int TYPE_FINISH = 6;                  // 任务结束
    private int type;
    private Object data;

    public Message(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
