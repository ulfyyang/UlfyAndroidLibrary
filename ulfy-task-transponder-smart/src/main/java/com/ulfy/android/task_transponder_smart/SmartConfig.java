package com.ulfy.android.task_transponder_smart;

import android.content.Context;

import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;

public final class SmartConfig {
    public static SmartRefreshConfig smartRefreshConfig;

    static {
        smartRefreshConfig = new DefaultSmartRefreshConfig();
    }

    /**
     * 下拉刷新样式配置
     */
    public interface SmartRefreshConfig {
        /**
         * 配置下拉刷新头的样式
         */
        public RefreshHeader getRefreshHeaderView(Context context);
        /**
         * 配置上拉加载头的样式
         */
        public RefreshFooter getRefreshFooterView(Context context);
    }

    public static class DefaultSmartRefreshConfig implements SmartRefreshConfig {
        @Override public RefreshHeader getRefreshHeaderView(Context context) {
            return new MaterialHeader(context);
        }
        @Override public RefreshFooter getRefreshFooterView(Context context) {
            return new ClassicsFooter(context);
        }
    }
}
