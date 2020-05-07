package com.ulfy.android.system;

/**
 * 裁切图片用到的参数
 */
public final class CropImageParam {
    public int previewX;       // 预览 x 边的比例
    public int previewY;       // 预览 y 边的比例
    public int outputX;        // 输出 x 边的大小
    public int outputY;        // 输出 y 边的大小

    public CropImageParam() { }

    public CropImageParam(int previewX, int previewY) {
        this.previewX = previewX;
        this.previewY = previewY;
    }

    public CropImageParam(int previewX, int previewY, int outputX, int outputY) {
        this.previewX = previewX;
        this.previewY = previewY;
        this.outputX = outputX;
        this.outputY = outputY;
    }
}
