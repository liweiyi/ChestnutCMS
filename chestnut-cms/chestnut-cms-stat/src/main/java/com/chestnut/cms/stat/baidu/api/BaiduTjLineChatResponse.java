package com.chestnut.cms.stat.baidu.api;

import com.chestnut.cms.stat.baidu.BaiduTjMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * BaiduTjLineChatResponse
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class BaiduTjLineChatResponse extends BaiduTjResponse {

    /**
     * x轴统计维度列表
     */
    private List<XAxisData> xAxisList = new ArrayList<>();

    /**
     * x轴
     */
    private List<String> xAxisDatas;

    /**
     * y轴数据，Map<指标，List<X轴数数据>>
     */
    private Map<String, List<Number>> datas;

    @Override
    public void fromJson(JsonNode jsonNode) {
        super.fromJson(jsonNode);
        if (!this.isSuccess()) {
            return;
        }
        JsonNode resultNode = jsonNode.get("result");
        List<String> fields = new ArrayList<>();
        resultNode.get("fields").forEach(node -> fields.add(node.asText()));

        JsonNode itemsNode = resultNode.get("items");
        itemsNode.get(0).forEach(node -> {
            XAxisData xAxisData = new XAxisData();
            xAxisData.setXAxis(node.get(0).asText());
            List<YAxisData> yAxisDataList = new ArrayList<>();
            for (int i = 1; i < fields.size(); i++) {
                YAxisData yAxisData = new YAxisData();
                yAxisData.setYAxis(fields.get(i));
                yAxisData.setYAxisLabel(BaiduTjMetrics.valueOf(fields.get(i)).getLabel());
                yAxisDataList.add(yAxisData);
            }
            xAxisData.setYAxisDataList(yAxisDataList);
            xAxisList.add(xAxisData);
        });

        int index = 0;
        Iterator<JsonNode> xAxisDataJsonArray = itemsNode.get(1).elements();
        while (xAxisDataJsonArray.hasNext()) {
            JsonNode yAxisDataArray = xAxisDataJsonArray.next();
            List<YAxisData> yAxisDataList = xAxisList.get(index).getYAxisDataList();
            for (int i = 0; i < yAxisDataList.size(); i++) {
                YAxisData yAxisData = yAxisDataList.get(i);
                yAxisData.setValue(yAxisDataArray.get(i).asDouble());
            }
            index++;
        }

        this.xAxisList.sort(Comparator.comparing(XAxisData::getXAxis));
        this.xAxisDatas = this.xAxisList.stream().map(XAxisData::getXAxis).toList();
        HashMap<String, List<Number>> datas = new HashMap<>();

        this.xAxisList.forEach(xAxisData -> {
            xAxisData.getYAxisDataList().forEach(yAxisData -> {
                List<Number> numbers = datas.computeIfAbsent(yAxisData.getYAxis(), key -> new ArrayList<>());
                numbers.add(yAxisData.getValue());
            });
        });
        this.datas = datas;
    }

    @Getter
    @Setter
    public static class XAxisData {

        /**
         * X轴统计维度（例如：日期[yyyy-MM-dd]）
         */
        private String xAxis;

        /**
         * Y轴统计指标数据
         */
        private List<YAxisData> yAxisDataList;
    }

    @Getter
    @Setter
    public static class YAxisData {

        /**
         * 统计指标标识（例如：PV）
         */
        private String yAxis;

        /**
         * 统计指标名称（例如：页面浏览量）
         */
        private String yAxisLabel;

        /**
         * 统计指标数值
         */
        private Number value;
    }

}
