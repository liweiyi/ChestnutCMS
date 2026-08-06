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
package com.chestnut.common.tencent;

import com.chestnut.common.cloud.CdnRefreshType;
import com.chestnut.common.cloud.DnsRecord;
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import tools.jackson.databind.node.ObjectNode;
import com.tencentcloudapi.cdn.v20180606.CdnClient;
import com.tencentcloudapi.cdn.v20180606.models.PurgePathCacheRequest;
import com.tencentcloudapi.cdn.v20180606.models.PurgePathCacheResponse;
import com.tencentcloudapi.cdn.v20180606.models.PurgeUrlsCacheRequest;
import com.tencentcloudapi.cdn.v20180606.models.PurgeUrlsCacheResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.dnspod.v20210323.DnspodClient;
import com.tencentcloudapi.dnspod.v20210323.models.CreateRecordRequest;
import com.tencentcloudapi.dnspod.v20210323.models.CreateRecordResponse;
import com.tencentcloudapi.dnspod.v20210323.models.DeleteRecordRequest;
import com.tencentcloudapi.dnspod.v20210323.models.DescribeRecordListRequest;
import com.tencentcloudapi.dnspod.v20210323.models.DescribeRecordListResponse;
import com.tencentcloudapi.dnspod.v20210323.models.ModifyRecordRequest;
import com.tencentcloudapi.dnspod.v20210323.models.ModifyRecordResponse;
import com.tencentcloudapi.dnspod.v20210323.models.RecordListItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component(ICloudProvider.BEAN_PREFIX + TencentCloudProvider.ID)
public class TencentCloudProvider implements ICloudProvider {

    private static final long DNS_PAGE_SIZE = 100L;

    public static final String ID = "Tencent";

    public static final String NAME = "腾讯云";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void dealSensitive(ObjectNode configProps) {
        configProps.put("secretKey", "******");
    }

    @Override
    public void updateConfigProps(ObjectNode oldProps, ObjectNode newProps) {
        String appSecret = newProps.get("secretKey").asString();
        if ("******".equals(appSecret)) {
            newProps.put("secretKey", oldProps.get("secretKey").asString());
        }
    }

