package com.chestnut.cms.stat.baidu.api;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 趋势分析接口返回结果
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class TrendTimeAResponse extends BaiduTjLineChatResponse {

    private Integer offset;

    private String timeSpan;

    private Integer total;

    private Map<String, Integer> sum;

    private Map<String, Integer> pageSum;

    @Override
    public void fromJson(JsonNode jsonNode) {
        super.fromJson(jsonNode);
        if (!this.isSuccess()) {
            return;
        }
        JsonNode resultNode = jsonNode.get("result");
        List<String> fields = new ArrayList<>();
        resultNode.get("fields").forEach(node -> fields.add(node.asText()));

        setOffset(resultNode.get("offset").asInt());
        setTimeSpan(resultNode.get("timeSpan").get(0).asText());
        setTotal(resultNode.get("total").asInt());

        setSum(new HashMap<>(fields.size() - 1));
        JsonNode sumJsonNode = resultNode.get("sum").get(0);
        for (int i = 1; i < fields.size(); i++) {
            getSum().put(fields.get(i), sumJsonNode.get(i - 1).asInt());
        }
        setPageSum(new HashMap<>(fields.size() - 1));
        JsonNode pageSumJsonNode = resultNode.get("pageSum").get(0);
        for (int i = 1; i < fields.size(); i++) {
            getSum().put(fields.get(i), pageSumJsonNode.get(i - 1).asInt());
        }
    }
}
