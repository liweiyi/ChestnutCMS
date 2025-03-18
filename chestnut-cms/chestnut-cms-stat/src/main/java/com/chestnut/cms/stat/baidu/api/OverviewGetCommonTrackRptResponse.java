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
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 网站概况(来源网站、搜索词、入口页面、受访页面)接口返回结果
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class OverviewGetCommonTrackRptResponse extends BaiduTjResponse {

    /**
     * 来源网站
     */
    private List<RatioData> sourceSiteRatioList = new ArrayList<>();

    /**
     * 搜索词
     */
    private List<RatioData> wordRatioList = new ArrayList<>();

    /**
     * 入口页面
     */
    private List<RatioData> landingPageRatioList = new ArrayList<>();

    /**
     * 访问页面
     */
    private List<RatioData> visitPageRatioList = new ArrayList<>();

    /**
     * 新老访客
     */
    private VisitorData visitorData;

    @Override
    public void fromJson(JsonNode jsonNode) {
        super.fromJson(jsonNode);
        if (!this.isSuccess()) {
            return;
        }
        JsonNode resultNode = jsonNode.get("result");

        readRatioData(resultNode.get("sourceSite"), this.sourceSiteRatioList);

        readRatioData(resultNode.get("sourceSite"), this.wordRatioList);

        readRatioData(resultNode.get("landingPage"), this.landingPageRatioList);

        readRatioData(resultNode.get("visitPage"), this.visitPageRatioList);

        readVisitorData(resultNode.get("visitType"));
    }

    private void readRatioData(JsonNode jsonNode, List<RatioData> ratioDataList) {
        JsonNode items = jsonNode.get("items");
        items.forEach(item -> {
            RatioData ratioData = new RatioData();
            ratioData.setLabel(item.get(0).asText());
            ratioData.setPv_count(item.get(1).asInt());
            ratioData.setRatio(item.get(2).asDouble());
            ratioDataList.add(ratioData);
        });
    }

    private void readVisitorData(JsonNode jsonNode) {
        this.visitorData = new VisitorData();
        JsonNode newVisitor = jsonNode.get("newVisitor");
        visitorData.newVisitor = List.of(
            new VisitorMetrics(BaiduTjMetrics.pv_count.name(), BaiduTjMetrics.pv_count.getLabel(), newVisitor.required(BaiduTjMetrics.pv_count.name()).asInt()),
            new VisitorMetrics(BaiduTjMetrics.visitor_count.name(), BaiduTjMetrics.visitor_count.getLabel(), newVisitor.required(BaiduTjMetrics.visitor_count.name()).asInt()),
            new VisitorMetrics(BaiduTjMetrics.bounce_ratio.name(), BaiduTjMetrics.bounce_ratio.getLabel(), newVisitor.required(BaiduTjMetrics.bounce_ratio.name()).asDouble()),
            new VisitorMetrics(BaiduTjMetrics.avg_visit_time.name(), BaiduTjMetrics.avg_visit_time.getLabel(), newVisitor.required(BaiduTjMetrics.avg_visit_time.name()).asInt()),
            new VisitorMetrics(BaiduTjMetrics.avg_visit_pages.name(), BaiduTjMetrics.avg_visit_pages.getLabel(), newVisitor.required(BaiduTjMetrics.avg_visit_pages.name()).asDouble()),
            new VisitorMetrics(BaiduTjMetrics.ratio.name(), BaiduTjMetrics.ratio.getLabel(), newVisitor.required(BaiduTjMetrics.ratio.name()).asDouble())
        );
        JsonNode oldVisitor = jsonNode.get("oldVisitor");
        visitorData.oldVisitor = List.of(
            new VisitorMetrics(BaiduTjMetrics.pv_count.name(), BaiduTjMetrics.pv_count.getLabel(), oldVisitor.required(BaiduTjMetrics.pv_count.name()).asInt()),
            new VisitorMetrics(BaiduTjMetrics.visitor_count.name(), BaiduTjMetrics.visitor_count.getLabel(), oldVisitor.required(BaiduTjMetrics.visitor_count.name()).asInt()),
            new VisitorMetrics(BaiduTjMetrics.bounce_ratio.name(), BaiduTjMetrics.bounce_ratio.getLabel(), oldVisitor.required(BaiduTjMetrics.bounce_ratio.name()).asDouble()),
            new VisitorMetrics(BaiduTjMetrics.avg_visit_time.name(), BaiduTjMetrics.avg_visit_time.getLabel(), oldVisitor.required(BaiduTjMetrics.avg_visit_time.name()).asInt()),
            new VisitorMetrics(BaiduTjMetrics.avg_visit_pages.name(), BaiduTjMetrics.avg_visit_pages.getLabel(), oldVisitor.required(BaiduTjMetrics.avg_visit_pages.name()).asDouble()),
            new VisitorMetrics(BaiduTjMetrics.ratio.name(), BaiduTjMetrics.ratio.getLabel(), oldVisitor.required(BaiduTjMetrics.ratio.name()).asDouble())
        );
    }

    @Getter
    @Setter
    public static class RatioData {

        private String label;

        private int pv_count;

        private double ratio;
    }

    @Getter
    @Setter
    public static class VisitorData {
        private List<VisitorMetrics> oldVisitor;
        private List<VisitorMetrics> newVisitor;
    }

    public record VisitorMetrics(String name, String label, Number value){}
}
