package com.ulfy.android.task_transponder;

/**
 * 如需提供失败重试功能则需要实现该接口
 */
public interface IReloadView {
    public void setOnReloadListener(OnReloadListener onReloadListener);
    public void reload();
}
