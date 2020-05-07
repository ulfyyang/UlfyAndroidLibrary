package com.ulfy.android.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;

/**
 * 日志工具类
 */
public final class LogUtils {

    /**
     * 打印一个对象
     */
    public static void log(Object object) {
        log(object, null);
    }

    /**
     * 打印一个对象和错误信息
     */
    public static void log(Object object, Throwable throwable) {
        Log.i(UtilsConfig.Config.logTag,  convertObjectToLogString(object), throwable);
    }

    /**
     * 将一个对象转化为需要打印的字符串
     */
    private static String convertObjectToLogString(Object object) {
        if (object == null) {
            return "";
        } else {
            String message = object.toString();
            // 如果是json则重新排布内容格式
            if ((message.startsWith("{") && message.endsWith("}")) || (message.startsWith("[") && message.endsWith("]"))) {
                try {
                    if (message.startsWith("{")) {
                        return JSON.toJSONString(JSON.parseObject(message), true);
                    } else if (message.startsWith("[")) {
                        return JSON.toJSONString(JSON.parseArray(message), true);
                    } else {
                        return message;
                    }
                } catch (Exception e) {
                    return message;
                }
            }
            // 常规内容直接返回
            else {
                return message;
            }
        }
    }
}
