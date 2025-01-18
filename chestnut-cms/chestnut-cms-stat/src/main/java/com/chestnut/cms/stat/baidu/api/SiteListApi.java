package com.chestnut.cms.stat.baidu.api;

import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.net.URI;

/**
 * GetSiteListApi
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@SuperBuilder
public class SiteListApi extends BaiduTjApi {

    /**
     * 获取站点列表（百度账号）
     */
    private static final String API_SITE_LIST = "https://openapi.baidu.com/rest/2.0/tongji/config/getSiteList";

    public SiteListResponse get() {
        String url = API_SITE_LIST + "?access_token=" + this.getAccess_token();
        String responseJson = HttpUtils.get(URI.create(url));
        return JacksonUtils.from(responseJson, SiteListResponse.class);
    }
}
