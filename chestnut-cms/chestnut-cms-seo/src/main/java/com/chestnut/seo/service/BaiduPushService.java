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
package com.chestnut.seo.service;

import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.HttpUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.seo.properties.BaiduPushAccessSecretProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BaiduUrlPusher
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduPushService {

    private final IContentService contentService;

    private final IPublishPipeService publishPipeService;

    private static final String API = "http://data.zz.baidu.com/urls?site={0}&token={1}";

    public List<BaiduPushResult> pushContents(CmsSite site, List<Long> contentIds) {
        String secret = BaiduPushAccessSecretProperty.getValue(site.getConfigProps());
        Assert.notEmpty(secret, () -> new GlobalException("Baidu push access secret not configured."));

        List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(site.getSiteId());
        List<BaiduPushResult> results = new ArrayList<>(publishPipes.size());
        publishPipes.forEach(pp -> {
            String domain = site.getUrl(pp.getCode());
            if (StringUtils.isEmpty(domain)) {
                return;
            }
            if (domain.contains("://")) {
                domain = StringUtils.substringAfter(domain, "://");
            }
            List<CmsContent> list = contentService.dao().lambdaQuery().in(CmsContent::getContentId, contentIds).list();
            List<String> urls = list.stream().map(content -> contentService
                    .getContentLink(content, 1, pp.getCode(), false)).toList();

            String apiUrl = StringUtils.messageFormat(API, domain, secret);
            String body = StringUtils.join(urls, "\n");
            try {
                String response = HttpUtils.post(URI.create(apiUrl), body, Map.of("Content-Type", "text/plain"));
                BaiduPushResult r = JacksonUtils.from(response, BaiduPushResult.class);
                r.setPublishPipeCode(pp.getCode());
                results.add(r);
            } catch (Exception e) {
                log.error("Publish content to baidu failed!", e);
            }
        });
        return results;
    }

    @Getter
    @Setter
    public static class BaiduPushResult {

        private String publishPipeCode;

        private Integer remain;

        private Integer success;

        private List<String> not_same_site;

        private List<String> not_valid;
    }
}
