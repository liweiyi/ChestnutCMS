package com.chestnut.cms.stat.baidu.api;

import com.chestnut.cms.stat.baidu.BaiduTjMetrics;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.net.URI;
import java.util.List;

/**
 * 网站概况（来源网站、搜索词、入口页面、受访页面）接口请求参数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@SuperBuilder
public class OverviewGetCommonTrackRptApi extends BaiduTjDataApi {

    private String start_date;

    private String end_date;

    private List<BaiduTjMetrics> metrics;

    public OverviewGetCommonTrackRptResponse request() {
        String url = generateApiUrl();
        String responseText = HttpUtils.get(URI.create(url));
        OverviewGetCommonTrackRptResponse response = new OverviewGetCommonTrackRptResponse();
        response.fromJson(JacksonUtils.from(responseText, ObjectNode.class));
        return response;
    }

    public String generateApiUrl() {
        return API_URL + "?method=overview/getCommonTrackRpt&access_token=" + this.getAccess_token()
                + "&site_id=" + this.getSite_id()
                + "&start_date=" + start_date + "&end_date=" + end_date
                + "&metrics=" + BaiduTjMetrics.pv_count.name();
    }

}
