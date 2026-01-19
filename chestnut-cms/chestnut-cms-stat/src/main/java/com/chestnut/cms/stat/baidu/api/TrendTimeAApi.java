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

import com.chestnut.cms.stat.baidu.BaiduTjMetrics;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 趋势分析接口请求参数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@SuperBuilder
public class TrendTimeAApi extends BaiduTjDataApi {

    private String start_date;

    private String end_date;

    /**
     * 时间粒度
     */
    private String gran;

    /**
     * 来源类型
     */
    private String source;

    /**
     * 设备类型
     */
    private String clientDevice;

    /**
     * 地域（中国=china，北京=province,1，上海=province,2）
     */
    private String area;

    /**
     * 访客类型（新访客=new，老访客=old）
     */
    private String visitor;

    private List<BaiduTjMetrics> metrics;

    public TrendTimeAResponse request() {
        String url = generateApiUrl();
        String responseText = HttpUtils.get(URI.create(url));
        TrendTimeAResponse response = new TrendTimeAResponse();
        response.fromJson(JacksonUtils.from(responseText, ObjectNode.class));
        return response;
    }

    public String generateApiUrl() {
        String url = API_URL + "?method=trend/time/a&access_token=" + getAccess_token() + "&site_id=" + getSite_id()
                + "&start_date=" + start_date + "&end_date=" + end_date + "&gran=" + gran
                + "&metrics=" + metrics.stream().map(BaiduTjMetrics::name).collect(Collectors.joining(","));
        url = StringUtils.appendIfNotEmpty(url, area, "&area=" + area);
        url = StringUtils.appendIfNotEmpty(url, source, "&source=" + source);
        url = StringUtils.appendIfNotEmpty(url, clientDevice, "&clientDevice=" + clientDevice);
        url = StringUtils.appendIfNotEmpty(url, visitor, "&visitor=" + visitor);
        return url;
    }
}
