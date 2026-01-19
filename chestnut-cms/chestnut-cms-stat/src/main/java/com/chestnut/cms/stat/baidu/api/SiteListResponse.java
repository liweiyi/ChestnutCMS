/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