    @Override
    public void refreshCdn(ObjectNode props, CdnRefreshType type, List<String> urls) {
        TencentConfig config = getTencentConfig(props, "Refresh cdn");
        if (CdnRefreshType.REGEX == type) {
            throw CommonErrorCode.CDN_REGEX_NOT_SUPPORTED.exception();
        }
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("cdn.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        Credential cred = new Credential(config.getSecretId().trim(), config.getSecretKey().trim());
        CdnClient client = new CdnClient(cred, "", clientProfile);
        try {
            if (CdnRefreshType.DIR == type) {
                this.refreshDirectory(client, urls);
            } else {
                this.refreshUrl(client, urls);
            }
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudException("Tencent cloud refresh cdn failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String addDnsRecord(ObjectNode props, String domainName, String rr, String type, String value) {
        TencentConfig config = getTencentConfig(props, "Add dns record");
        try {
            DnspodClient client = createDnspodClient(config);
            CreateRecordRequest req = new CreateRecordRequest();
            req.setDomain(domainName);
            req.setSubDomain(rr);
            req.setRecordType(type);
            req.setRecordLine("默认");
            req.setValue(value);
            req.setStatus("ENABLE");
            CreateRecordResponse resp = client.CreateRecord(req);
            String recordId = String.valueOf(resp.getRecordId());
            log.debug("Tencent dnspod add record: {} {} {} -> {}", domainName, rr, type, recordId);
            return recordId;
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudException("Tencent cloud add dns record failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String updateDnsRecord(ObjectNode props, String recordId, String domainName, String rr, String type, String value) {
        TencentConfig config = getTencentConfig(props, "Update dns record");
        try {
            DnspodClient client = createDnspodClient(config);
            ModifyRecordRequest req = new ModifyRecordRequest();
            req.setDomain(domainName);
            req.setRecordId(Long.valueOf(recordId));
            req.setSubDomain(rr);
            req.setRecordType(type);
            req.setRecordLine("默认");
            req.setValue(value);
            req.setStatus("ENABLE");
            ModifyRecordResponse resp = client.ModifyRecord(req);
            String updatedRecordId = String.valueOf(resp.getRecordId());
            log.debug("Tencent dnspod update record: {} {} {} -> {}", domainName, rr, type, updatedRecordId);
            return updatedRecordId;
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudException("Tencent cloud update dns record failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDnsRecord(ObjectNode props, String recordId, String domainName) {
        TencentConfig config = getTencentConfig(props, "Delete dns record");
        try {
            DnspodClient client = createDnspodClient(config);
            DeleteRecordRequest req = new DeleteRecordRequest();
            req.setDomain(domainName);
            req.setRecordId(Long.valueOf(recordId));
            client.DeleteRecord(req);
            log.debug("Tencent dnspod delete record: {} -> {}", domainName, recordId);
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudException("Tencent cloud delete dns record failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DnsRecord> listDnsRecords(ObjectNode props, String domainName) {
        TencentConfig config = getTencentConfig(props, "List dns records");
        try {
            DnspodClient client = createDnspodClient(config);
            List<DnsRecord> records = new ArrayList<>();
            long offset = 0L;
            while (true) {
                DescribeRecordListRequest req = new DescribeRecordListRequest();
                req.setDomain(domainName);
                req.setOffset(offset);
                req.setLimit(DNS_PAGE_SIZE);
                DescribeRecordListResponse resp = client.DescribeRecordList(req);
                RecordListItem[] pageRecords = resp.getRecordList();
                if (pageRecords == null || pageRecords.length == 0) {
                    break;
                }
                for (RecordListItem record : pageRecords) {
                    DnsRecord dnsRecord = new DnsRecord();
                    dnsRecord.setRr(record.getName());
                    dnsRecord.setType(record.getType());
                    dnsRecord.setValue(record.getValue());
                    dnsRecord.setEnabled(DnsRecord.parseEnableStatus(record.getStatus()));
                    records.add(dnsRecord);
                }
                if (pageRecords.length < DNS_PAGE_SIZE) {
                    break;
                }
                offset += DNS_PAGE_SIZE;
            }
            return records;
        } catch (TencentCloudSDKException e) {
            throw new TencentCloudException("Tencent cloud list dns records failed: " + e.getMessage(), e);
        }
    }

    private TencentConfig getTencentConfig(ObjectNode props, String operation) {
        TencentConfig config = JacksonUtils.convertValue(props, TencentConfig.class);
        if (Objects.isNull(config)) {
            throw CommonErrorCode.CLOUD_CONFIG_NULL.exception();
        }
        if (StringUtils.isEmpty(config.getSecretId())) {
            throw CommonErrorCode.CLOUD_MISSING_SECRET_ID.exception();
        }
        if (StringUtils.isEmpty(config.getSecretKey())) {
            throw CommonErrorCode.CLOUD_MISSING_SECRET_KEY.exception();
        }
        return config;
    }

    private DnspodClient createDnspodClient(TencentConfig config) {
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("dnspod.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        Credential cred = new Credential(config.getSecretId().trim(), config.getSecretKey().trim());
        return new DnspodClient(cred, "", clientProfile);
    }

    private void refreshDirectory(CdnClient client , List<String> urls) throws TencentCloudSDKException {
        PurgePathCacheRequest req = new PurgePathCacheRequest();
        req.setPaths(urls.toArray(String[]::new));
        req.setFlushType("flush"); // flush：刷新产生更新的资源, delete：刷新全部资源

        PurgePathCacheResponse resp = client.PurgePathCache(req);
        log.debug("Tencent cloud provider cdn refresh dir resp: {}", JacksonUtils.to(resp));
    }

    private void refreshUrl(CdnClient client, List<String> urls) throws TencentCloudSDKException {
        PurgeUrlsCacheRequest req = new PurgeUrlsCacheRequest();
        req.setUrls(urls.toArray(String[]::new));

        PurgeUrlsCacheResponse resp = client.PurgeUrlsCache(req);
        log.debug("Tencent cloud provider cdn refresh url resp: {}", JacksonUtils.to(resp));
    }
}
