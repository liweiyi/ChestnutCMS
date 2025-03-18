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

import java.util.*;

/**
 * 搜索词来源口返回结果
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class SourceSearchWordAResponse extends BaiduTjResponse {

    private long total;

    private List<SumData> sumDataList = new ArrayList<>();

    private List<SumData> pageSumDataList = new ArrayList<>();

    private List<VisitSource> sourceDataList = new ArrayList<>();

    /**
     * x轴
     */
    private List<String> xAxisDatas;

    /**
     * y轴数据，Map<指标，List<X轴数数据>>
     */
    private Map<String, List<Number>> datas  = new HashMap<>();

    @Override
    public void fromJson(JsonNode jsonNode) {
        super.fromJson(jsonNode);
        if (!this.isSuccess()) {
            return;
        }
        JsonNode resultNode = jsonNode.get("result");
        this.total = resultNode.get("total").asLong();
        List<String> fields = new ArrayList<>();
        JsonNode fieldNodes = resultNode.get("fields");
        for (int i = 1; i < fieldNodes.size(); i++) {
            fields.add(fieldNodes.get(i).asText());
        }
        JsonNode sumJsonNode = resultNode.get("sum").get(0);
        for (int i = 0; i < fields.size(); i++) {
            BaiduTjMetrics metrics = BaiduTjMetrics.valueOf(fields.get(i));
            sumDataList.add(new SumData(metrics.name(), metrics.getLabel(), sumJsonNode.get(i).asDouble()));
        }
        JsonNode pageSumJsonNode = resultNode.get("pageSum").get(0);
        for (int i = 0; i < fields.size(); i++) {
            BaiduTjMetrics metrics = BaiduTjMetrics.valueOf(fields.get(i));
            pageSumDataList.add(new SumData(metrics.name(), metrics.getLabel(), pageSumJsonNode.get(i).asDouble()));
        }

        JsonNode itemNodes = resultNode.get("items").get(0);
        JsonNode dataNodes = resultNode.get("items").get(1);
        for (int i = 0; i < itemNodes.size(); i++) {
            JsonNode itemNode = itemNodes.get(i).get(0);
            String name = itemNode.get("name").asText();
            String gbkName = itemNode.get("gbkName").asText();
            String keywordId = itemNode.get("keywordId").asText();
            String encodeName = itemNode.get("encodeName").asText();
            VisitSource visitSource = new VisitSource();
            visitSource.setName(name);
            visitSource.setGbkName(gbkName);
            visitSource.setKeywordId(keywordId);
            visitSource.setEncodeName(encodeName);
            JsonNode dataArrayNode = dataNodes.get(i);
            for (int j = 0; j < dataArrayNode.size(); j++) {
                BaiduTjMetrics metrics = BaiduTjMetrics.valueOf(fields.get(j));
                BaiduTjLineChatResponse.YAxisData yAxisData = new BaiduTjLineChatResponse.YAxisData();
                yAxisData.setYAxis(metrics.name());
                yAxisData.setYAxisLabel(metrics.getLabel());
                yAxisData.setValue(dataArrayNode.get(j).asDouble());
                visitSource.getDataList().add(yAxisData);
            }
            sourceDataList.add(visitSource);
        }

        this.xAxisDatas = sourceDataList.stream().map(VisitSource::getName).toList();
        for (String field : fields) {
            BaiduTjMetrics metrics = BaiduTjMetrics.valueOf(field);
            List<Number> dataList = sourceDataList.stream().map(visitSource -> {
                Optional<BaiduTjLineChatResponse.YAxisData> first = visitSource.getDataList().stream()
                        .filter(data -> data.getYAxis().equals(metrics.name())).findFirst();
                if (first.isPresent()) {
                    return first.get().getValue();
                } else {
                    return 0d;
                }
            }).toList();
            datas.put(metrics.name(), dataList);
        }
    }

    public record SumData(String metrics, String label, Number value) {}

    @Getter
    @Setter
    public static class VisitSource {

        private String name;

        private String gbkName;

        private String keywordId;

        private String encodeName;

        private List<BaiduTjLineChatResponse.YAxisData> dataList = new ArrayList<>();
    }
}
