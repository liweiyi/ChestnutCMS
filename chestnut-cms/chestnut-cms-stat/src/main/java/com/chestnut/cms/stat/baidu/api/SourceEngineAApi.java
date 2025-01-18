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
 * 搜索引擎来源接口请求参数
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@SuperBuilder
public class SourceEngineAApi extends BaiduTjDataApi {

    private String start_date;

    private String end_date;

    private List<BaiduTjMetrics> metrics;

    /**
     * 地域
     */
    private String area;

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

    public SourceEngineAResponse request() {
        String url = generateApiUrl();
        String responseText = HttpUtils.get(URI.create(url));
        SourceEngineAResponse response = new SourceEngineAResponse();
        response.fromJson(JacksonUtils.from(responseText, ObjectNode.class));
        return response;
    }

    public String generateApiUrl() {
        return API_URL + "?method=source/engine/a&access_token=" + this.getAccess_token()
                + "&site_id=" + this.getSite_id() + "&area=" + area
                + "&clientDevice=" + clientDevice + "&visitor=" + visitor
                + "&start_date=" + start_date + "&end_date=" + end_date
                + "&metrics=" + metrics.stream().map(BaiduTjMetrics::name).collect(Collectors.joining(","));
    }
}
