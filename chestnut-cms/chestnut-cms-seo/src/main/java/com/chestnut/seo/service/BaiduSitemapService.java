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
package com.chestnut.seo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.impl.CatalogType_Common;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.seo.fixed.dict.SitemapPageType;
import com.chestnut.seo.publishpipe.PublishPipeProp_EnableSitemap;
import com.chestnut.seo.publishpipe.PublishPipeProp_SitemapPageType;
import com.chestnut.seo.publishpipe.PublishPipeProp_SitemapUrlLimit;
import com.chestnut.system.fixed.dict.YesOrNo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 百度站点地图服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaiduSitemapService {

    /**
     * 文件名前缀
     */
    static final String SitemapFileNamePrefix = "sitemap_";

    static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IContentService contentService;

    private final IPublishPipeService publishPipeService;

    private final AsyncTaskManager asyncTaskManager;

    /**
     * 接口推送
     */
    public void pushUrlToBaidu() {

    }

    public AsyncTask asyncGenerateSitemapXml(final CmsSite site) {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            public void run0() {
                generateSitemapXml(site);
            }
        };
        asyncTask.setTaskId("Sitemap-" + site.getSiteId());
        asyncTask.setType("Sitemap");
        this.asyncTaskManager.execute(asyncTask);
        return asyncTask;
    }

    public void generateSitemapXml() {
        this.siteService.list().forEach(this::generateSitemapXml);
    }

    public void generateSitemapXml(CmsSite site) {
        List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId())
                .stream().filter(pp ->
                    PublishPipeProp_EnableSitemap.getValue(pp.getCode(), site.getPublishPipeProps())
                ).toList();
        generateSitemapXml(site, publishPipes);
    }

    public void generateSitemapXml(CmsSite site, List<CmsPublishPipe> publishPipes) {
        long s = System.currentTimeMillis();
        List<CmsCatalog> catalogs = this.catalogService.lambdaQuery()
                .eq(CmsCatalog::getSiteId, site.getSiteId())
                .eq(CmsCatalog::getVisibleFlag, YesOrNo.YES)
                .eq(CmsCatalog::getStaticFlag, YesOrNo.YES)
                .eq(CmsCatalog::getCatalogType, CatalogType_Common.ID)
                .list();
        // 创建sitemap_?.xml，忽略复制内容和链接内容
        Map<String, SiteMapXmlCreator> creatorMap = publishPipes.stream()
                .collect(Collectors.toMap(CmsPublishPipe::getCode, pp -> {
                    String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
                    int urlLimit = PublishPipeProp_SitemapUrlLimit.getValue(pp.getCode(), site.getPublishPipeProps());
                    String pageType = PublishPipeProp_SitemapPageType.getValue(pp.getCode(), site.getPublishPipeProps());
                    return new SiteMapXmlCreator(urlLimit, pageType, siteRoot);
                }));
        // 站点链接
        AsyncTaskManager.setTaskProgressInfo(5, site.getName());
        creatorMap.forEach((code, creator) -> {
            String url = site.getUrl(code);
            creator.append(url, "1", LocalDateTime.now().format(FORMAT));
        });
        // 栏目链接
        catalogs.forEach(catalog -> {
            creatorMap.forEach((code, creator) -> {
                String url = catalogService.getCatalogLink(catalog, 1, code, false);
                creator.append(url, "0.8", LocalDateTime.now().format(FORMAT));
                AsyncTaskManager.setTaskProgressInfo(10, catalog.getName());
            });
        });
        // 内容链接
        int count = 0; // 计数
        Long total = this.contentService.dao().lambdaQuery()
                .eq(CmsContent::getSiteId, site.getSiteId())
                .eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
                .eq(CmsContent::getCopyType, 0)
                .ne(CmsContent::getLinkFlag, YesOrNo.YES)
                .count();
        int pageSize = 1000;
        for (CmsCatalog catalog : catalogs) {
            long lastContentId = 0;
            while (true) {
                LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
                        .eq(CmsContent::getCatalogId, catalog.getCatalogId())
                        .eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
                        .eq(CmsContent::getCopyType, 0)
                        .ne(CmsContent::getLinkFlag, YesOrNo.YES)
                        .gt(CmsContent::getContentId, lastContentId)
                        .orderByAsc(CmsContent::getContentId);
                Page<CmsContent> page = contentService.dao().page(new Page<>(0, pageSize, false), q);
                for (CmsContent content : page.getRecords()) {
                    lastContentId = content.getContentId();
                    String lastmod = content.getPublishDate().format(FORMAT);
                    creatorMap.forEach((code, creator) -> {
                        String url = contentService.getContentLink(content, 1, code, false);
                        creator.append(url, "0.8", lastmod);
                    });
                    count++;
                    AsyncTaskManager.setTaskProgressInfo(count * 90 / total.intValue(), content.getTitle());
                }
                if (page.getRecords().size() < pageSize) {
                    break;
                }
            }
        }
        // 剩余链接写入文件
        creatorMap.forEach((code, creator) -> creator.writeFile());
        // 创建sitemapindex.xml
        publishPipes.forEach(pp -> {
            try {
                generateSitemapIndexXml(site, pp.getCode());
            } catch (IOException e) {
                log.error("生成站点地图索引文件失败", e);
            }
        });
        AsyncTaskManager.setTaskProgressInfo(100, "Sitemap生成完成");
        log.info("站点地图生成完成，耗时：{}", System.currentTimeMillis() - s);
    }

    /**
     * 生成Sitemap索引文件
     */
    public void generateSitemapIndexXml(CmsSite site, String publishPipeCode) throws IOException {
        String siteUrl = site.getUrl(publishPipeCode);
        String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
        File[] files = new File(siteRoot).listFiles(f -> f.getName().startsWith(SitemapFileNamePrefix));
        if (files != null)  {
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            sb.append("<sitemapindex>");
            for (File file : files) {
                sb.append("<sitemap>");
                sb.append("<loc>").append(siteUrl).append(file.getName()).append("</loc>");
                String lastmod = DateUtils.epochMilliToLocalDateTime(file.lastModified()).format(FORMAT);
                sb.append("<lastmod>").append(lastmod).append("</lastmod>");
                sb.append("</sitemap>");
            }
            sb.append("</sitemapindex>");
            String path = siteRoot + "sitemapindex.xml";
            FileUtils.writeStringToFile(new File(path), sb.toString(), StandardCharsets.UTF_8);
            log.info("站点地图索引文件：[{}]{}->{}", publishPipeCode, site.getName(), path);
        }
    }

    static class SiteMapXmlCreator {

        private final int urlLimit;

        private final String pageType;

        private final String dir;

        private int index = 1;

        private int count;

        private final StringBuilder sb = new StringBuilder();

        SiteMapXmlCreator(int urlLimit, String pageType, String dir) {
            this.urlLimit = urlLimit;
            this.pageType = pageType;
            this.dir = dir;
        }

        void append(String loc, String priority, String lastmod) {
            sb.append("<url>");
            sb.append("<loc>").append(loc).append("</loc>");
            if (StringUtils.isNotEmpty(pageType) && !SitemapPageType.PC.equals(pageType)) {
                sb.append("<mobile:mobile type=\"").append(pageType).append("\" />");
            }
            sb.append("<priority>").append(priority).append("</priority>");
            sb.append("<lastmod>").append(lastmod).append("</lastmod>");
            sb.append("</url>");
            count++;
            if (count >= urlLimit) {
                writeFile();
                count = 0;
                index++;
                sb.setLength(0);
            }
        }

        void writeFile() {
            sb.insert(0, "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" xmlns:mobile=\"http://www.baidu.com/schemas/sitemap-mobile/1/\">");
            sb.append("</urlset>");
            String path = dir + SitemapFileNamePrefix + index + ".xml";
            try {
                File file = new File(path);
                FileUtils.writeStringToFile(file, sb.toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("生成站点地图文件失败：" + path, e);
            }
        }
    }
}
