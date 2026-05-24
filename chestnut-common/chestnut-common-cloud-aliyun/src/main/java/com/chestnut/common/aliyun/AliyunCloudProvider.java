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

import com.aliyun.cdn20180510.models.RefreshObjectCachesRequest;
import com.aliyun.cdn20180510.models.RefreshObjectCachesResponse;
import com.aliyun.credentials.Client;
import com.aliyun.credentials.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.chestnut.common.cloud.CdnRefreshType;
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component(ICloudProvider.BEAN_PREFIX + AliyunCloudProvider.ID)
public class AliyunCloudProvider implements ICloudProvider {

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
        String appSecret = newProps.get("secretKey").asText();
        if ("******".equals(appSecret)) {
            newProps.put("secretKey", oldProps.get("secretKey").asText());
        }
    }

    @Override
    public void refreshCdn(ObjectNode props, CdnRefreshType type, List<String> urls) {
        AliyunConfig config = JacksonUtils.convertValue(props, AliyunConfig.class);
        if (Objects.isNull(config)) {
            throw new AliyunException("Refresh cdn failed, config is null.");
        }
        if (StringUtils.isEmpty(config.getAccessKey())) {
            throw new AliyunException("Refresh cdn failed, Missing access key.");
        }
        if (StringUtils.isEmpty(config.getSecretKey())) {
            throw new AliyunException("Refresh cdn failed, Missing secret key.");
        }
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
}
