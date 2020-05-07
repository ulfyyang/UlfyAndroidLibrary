package com.ulfy.android.system.media_picker;

import java.io.File;
import java.io.Serializable;

/**
 * 多媒体实体
 */
public class MediaEntity implements Serializable {
    private static final long serialVersionUID = 2375638090901166566L;
    public int id;             // 实体在安卓系统中的id
    public String title;       // 实体标题
    public String filePath;    // 实体在安卓系统中存放的路径
    public File file;          // 具体的文件路径
    public long size;          // 媒体的大小，单位b

    public MediaEntity(int id, String title, String filePath, long size) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.file = new File(filePath);
        this.size = size;
    }

    @Override public final boolean equals(Object obj) {
        return this.getClass() == obj.getClass() && this.id == ((MediaEntity)obj).id;
    }

    public final boolean exists() {
        return new File(filePath).exists();
    }
}
