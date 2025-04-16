/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chestnut.cms.stat.baidu.api;

import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
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
