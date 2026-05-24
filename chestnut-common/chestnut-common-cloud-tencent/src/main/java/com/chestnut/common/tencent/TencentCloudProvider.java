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
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tencentcloudapi.cdn.v20180606.CdnClient;
import com.tencentcloudapi.cdn.v20180606.models.PurgePathCacheRequest;
import com.tencentcloudapi.cdn.v20180606.models.PurgePathCacheResponse;
import com.tencentcloudapi.cdn.v20180606.models.PurgeUrlsCacheRequest;
import com.tencentcloudapi.cdn.v20180606.models.PurgeUrlsCacheResponse;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component(ICloudProvider.BEAN_PREFIX + TencentCloudProvider.ID)
public class TencentCloudProvider implements ICloudProvider {

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
        String appSecret = newProps.get("secretKey").asText();
        if ("******".equals(appSecret)) {
            newProps.put("secretKey", oldProps.get("secretKey").asText());
        }
    }

    @Override
    public void refreshCdn(ObjectNode props, CdnRefreshType type, List<String> urls) {
        TencentConfig config = JacksonUtils.convertValue(props, TencentConfig.class);
        if (Objects.isNull(config)) {
            throw new GlobalException("Refresh cdn failed, config is null.");
        }
        if (StringUtils.isEmpty(config.getSecretId())) {
            throw new GlobalException("Refresh cdn failed, Missing secret id.");
        }
        if (StringUtils.isEmpty(config.getSecretKey())) {
            throw new GlobalException("Refresh cdn failed, Missing secret key.");
        }
        if (CdnRefreshType.REGEX == type) {
            throw new GlobalException("Refresh cdn failed, Tencent Cloud CDN does not support regex refresh.");
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
