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
package com.chestnut.cms.stat.baidu;

import lombok.Getter;

/**
 * BaiduTjMetrics
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public enum BaiduTjMetrics {

    pv_count("浏览量（PV）"),
    pv_ratio("浏览量占比"),
    visit_count("访问次数"),
    visitor_count("访客数（UV）"),
    new_visitor_count ("新访客数"),
    new_visitor_ratio ("新访客比率"),
    ip_count("IP数"),
    bounce_ratio("跳出率"),
    avg_visit_time("平均访问时长（秒）"),
    avg_visit_pages("平均访问页数"),
    trans_count("转化次数"),
    trans_ratio("转化率"),
    avg_trans_cost("平均转化成本（元）"),
    income("收益（元）"),
    ratio("占比"),
    total_search_count("总搜索次数"),
    search_engine_baidu("百度"),
    search_engine_g("Google"),
    search_engine_sogou("搜狗"),
    search_engine_other("其他"),
    search_ratio("占比");

    @Getter
    private final String label;

    BaiduTjMetrics(String label) {
        this.label = label;
    }
}
