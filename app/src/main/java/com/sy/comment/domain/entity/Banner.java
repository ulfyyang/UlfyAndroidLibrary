package com.sy.comment.domain.entity;

import com.stx.xhb.xbanner.entity.SimpleBannerInfo;

public class Banner extends SimpleBannerInfo {
    public String url;

    public Banner(String url) {
        this.url = url;
    }

    @Override public Object getXBannerUrl() {
        return null;
    }
}
