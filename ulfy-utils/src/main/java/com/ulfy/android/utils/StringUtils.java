package com.ulfy.android.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * 字符串辅助工具类
 */
public final class StringUtils {

    /**
     * 判定字符串是否为空，该方法会先去除字符串连边的空格，使用处理过后的字符串进行判断
     */
    public static boolean isEmpty(CharSequence charSequence) {
        if (charSequence == null) {
            return true;
        } else {
            if (charSequence instanceof String) {
                return ((String)charSequence).trim().length() == 0;
            } else {
                return charSequence.length() == 0;
            }
        }
    }

    /**
     * 补全不完整的url地址
     */
    public static String complementUrl(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        return url;
    }
}
