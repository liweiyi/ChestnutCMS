/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.article;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.format.ArticleBodyFormat_RichText;
import com.chestnut.article.service.IArticleService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.contentcore.core.IResourceStat;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * 文章正文资源引用统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(IResourceStat.BEAN_PREFIX + ArticleRichTextResourceStat.TYPE)
public class ArticleRichTextResourceStat implements IResourceStat {

    public static final String TYPE = "ArticleRichText";

    private final IArticleService articleService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void statQuotedResource(Long siteId, Map<Long, Long> quotedResources) throws InterruptedException {
        int pageSize = 100;
        long lastId = 0L;
        int count = 0;
        long total = articleService.dao().lambdaQuery().select(List.of(CmsArticleDetail::getContentId))
                .eq(CmsArticleDetail::getSiteId, siteId)
                .eq(CmsArticleDetail::getFormat, ArticleBodyFormat_RichText.ID)
                .count();
        while (true) {
            LambdaQueryWrapper<CmsArticleDetail> q = new LambdaQueryWrapper<CmsArticleDetail>()
                    .select(List.of(CmsArticleDetail::getContentHtml))
                    .eq(CmsArticleDetail::getSiteId, siteId)
                    .eq(CmsArticleDetail::getFormat, ArticleBodyFormat_RichText.ID)
                    .gt(CmsArticleDetail::getContentId, lastId)
                    .orderByAsc(CmsArticleDetail::getContentId);
            Page<CmsArticleDetail> page = articleService.dao().page(new Page<>(0, pageSize, false), q);
            for (CmsArticleDetail articleDetail : page.getRecords()) {
                AsyncTaskManager.setTaskProgressInfo((int) (count * 100 / total),
                        "正在统计富文本文章正文资源引用：" + count + " / " + total + "]");
                lastId = articleDetail.getContentId();
                // 解析文章正文
                Matcher matcher = InternalUrlUtils.InternalUrlTagPattern.matcher(articleDetail.getContentHtml());
                while (matcher.find()) {
                    String iurl = matcher.group(1);
                    try {
                        InternalURL internalUrl = InternalUrlUtils.parseInternalUrl(iurl);
                        if (Objects.nonNull(internalUrl)) {
                            quotedResources.compute(internalUrl.getId(), (k, v) -> Objects.isNull(v) ? 1 : v + 1);
                        }
                    } catch (Exception e) {
                        log.warn("InternalUrl parse failed: " + iurl, e);
                    }
                }
                AsyncTaskManager.checkInterrupt();
                count++;
            }
            if (page.getRecords().size() < pageSize) {
                break;
            }
        }
    }
}
