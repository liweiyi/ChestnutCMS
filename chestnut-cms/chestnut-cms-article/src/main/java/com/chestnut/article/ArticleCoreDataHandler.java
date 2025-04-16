/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.ICoreDataHandler;
import com.chestnut.contentcore.core.SiteExportContext;
import com.chestnut.contentcore.core.SiteImportContext;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 文章内容核心数据处理器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCoreDataHandler implements ICoreDataHandler {

    private final IArticleService articleService;

    private final Map<String, IArticleBodyFormat> articleBodyFormatMap;

    @Override
    public void onSiteExport(SiteExportContext context) {
        // cms_article_detail
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导出文章详情数据");
        long offset = 0;
        int pageSize = 200;
        int fileIndex = 1;
        while (true) {
            LambdaQueryWrapper<CmsArticleDetail> q = new LambdaQueryWrapper<CmsArticleDetail>()
                    .eq(CmsArticleDetail::getSiteId, context.getSite().getSiteId())
                    .gt(CmsArticleDetail::getContentId, offset)
                    .orderByAsc(CmsArticleDetail::getContentId);
            Page<CmsArticleDetail> page = articleService.dao().page(new Page<>(1, pageSize, false), q);
            if (!page.getRecords().isEmpty()) {
                context.saveData(CmsArticleDetail.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                if (page.getRecords().size() < pageSize) {
                    break;
                }
                offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                fileIndex++;
            } else {
                break;
            }
        }
    }

    @Override
    public void onSiteImport(SiteImportContext context) {
        AsyncTaskManager.setTaskTenPercentProgressInfo("正在导入文章详情数据");
        // cms_article_detail
        List<File> files = context.readDataFiles(CmsArticleDetail.TABLE_NAME);
        files.forEach(file -> {
            List<CmsArticleDetail> list = JacksonUtils.fromList(file, CmsArticleDetail.class);
            for (CmsArticleDetail data : list) {
                Long oldContentId = data.getContentId();
                try {
                    Long contentId = context.getContentIdMap().get(oldContentId);
                    data.setContentId(contentId);
                    data.setSiteId(context.getSite().getSiteId());
                    String contentHtml = data.getContentHtml();
                    // 替换正文内部资源地址
                    StringBuilder html = new StringBuilder();
                    int index = 0;
                    Matcher matcher = InternalUrlUtils.InternalUrlTagPattern.matcher(data.getContentHtml());
                    while (matcher.find()) {
                        String tagStr = matcher.group();
                        String iurl = matcher.group(1);
                        String newIurl = context.dealInternalUrl(iurl);
                        tagStr = StringUtils.replaceEx(tagStr, iurl, newIurl);
                        html.append(contentHtml, index, matcher.start()).append(tagStr);
                        index = matcher.end();
                    }
                    html.append(contentHtml.substring(index));
                    data.setContentHtml(html.toString());
                    if (!articleBodyFormatMap.containsKey(IArticleBodyFormat.BEAN_PREFIX + data.getFormat())) {
                        data.setFormat(ArticleBodyFormat_RichText.ID); // 不支持的文章格式一律设置为富文本格式
                    }
                    articleService.dao().save(data);
                } catch (Exception e) {
                    AsyncTaskManager.addErrMessage("导入文章数据`" + oldContentId + "`失败：" + e.getMessage());
                    log.error("Import article detail failed: {}", oldContentId, e);
                }
            }
        });

    }
}
