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

import com.chestnut.cms.stat.baidu.BaiduTjMetrics;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.URI;

/**
 * 网站概况(地域分布)接口请求参数
 * <p>
 *     只支持PV数据占比查询
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@SuperBuilder
public class OverviewGetDistrictRptApi extends BaiduTjDataApi {

    private String start_date;

    private String end_date;

    public OverviewGetDistrictRptResponse request() {
        String url = generateApiUrl();
        String responseText = HttpUtils.get(URI.create(url));
        ObjectNode responseJson = JacksonUtils.from(responseText, ObjectNode.class);
        OverviewGetDistrictRptResponse response = new OverviewGetDistrictRptResponse();
        response.fromJson(responseJson);
        return response;
    }

    public String generateApiUrl() {
        return API_URL + "?method=overview/getDistrictRpt&access_token=" + this.getAccess_token()
                + "&site_id=" + this.getSite_id()
                + "&start_date=" + start_date + "&end_date=" + end_date
                + "&metrics=" + BaiduTjMetrics.pv_count.name() + "," + BaiduTjMetrics.ratio.name();
    }
}
