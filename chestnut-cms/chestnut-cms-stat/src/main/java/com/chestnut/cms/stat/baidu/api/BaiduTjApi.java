/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
