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
package com.chestnut.cloud.logs;

import com.chestnut.cloud.domain.dto.QueryCdnRefreshLogRequest;
import com.chestnut.cloud.domain.vo.CdnRefreshLogVO;
import com.chestnut.common.security.web.TableData;
import tools.jackson.databind.node.ObjectNode;

import java.time.ZoneId;

public interface ICdnRefreshLogProvider {

    /** 两家云厂商的时间统一为北京时间展示和查询。 */
    ZoneId TIME_ZONE = ZoneId.of("Asia/Shanghai");

    String getCloudType();

    TableData<CdnRefreshLogVO> queryLogs(ObjectNode config, QueryCdnRefreshLogRequest query);
}
