package com.ulfy.android.task;

import android.content.Context;

/**
 * 任务便捷执行工具类
 */
public final class TaskUtils {

    /**
     * 执行一个加载数据的任务
     */
    public static void loadData(Context context, LoadDataUiTask.OnExecute executeBody, Transponder transponder) {
        loadData(context, executeBody, transponder, TaskExecutor.defaultExecutor(), TaskConfig.Config.checkInternet);
    }

    /**
     * 执行一个加载数据的任务
     */
    public static void loadData(Context context, LoadDataUiTask.OnExecute executeBody, Transponder transponder, ITaskExecutor executor) {
        loadData(context, executeBody, transponder, executor, TaskConfig.Config.checkInternet);
    }

    /**
     * 执行一个加载数据的任务
     */
    public static void loadData(Context context, LoadDataUiTask.OnExecute executeBody, Transponder transponder, boolean checkInternet) {
        loadData(context, executeBody, transponder, TaskExecutor.defaultExecutor(), checkInternet);
    }

    /**
     * 执行一个加载数据的任务
     * 指定执行器并设置是否显示网络错误
     */
    public static void loadData(Context context, LoadDataUiTask.OnExecute executeBody, Transponder transponder, ITaskExecutor executor, boolean checkInternet) {
        LoadDataUiTask loadDataUiTask = new LoadDataUiTask(context, executeBody, transponder);
        if (checkInternet) {
            executor.post(new NetUiTask(context, loadDataUiTask, transponder));
        } else {
            executor.post(loadDataUiTask);
        }
    }

    /**
     * 执行一个加载分页数据的任务
     */
    public static void loadData(Context context, LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody, Transponder transponder) {
        loadData(context, taskInfo, executeBody, transponder, TaskExecutor.defaultExecutor(), TaskConfig.Config.checkInternet);
    }

    /**
     * 执行一个加载分页数据的任务
     */
    public static void loadData(Context context, LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody, Transponder transponder, ITaskExecutor executor) {
        loadData(context, taskInfo, executeBody, transponder, executor, TaskConfig.Config.checkInternet);
    }

    /**
     * 执行一个加载分页数据的任务
     */
    public static void loadData(Context context, LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody, Transponder transponder, boolean checkInternet) {
        loadData(context, taskInfo, executeBody, transponder, TaskExecutor.defaultExecutor(), checkInternet);
    }

    /**
     * 执行一个加载数据的任务
     */
    public static void loadData(Context context, LoadListPageUiTask.LoadListPageUiTaskInfo taskInfo, LoadListPageUiTask.OnLoadListPage executeBody, Transponder transponder, ITaskExecutor executor, boolean checkInternet) {
        LoadListPageUiTask loadListPageUiTask = new LoadListPageUiTask(context, taskInfo, executeBody, transponder);
        if (checkInternet) {
            executor.post(new NetUiTask(context, loadListPageUiTask, transponder));
        } else {
            executor.post(loadListPageUiTask);
        }
    }

}
