package com.chestnut.cms.stat.baidu.api;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SiteListRequest
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class SiteListResponse extends BaiduTjResponse {

    private List<BaiduSite> list;

    @Getter
    @Setter
    public static class BaiduSite {

        /**
         * 站点ID
         */
        private Long site_id;

        /**
         * 站点域名
         */
        private String domain;

        /**
         * 状态（0=正常，1=停用）
         */
        private Integer status;

        /**
         * 创建时间
         */
        private LocalDateTime create_time;
    }
}
