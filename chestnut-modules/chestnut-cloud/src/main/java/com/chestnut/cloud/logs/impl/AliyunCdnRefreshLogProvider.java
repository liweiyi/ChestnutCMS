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
package com.chestnut.cloud.logs.impl;

import com.aliyun.cdn20180510.Client;
import com.aliyun.cdn20180510.models.DescribeRefreshTasksRequest;
import com.aliyun.cdn20180510.models.DescribeRefreshTasksResponseBody;
import com.aliyun.teautil.models.RuntimeOptions;
import com.chestnut.cloud.domain.dto.QueryCdnRefreshLogRequest;
import com.chestnut.cloud.domain.vo.CdnRefreshLogVO;
import com.chestnut.cloud.logs.ICdnRefreshLogProvider;
import com.chestnut.common.aliyun.AliyunCloudProvider;
import com.chestnut.common.aliyun.AliyunConfig;
import com.chestnut.common.aliyun.AliyunException;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class AliyunCdnRefreshLogProvider implements ICdnRefreshLogProvider {

    @Override
    public String getCloudType() {
        return AliyunCloudProvider.ID;
    }

    @Override
    public TableData<CdnRefreshLogVO> queryLogs(ObjectNode props, QueryCdnRefreshLogRequest query) {
        DescribeRefreshTasksRequest request = new DescribeRefreshTasksRequest()
                .setObjectType(switch (query.getType()) {
                    case "directory" -> "directory";
                    case "preheat" -> "preload";
                    default -> "file";
                })
                .setPageNumber(query.getPageNum())
                .setPageSize(query.getPageSize())
                .setStartTime(DateTimeFormatter.ISO_INSTANT.format(query.getBeginTime().atZone(TIME_ZONE).toInstant()))
                .setEndTime(DateTimeFormatter.ISO_INSTANT.format(query.getEndTime().atZone(TIME_ZONE).toInstant()));
        if (StringUtils.isNotBlank(query.getUrl())) {
            request.setObjectPath(query.getUrl().trim());
        }
        try {
            DescribeRefreshTasksResponseBody body = createClient(props)
                    .describeRefreshTasksWithOptions(request, new RuntimeOptions()).getBody();
            List<CdnRefreshLogVO> rows = body.getTasks() == null || body.getTasks().getCDNTask() == null
                    ? List.of() : body.getTasks().getCDNTask().stream().map(task -> new CdnRefreshLogVO(
                            task.getObjectPath(),
                            "preload".equalsIgnoreCase(task.getObjectType()) ? "preheat" : "refresh",
                            "directory".equalsIgnoreCase(task.getObjectType()) ? "directory" : "url",
                            StringUtils.isBlank(task.getCreationTime()) ? null : OffsetDateTime.parse(task.getCreationTime())
                                    .atZoneSameInstant(TIME_ZONE).toLocalDateTime(),
                            CdnRefreshLogVO.Status.fromCloudStatus(task.getStatus())
                    )).toList();
            return TableData.of(rows, body.getTotalCount() == null ? 0 : body.getTotalCount());
        } catch (Exception e) {
            throw new AliyunException("Query CDN refresh/preheat tasks failed.", e);
        }
    }

    protected Client createClient(ObjectNode props) throws Exception {
        AliyunConfig config = JacksonUtils.convertValue(props, AliyunConfig.class);
        if (config == null || StringUtils.isBlank(config.getAccessKey()) || StringUtils.isBlank(config.getSecretKey())) {
            throw new AliyunException("Missing cloud access key or secret key.");
        }
        com.aliyun.teaopenapi.models.Config clientConfig = new com.aliyun.teaopenapi.models.Config()
                .setAccessKeyId(config.getAccessKey().trim())
                .setAccessKeySecret(config.getSecretKey().trim())
                .setEndpoint("cdn.aliyuncs.com");
        return new Client(clientConfig);
    }
}
