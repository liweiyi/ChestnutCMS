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
package com.chestnut.common.aliyun;

import com.aliyun.alidns20150109.models.AddDomainRecordRequest;
import com.aliyun.alidns20150109.models.AddDomainRecordResponse;
import com.aliyun.alidns20150109.models.DeleteDomainRecordRequest;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsRequest;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponse;
import com.aliyun.alidns20150109.models.DescribeDomainRecordsResponseBody;
import com.aliyun.alidns20150109.models.UpdateDomainRecordRequest;
import com.aliyun.alidns20150109.models.UpdateDomainRecordResponse;
import com.aliyun.cdn20180510.models.RefreshObjectCachesRequest;
import com.aliyun.cdn20180510.models.RefreshObjectCachesResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.chestnut.common.cloud.CdnRefreshType;
import com.chestnut.common.cloud.DnsRecord;
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import tools.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component(ICloudProvider.BEAN_PREFIX + AliyunCloudProvider.ID)
public class AliyunCloudProvider implements ICloudProvider {

    private static final long DNS_PAGE_SIZE = 500L;

    public static final String ID = "Aliyun";

    public static final String NAME = "阿里云";

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
        AliyunConfig config = getAliyunConfig(props, "Refresh cdn");
        if (urls.size() > 10) {
            throw new AliyunException("Refresh cdn urls limit 10.");
        }
        if (urls.isEmpty()) {
            throw new AliyunException("Refresh cdn urls cannot be empty.");
        }
        try {
            com.aliyun.cdn20180510.Client client = this.createClient(config.getAccessKey().trim(),
                    config.getSecretKey().trim());
            RefreshObjectCachesRequest req = new RefreshObjectCachesRequest();
            req.setObjectType(this.getObjectType(type));
            req.setObjectPath(String.join("\n", urls));
            req.setForce(false);
            RuntimeOptions runtimeOptions = new RuntimeOptions();
            RefreshObjectCachesResponse res = client.refreshObjectCachesWithOptions(req, runtimeOptions);
            log.debug("AliYun cdn refresh: {}", JacksonUtils.to(res));
        } catch (Exception e) {
            throw new AliyunException("Refresh cdn failed.", e);
        }
    }

    @Override
    public String addDnsRecord(ObjectNode props, String domainName, String rr, String type, String value) {
        AliyunConfig config = getAliyunConfig(props, "Add dns record");
        try {
            com.aliyun.alidns20150109.Client client = this.createDnsClient(config.getAccessKey().trim(),
                    config.getSecretKey().trim());
            AddDomainRecordRequest req = new AddDomainRecordRequest()
                    .setDomainName(domainName)
                    .setRR(rr)
                    .setType(type)
                    .setValue(value);
            AddDomainRecordResponse res = client.addDomainRecordWithOptions(req, new RuntimeOptions());
            String recordId = res.getBody().getRecordId();
            log.debug("AliYun dns add record: {} {} {} -> {}", domainName, rr, type, recordId);
            return recordId;
        } catch (Exception e) {
            throw new AliyunException("Add dns record failed.", e);
        }
    }

    @Override
    public String updateDnsRecord(ObjectNode props, String recordId, String domainName, String rr, String type, String value) {
        AliyunConfig config = getAliyunConfig(props, "Update dns record");
        try {
            com.aliyun.alidns20150109.Client client = this.createDnsClient(config.getAccessKey().trim(),
                    config.getSecretKey().trim());
            UpdateDomainRecordRequest req = new UpdateDomainRecordRequest()
                    .setRecordId(recordId)
                    .setRR(rr)
                    .setType(type)
                    .setValue(value);
            UpdateDomainRecordResponse res = client.updateDomainRecordWithOptions(req, new RuntimeOptions());
            String updatedRecordId = res.getBody().getRecordId();
            log.debug("AliYun dns update record: {} {} {} -> {}", domainName, rr, type, updatedRecordId);
            return updatedRecordId;
        } catch (Exception e) {
            throw new AliyunException("Update dns record failed.", e);
        }
    }

    @Override
    public void deleteDnsRecord(ObjectNode props, String recordId, String domainName) {
        AliyunConfig config = getAliyunConfig(props, "Delete dns record");
        try {
            com.aliyun.alidns20150109.Client client = this.createDnsClient(config.getAccessKey().trim(),
                    config.getSecretKey().trim());
            DeleteDomainRecordRequest req = new DeleteDomainRecordRequest()
                    .setRecordId(recordId);
            client.deleteDomainRecordWithOptions(req, new RuntimeOptions());
            log.debug("AliYun dns delete record: {} -> {}", domainName, recordId);
        } catch (Exception e) {
            throw new AliyunException("Delete dns record failed.", e);
        }
    }

    @Override
    public List<DnsRecord> listDnsRecords(ObjectNode props, String domainName) {
        AliyunConfig config = getAliyunConfig(props, "List dns records");
        try {
            com.aliyun.alidns20150109.Client client = this.createDnsClient(config.getAccessKey().trim(),
                    config.getSecretKey().trim());
            RuntimeOptions runtimeOptions = new RuntimeOptions();
            List<DnsRecord> records = new ArrayList<>();
            long pageNumber = 1;
            long totalCount = Long.MAX_VALUE;
            while (records.size() < totalCount) {
                DescribeDomainRecordsRequest req = new DescribeDomainRecordsRequest()
                        .setDomainName(domainName)
                        .setPageNumber(pageNumber)
                        .setPageSize(DNS_PAGE_SIZE);
                DescribeDomainRecordsResponse res = client.describeDomainRecordsWithOptions(req, runtimeOptions);
                DescribeDomainRecordsResponseBody body = res.getBody();
                totalCount = body.getTotalCount() == null ? 0 : body.getTotalCount();
                List<DescribeDomainRecordsResponseBody.DescribeDomainRecordsResponseBodyDomainRecordsRecord> pageRecords =
                        body.getDomainRecords() == null || body.getDomainRecords().getRecord() == null
                                ? List.of() : body.getDomainRecords().getRecord();
                for (var record : pageRecords) {
                    DnsRecord dnsRecord = new DnsRecord();
                    dnsRecord.setRr(record.getRR());
                    dnsRecord.setType(record.getType());
                    dnsRecord.setValue(record.getValue());
                    dnsRecord.setEnabled(DnsRecord.parseEnableStatus(record.getStatus()));
                    records.add(dnsRecord);
                }
                if (pageRecords.isEmpty()) {
                    break;
                }
                pageNumber++;
            }
            return records;
        } catch (Exception e) {
            throw new AliyunException("List dns records failed.", e);
        }
    }

    private AliyunConfig getAliyunConfig(ObjectNode props, String operation) {
        AliyunConfig config = JacksonUtils.convertValue(props, AliyunConfig.class);
        if (Objects.isNull(config)) {
            throw new AliyunException(operation + " failed, config is null.");
        }
        if (StringUtils.isEmpty(config.getAccessKey())) {
            throw new AliyunException(operation + " failed, Missing access key.");
        }
        if (StringUtils.isEmpty(config.getSecretKey())) {
            throw new AliyunException(operation + " failed, Missing secret key.");
        }
        return config;
    }

    private String getObjectType(CdnRefreshType type) {
        if (CdnRefreshType.DIR == type) {
            return "Directory";
        } else if (CdnRefreshType.REGEX == type) {
            return "Regex";
        }
        return "File";
    }

    private com.aliyun.cdn20180510.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        config.setEndpoint("cdn.aliyuncs.com");
        return new com.aliyun.cdn20180510.Client(config);
    }

    private com.aliyun.alidns20150109.Client createDnsClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        config.setEndpoint("alidns.cn-shanghai.aliyuncs.com");
        return new com.aliyun.alidns20150109.Client(config);
    }
}
