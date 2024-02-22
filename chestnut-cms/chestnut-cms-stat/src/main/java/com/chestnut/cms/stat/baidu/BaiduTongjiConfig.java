/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.cms.stat.baidu;

import com.chestnut.cms.stat.properties.BaiduTjAccessTokenProperty;
import com.chestnut.cms.stat.properties.BaiduTjApiKeyProperty;
import com.chestnut.cms.stat.properties.BaiduTjRefreshTokenProperty;
import com.chestnut.cms.stat.properties.BaiduTjSecretKeyProperty;
import com.chestnut.common.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 百度统计配置
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@AllArgsConstructor
public class BaiduTongjiConfig {

    private String apiKey;
    private String secretKey;
    private String accessToken;
    private String refreshToken;

    public boolean validate() {
        return !StringUtils.isAnyBlank(apiKey, secretKey, refreshToken, accessToken);
    }

    public static BaiduTongjiConfig read(Map<String, String> siteConfigProps) {
        String apiKey = BaiduTjApiKeyProperty.getValue(siteConfigProps);
        String secretKey = BaiduTjSecretKeyProperty.getValue(siteConfigProps);
        String accessToken = BaiduTjAccessTokenProperty.getValue(siteConfigProps);
        String refreshToken = BaiduTjRefreshTokenProperty.getValue(siteConfigProps);
        return new BaiduTongjiConfig(apiKey, secretKey, accessToken, refreshToken);
    }
}
