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
import java.util.stream.Collectors;

/**
 * 搜索词接口请求参数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@SuperBuilder
public class SourceSearchWordAApi extends BaiduTjDataApi {

    private String start_date;

    private String end_date;

    private List<BaiduTjMetrics> metrics;

    /**
     * 来源类型（无效）
     * indicator = 按指标
     * engine = 按搜索引擎
     */
    private String source;

    /**
     * 设备类型
     * pc = 计算机
     * mobile = 移动设备
     */
    private String clientDevice;

    /**
     * 访客类型
     * new = 新访客
     * old = 老访客
     */
    private String visitor;

    public SourceSearchWordAResponse request() {
        if ("engine".equals(source)) {
            this.metrics = List.of(
                BaiduTjMetrics.total_search_count,
                BaiduTjMetrics.search_engine_baidu,
                BaiduTjMetrics.search_engine_g,
                BaiduTjMetrics.search_engine_sogou,
                BaiduTjMetrics.search_engine_other,
                BaiduTjMetrics.search_ratio
            );
        } else {
            this.metrics = List.of(
                BaiduTjMetrics.pv_count,
                BaiduTjMetrics.pv_ratio,
                BaiduTjMetrics.visitor_count,
                BaiduTjMetrics.new_visitor_count,
                BaiduTjMetrics.new_visitor_ratio,
                BaiduTjMetrics.ip_count,
                BaiduTjMetrics.bounce_ratio,
                BaiduTjMetrics.avg_visit_time,
                BaiduTjMetrics.avg_visit_pages
            );
        }
        String url = generateApiUrl();
        String responseText = HttpUtils.get(URI.create(url));
        SourceSearchWordAResponse response = new SourceSearchWordAResponse();
        response.fromJson(JacksonUtils.from(responseText, ObjectNode.class));
        return response;
    }

    public String generateApiUrl() {
        return API_URL + "?method=source/searchword/a&access_token=" + this.getAccess_token()
                + "&site_id=" + this.getSite_id() + "&flag=" + source
                + "&clientDevice=" + clientDevice + "&visitor=" + visitor
                + "&start_date=" + start_date + "&end_date=" + end_date
                + "&metrics=" + metrics.stream().map(BaiduTjMetrics::name).collect(Collectors.joining(","));
    }
}
