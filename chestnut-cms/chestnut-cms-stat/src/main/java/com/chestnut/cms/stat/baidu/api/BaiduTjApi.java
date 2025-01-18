package com.chestnut.cms.stat.baidu.api;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * BaiduTjApi
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@SuperBuilder
public class BaiduTjApi {

    /**
     * 获取站点统计数据（百度账号）
     */
    static final String API_URL = "https://openapi.baidu.com/rest/2.0/tongji/report/getData";

    private String access_token;
}
