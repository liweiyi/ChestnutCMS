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
import com.aliyun.cdn20180510.models.DescribeRefreshTasksResponse;
import com.aliyun.cdn20180510.models.DescribeRefreshTasksResponseBody;
import com.aliyun.cdn20180510.models.DescribeRefreshTasksResponseBody.DescribeRefreshTasksResponseBodyTasks;
import com.aliyun.cdn20180510.models.DescribeRefreshTasksResponseBody.DescribeRefreshTasksResponseBodyTasksCDNTask;
import com.aliyun.teautil.models.RuntimeOptions;
import com.chestnut.cloud.domain.dto.QueryCdnRefreshLogRequest;
import com.chestnut.cloud.domain.vo.CdnRefreshLogVO;
import com.chestnut.common.aliyun.AliyunException;
import com.chestnut.common.tencent.TencentCloudException;
import com.tencentcloudapi.cdn.v20180606.CdnClient;
import com.tencentcloudapi.cdn.v20180606.models.*;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CdnRefreshLogProviderTest {

    @Test
    void aliyunMapsRefreshAndPreheatRequestsAndPreservesPagination() throws Exception {
        FakeAliyunClient client = new FakeAliyunClient();
        var provider = new AliyunCdnRefreshLogProvider() {
            @Override
            protected Client createClient(ObjectNode props) { return client; }
        };
        for (var type : Map.of("url", "file", "directory", "directory", "preheat", "preload").entrySet()) {
            client.response = new DescribeRefreshTasksResponse().setBody(new DescribeRefreshTasksResponseBody()
                    .setTotalCount(43L).setTasks(new DescribeRefreshTasksResponseBodyTasks().setCDNTask(List.of(
                            new DescribeRefreshTasksResponseBodyTasksCDNTask()
                                    .setObjectPath("https://example.com/news/")
                                    .setObjectType(type.getValue())
                                    .setCreationTime("2026-09-03T01:02:03Z")
                                    .setStatus("Complete")))));

            var result = provider.queryLogs(null, query(type.getKey()));

            assertEquals(type.getValue(), client.request.getObjectType());
            assertEquals(3, client.request.getPageNumber());
            assertEquals(20, client.request.getPageSize());
            assertEquals("2026-09-02T00:00:00Z", client.request.getStartTime());
            assertEquals("2026-09-03T02:00:00Z", client.request.getEndTime());
            assertEquals("https://example.com/news/", client.request.getObjectPath());
            assertEquals(43, result.getTotal());
            assertEquals(1, result.getRows().size());
            var row = result.getRows().get(0);
            assertEquals("https://example.com/news/", row.getUrl());
            assertEquals("preheat".equals(type.getKey()) ? "preheat" : "refresh", row.getTaskType());
            assertEquals("directory".equals(type.getKey()) ? "directory" : "url", row.getType());
            assertEquals(LocalDateTime.of(2026, 9, 3, 9, 2, 3), row.getTime());
            assertEquals(CdnRefreshLogVO.Status.SUCCESS, row.getStatus());
        }
    }

    @Test
    void tencentUsesPurgeApiForUrlsAndDirectories() {
        FakeTencentClient client = new FakeTencentClient();
        var provider = tencentProvider(client);
        for (String type : List.of("url", "directory")) {
            PurgeTask task = new PurgeTask();
            task.setUrl("https://example.com/news/");
            task.setPurgeType("directory".equals(type) ? "path" : "url");
            task.setCreateTime("2026-09-03 09:02:03");
            task.setStatus("process");
            client.purgeResponse.setPurgeLogs(new PurgeTask[]{task});
            client.purgeResponse.setTotalCount(43L);

            var result = provider.queryLogs(null, query(type));

            assertNull(client.pushRequest);
            assertEquals(task.getPurgeType(), client.purgeRequest.getPurgeType());
            assertEquals(40L, client.purgeRequest.getOffset());
            assertEquals(20L, client.purgeRequest.getLimit());
            assertEquals("2026-09-02 08:00:00", client.purgeRequest.getStartTime());
            assertEquals("2026-09-03 10:00:00", client.purgeRequest.getEndTime());
            assertEquals("https://example.com/news/", client.purgeRequest.getKeyword());
            assertEquals(43, result.getTotal());
            var row = result.getRows().get(0);
            assertEquals("https://example.com/news/", row.getUrl());
            assertEquals("refresh", row.getTaskType());
            assertEquals(type, row.getType());
            assertEquals(LocalDateTime.of(2026, 9, 3, 9, 2, 3), row.getTime());
            assertEquals(CdnRefreshLogVO.Status.PROCESSING, row.getStatus());
        }
    }

    @Test
    void tencentUsesPushApiForPreheatAndKeepsInvalidStatus() {
        FakeTencentClient client = new FakeTencentClient();
        PushTask task = new PushTask();
        task.setUrl("https://example.com/file.html");
        task.setCreateTime("2026-09-03 09:02:03");
        task.setStatus("invalid");
        client.pushResponse.setPushLogs(new PushTask[]{task});
        client.pushResponse.setTotalCount(41L);

        var result = tencentProvider(client).queryLogs(null, query("preheat"));

        assertNull(client.purgeRequest);
        assertEquals(40L, client.pushRequest.getOffset());
        assertEquals(20L, client.pushRequest.getLimit());
        assertEquals("2026-09-02 08:00:00", client.pushRequest.getStartTime());
        assertEquals("2026-09-03 10:00:00", client.pushRequest.getEndTime());
        assertEquals("https://example.com/news/", client.pushRequest.getKeyword());
        assertEquals(41, result.getTotal());
        var row = result.getRows().get(0);
        assertEquals("https://example.com/file.html", row.getUrl());
        assertEquals("url", row.getType());
        assertEquals("preheat", row.getTaskType());
        assertEquals(CdnRefreshLogVO.Status.INVALID, row.getStatus());
    }

    @Test
    void normalizesCloudStatusesWithoutTreatingUnknownAsSuccess() {
        Map<String, CdnRefreshLogVO.Status> statuses = Map.ofEntries(
                Map.entry("Complete", CdnRefreshLogVO.Status.SUCCESS),
                Map.entry("Done", CdnRefreshLogVO.Status.SUCCESS),
                Map.entry("Refreshing", CdnRefreshLogVO.Status.PROCESSING),
                Map.entry("process", CdnRefreshLogVO.Status.PROCESSING),
                Map.entry("Failed", CdnRefreshLogVO.Status.FAILED),
                Map.entry("fail", CdnRefreshLogVO.Status.FAILED),
                Map.entry("Timeout", CdnRefreshLogVO.Status.TIMEOUT),
                Map.entry("Canceled", CdnRefreshLogVO.Status.CANCELED),
                Map.entry("invalid", CdnRefreshLogVO.Status.INVALID));
        statuses.forEach((value, expected) -> assertEquals(expected, CdnRefreshLogVO.Status.fromCloudStatus(value)));
        assertEquals(CdnRefreshLogVO.Status.UNKNOWN, CdnRefreshLogVO.Status.fromCloudStatus(null));
        assertEquals(CdnRefreshLogVO.Status.UNKNOWN, CdnRefreshLogVO.Status.fromCloudStatus("new-cloud-status"));
    }

    @Test
    void emptyCloudResponsesReturnEmptyPages() throws Exception {
        FakeAliyunClient aliyun = new FakeAliyunClient();
        aliyun.response = new DescribeRefreshTasksResponse().setBody(new DescribeRefreshTasksResponseBody());
        var aliyunProvider = new AliyunCdnRefreshLogProvider() {
            @Override
            protected Client createClient(ObjectNode props) { return aliyun; }
        };
        var result = aliyunProvider.queryLogs(null, query("url"));
        assertTrue(result.getRows().isEmpty());
        assertEquals(0, result.getTotal());
        for (String type : List.of("url", "preheat")) {
            var tencentResult = tencentProvider(new FakeTencentClient()).queryLogs(null, query(type));
            assertTrue(tencentResult.getRows().isEmpty());
            assertEquals(0, tencentResult.getTotal());
        }
    }

    @Test
    void cloudFailuresPropagateInsteadOfReturningEmptyLogs() throws Exception {
        FakeAliyunClient aliyun = new FakeAliyunClient();
        aliyun.failure = new Exception("AccessDenied");
        var provider = new AliyunCdnRefreshLogProvider() {
            @Override
            protected Client createClient(ObjectNode props) { return aliyun; }
        };
        assertSame(aliyun.failure, assertThrows(AliyunException.class,
                () -> provider.queryLogs(null, query("url"))).getCause());
        FakeTencentClient tencent = new FakeTencentClient();
        tencent.failure = new TencentCloudSDKException("UnauthorizedOperation");
        for (String type : List.of("url", "preheat")) {
            assertSame(tencent.failure, assertThrows(TencentCloudException.class,
                    () -> tencentProvider(tencent).queryLogs(null, query(type))).getCause());
        }
    }

    private QueryCdnRefreshLogRequest query(String type) {
        var query = new QueryCdnRefreshLogRequest();
        query.setConfigId(101L);
        query.setType(type);
        query.setPageNum(3);
        query.setPageSize(20);
        query.setUrl(" https://example.com/news/ ");
        query.setBeginTime(LocalDateTime.of(2026, 9, 2, 8, 0));
        query.setEndTime(LocalDateTime.of(2026, 9, 3, 10, 0));
        return query;
    }

    private TencentCdnRefreshLogProvider tencentProvider(FakeTencentClient client) {
        return new TencentCdnRefreshLogProvider() {
            @Override
            protected CdnClient createClient(ObjectNode props) { return client; }
        };
    }

    private static class FakeAliyunClient extends Client {
        DescribeRefreshTasksRequest request;
        DescribeRefreshTasksResponse response;
        Exception failure;

        FakeAliyunClient() throws Exception {
            super(new com.aliyun.teaopenapi.models.Config()
                    .setAccessKeyId("test-id").setAccessKeySecret("test-secret").setEndpoint("cdn.aliyuncs.com"));
        }

        @Override
        public DescribeRefreshTasksResponse describeRefreshTasksWithOptions(DescribeRefreshTasksRequest request,
                                                                            RuntimeOptions runtime) throws Exception {
            this.request = request;
            if (failure != null) throw failure;
            return response;
        }
    }

    private static class FakeTencentClient extends CdnClient {
        DescribePurgeTasksRequest purgeRequest;
        DescribePushTasksRequest pushRequest;
        DescribePurgeTasksResponse purgeResponse = new DescribePurgeTasksResponse();
        DescribePushTasksResponse pushResponse = new DescribePushTasksResponse();
        TencentCloudSDKException failure;

        FakeTencentClient() { super(new Credential("test-id", "test-secret"), ""); }

        @Override
        public DescribePurgeTasksResponse DescribePurgeTasks(DescribePurgeTasksRequest request) throws TencentCloudSDKException {
            this.purgeRequest = request;
            if (failure != null) throw failure;
            return purgeResponse;
        }

        @Override
        public DescribePushTasksResponse DescribePushTasks(DescribePushTasksRequest request) throws TencentCloudSDKException {
            this.pushRequest = request;
            if (failure != null) throw failure;
            return pushResponse;
        }
    }
}
