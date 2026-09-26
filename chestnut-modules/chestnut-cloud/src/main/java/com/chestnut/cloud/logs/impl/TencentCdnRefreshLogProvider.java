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

import com.chestnut.cloud.domain.dto.QueryCdnRefreshLogRequest;
import com.chestnut.cloud.domain.vo.CdnRefreshLogVO;
import com.chestnut.cloud.logs.ICdnRefreshLogProvider;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.tencent.TencentCloudException;
import com.chestnut.common.tencent.TencentCloudProvider;
import com.chestnut.common.tencent.TencentConfig;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.tencentcloudapi.cdn.v20180606.CdnClient;
import com.tencentcloudapi.cdn.v20180606.models.DescribePurgeTasksRequest;
import com.tencentcloudapi.cdn.v20180606.models.DescribePushTasksRequest;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
public class TencentCdnRefreshLogProvider implements ICdnRefreshLogProvider {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String getCloudType() {
        return TencentCloudProvider.ID;
    }

    @Override
    public TableData<CdnRefreshLogVO> queryLogs(ObjectNode props, QueryCdnRefreshLogRequest query) {
        CdnClient client = createClient(props);
        long offset = (long) (query.getPageNum() - 1) * query.getPageSize();
        try {
            if ("preheat".equals(query.getType())) {
                DescribePushTasksRequest request = new DescribePushTasksRequest();
                request.setStartTime(query.getBeginTime().format(TIME_FORMAT));
                request.setEndTime(query.getEndTime().format(TIME_FORMAT));
                request.setOffset(offset);
                request.setLimit((long) query.getPageSize());
                if (StringUtils.isNotBlank(query.getUrl())) {
                    request.setKeyword(query.getUrl().trim());
                }
                var response = client.DescribePushTasks(request);
                List<CdnRefreshLogVO> rows = response.getPushLogs() == null ? List.of()
                        : Arrays.stream(response.getPushLogs()).map(task -> new CdnRefreshLogVO(
                                task.getUrl(), "preheat", "url", parseTime(task.getCreateTime()),
                                CdnRefreshLogVO.Status.fromCloudStatus(task.getStatus())
                        )).toList();
                return TableData.of(rows, response.getTotalCount() == null ? 0 : response.getTotalCount());
            }
            DescribePurgeTasksRequest request = new DescribePurgeTasksRequest();
            request.setPurgeType("directory".equals(query.getType()) ? "path" : "url");
            request.setStartTime(query.getBeginTime().format(TIME_FORMAT));
            request.setEndTime(query.getEndTime().format(TIME_FORMAT));
            request.setOffset(offset);
            request.setLimit((long) query.getPageSize());
            if (StringUtils.isNotBlank(query.getUrl())) {
                request.setKeyword(query.getUrl().trim());
            }
            var response = client.DescribePurgeTasks(request);
            List<CdnRefreshLogVO> rows = response.getPurgeLogs() == null ? List.of()
                    : Arrays.stream(response.getPurgeLogs()).map(task -> new CdnRefreshLogVO(
                            task.getUrl(), "refresh", "path".equalsIgnoreCase(task.getPurgeType()) ? "directory" : "url",
                            parseTime(task.getCreateTime()), CdnRefreshLogVO.Status.fromCloudStatus(task.getStatus())
                    )).toList();
            return TableData.of(rows, response.getTotalCount() == null ? 0 : response.getTotalCount());
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudException("Query CDN refresh/preheat tasks failed: " + e.getMessage(), e);
        }
    }

    protected CdnClient createClient(ObjectNode props) {
        TencentConfig config = JacksonUtils.convertValue(props, TencentConfig.class);
        if (config == null || StringUtils.isBlank(config.getSecretId()) || StringUtils.isBlank(config.getSecretKey())) {
            throw new TencentCloudException("Missing cloud secret ID or secret key.");
        }
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("cdn.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        return new CdnClient(new Credential(config.getSecretId().trim(), config.getSecretKey().trim()), "", clientProfile);
    }

    private LocalDateTime parseTime(String value) {
        return StringUtils.isBlank(value) ? null : LocalDateTime.parse(value, TIME_FORMAT);
    }
}
