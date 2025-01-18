package com.chestnut.cms.stat.baidu.api;

import com.chestnut.cms.stat.baidu.BaiduTjMetrics;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网站概况（趋势数据）接口请求参数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@SuperBuilder
public class OverviewGetTimeTrendRptApi extends BaiduTjDataApi {

    private String start_date;

    private String end_date;

    private List<BaiduTjMetrics> metrics;

    public OverviewGetTimeTrendRptResponse request() {
        String url = generateApiUrl();
        String responseText = HttpUtils.get(URI.create(url));
        OverviewGetTimeTrendRptResponse response = new OverviewGetTimeTrendRptResponse();
        response.fromJson(JacksonUtils.from(responseText, ObjectNode.class));
        return response;
    }

    public String generateApiUrl() {
        return API_URL + "?method=overview/getTimeTrendRpt&access_token=" + this.getAccess_token()
                + "&site_id=" + this.getSite_id()
                + "&start_date=" + start_date + "&end_date=" + end_date
                + "&metrics=" + metrics.stream().map(BaiduTjMetrics::name).collect(Collectors.joining(","));
    }
}
