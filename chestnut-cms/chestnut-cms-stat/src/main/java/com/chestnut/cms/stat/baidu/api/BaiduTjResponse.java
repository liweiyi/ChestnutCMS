package com.chestnut.cms.stat.baidu.api;

import com.chestnut.common.utils.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * BaiduTjResponse
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class BaiduTjResponse {

    private String error_msg;

    public void fromJson(JsonNode jsonNode) {
        if (jsonNode.has("error_msg")) {
            this.setError_msg(jsonNode.required("error_msg").asText());
        }
    }

    public boolean isSuccess() {
        return StringUtils.isEmpty(getError_msg());
    }
}
