package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.*;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.domain.*;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import jodd.io.ZipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 站点导入导出
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SiteThemeService {

    private final AsyncTaskManager asyncTaskManager;

    private final IPublishPipeService publishPipeService;

    private final ISiteService siteService;

    private final ISitePropertyService sitePropertyService;

    private final ICatalogService catalogService;

    private final IPageWidgetService pageWidgetService;

    private final IResourceService resourceService;

    private final IContentService contentService;

    private final List<ICoreDataHandler> contentCoreHandlers;

    public AsyncTask importSiteTheme(CmsSite site, final File zipFile, LoginUser operator) throws IOException {
        // TODO 校验数据，必须无栏目、内容、页面部件等数据的站点才能导入
        AsyncTask asyncTask = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                SiteImportContext context = new SiteImportContext(site);
                context.setOperator(operator.getUsername());
                // 解压导入zip包
                AsyncTaskManager.setTaskProgressInfo(10, "正在解压");
                String destDir = SiteUtils.getSiteResourceRoot(site) + SiteImportContext.ImportDir;
                ZipUtil.unzip(zipFile, new File(destDir));
                try {
                    List<File> files = context.readDataFiles(CmsSite.TABLE_NAME);
                    CmsSite sourceSite = JacksonUtils.from(files.get(0), CmsSite.class);
                    context.setSourceSite(sourceSite);
                    // 导入站点扩展属性
                    AsyncTaskManager.setTaskProgressInfo(5, "正在导入站点扩展属性数据");
                    files = context.readDataFiles(CmsSiteProperty.TABLE_NAME);
                    files.forEach(file -> {
                        List<CmsSiteProperty> list = JacksonUtils.fromList(file, CmsSiteProperty.class);
                        list.forEach(data -> {
                            Long propertyId = data.getPropertyId();
                            try {
                                data.setPropertyId(IdUtils.getSnowflakeId());
                                data.setSiteId(site.getSiteId());
                                data.createBy(context.getOperator());
                                sitePropertyService.save(data);
                            } catch (Exception e) {
                                this.addErrorMessage("导入站点扩展属性数据失败：" + propertyId);
                                e.printStackTrace();
                            }
                        });
                    });
                    // 导入素材数据
                    AsyncTaskManager.setTaskProgressInfo(20, "正在导入资源数据");
                    files = context.readDataFiles(CmsResource.TABLE_NAME);
                    files.forEach(file -> {
                        List<CmsResource> list = JacksonUtils.fromList(file, CmsResource.class);
                        list.forEach(data -> {
                            Long oldResource = data.getResourceId();
                            try {
                                data.setResourceId(IdUtils.getSnowflakeId());
                                data.setSiteId(site.getSiteId());
                                data.createBy(context.getOperator());
                                resourceService.save(data);
                                context.getResourceIdMap().put(oldResource, data.getResourceId());
                            } catch (Exception e) {
                                this.addErrorMessage("导入素材资源数据失败：" + oldResource);
                                e.printStackTrace();
                            }
                        });
                    });
                    // 处理站点数据
                    site.setConfigProps(sourceSite.getConfigProps());
                    site.setPublishPipeProps(sourceSite.getPublishPipeProps());
                    site.setResourceUrl(sourceSite.getResourceUrl());
                    site.setDescription(sourceSite.getDescription());
                    site.setSeoTitle(sourceSite.getSeoTitle());
                    site.setSeoKeywords(sourceSite.getSeoKeywords());
                    site.setSeoDescription(sourceSite.getSeoDescription());
                    site.setLogo(context.dealInternalUrl(sourceSite.getLogo()));
                    siteService.updateById(site);
                    siteService.clearCache(site.getSiteId());
                    // 导入栏目数据
                    List<CmsCatalog> linkCatalogs = new ArrayList<>();
                    files = context.readDataFiles(CmsCatalog.TABLE_NAME);
                    files.forEach(file -> {
                        List<CmsCatalog> list = JacksonUtils.fromList(file, CmsCatalog.class);
                        // 必须保证顺序
                        list.sort(Comparator.comparing(CmsCatalog::getAncestors));
                        list.forEach(data -> {
                            try {
                                Long sourceCatalogId = data.getCatalogId();
                                data.setCatalogId(IdUtils.getSnowflakeId());
                                data.setSiteId(site.getSiteId());
                                data.setAncestors(data.getCatalogId().toString());
                                if (IdUtils.validate(data.getParentId())) {
                                    Long pCatalogId = context.getCatalogIdMap().get(data.getParentId());
                                    CmsCatalog parent = catalogService.getCatalog(pCatalogId);
                                    if (Objects.nonNull(parent)) {
                                        data.setParentId(parent.getCatalogId());
                                        data.setAncestors(CatalogUtils.getCatalogAncestors(parent, data.getCatalogId()));
                                    } else {
                                        data.setParentId(0L);
                                    }
                                }
                                data.createBy(context.getOperator());
                                // 处理logo
                                data.setLogo(context.dealInternalUrl(data.getLogo()));
                                catalogService.save(data);
                                context.getCatalogIdMap().put(sourceCatalogId, data.getCatalogId());
                                if (CatalogType_Link.ID.equals(data.getCatalogType())) {
                                    linkCatalogs.add(data);
                                }
                            } catch (Exception e) {
                                this.addErrorMessage("导入栏目数据失败：" + data.getName());
                                e.printStackTrace();
                            }
                        });
                    });
                    AsyncTaskManager.setTaskProgressInfo(30, "正在导入发布通道数据");
                    files = context.readDataFiles(CmsPublishPipe.TABLE_NAME);
                    files.forEach(file -> {
                        List<CmsPublishPipe> list = JacksonUtils.fromList(file, CmsPublishPipe.class);
                        for (CmsPublishPipe data : list) {
                            try {
                                data.setPublishpipeId(IdUtils.getSnowflakeId());
                                data.setSiteId(site.getSiteId());
                                data.setCreateBy(operator.getUsername());
                                publishPipeService.addPublishPipe(data);
                            } catch (Exception e) {
                                this.addErrorMessage("导入发布通道数据失败：" + data.getName());
                                e.printStackTrace();
                            }
                        }
                    });
                    AsyncTaskManager.setTaskProgressInfo(50, "正在导入页面部件数据");
                    files = context.readDataFiles(CmsPageWidget.TABLE_NAME);
                    files.forEach(file -> {
                        List<CmsPageWidget> list = JacksonUtils.fromList(file, CmsPageWidget.class);
                        list.forEach(data -> {
                            try {
                                Long oldPageWidgetId = data.getPageWidgetId();
                                data.setPageWidgetId(IdUtils.getSnowflakeId());
                                data.setSiteId(site.getSiteId());
                                if (IdUtils.validate(data.getCatalogId())) {
                                    Long catalogId = context.getCatalogIdMap().get(data.getCatalogId());
                                    CmsCatalog catalog = catalogService.getCatalog(catalogId);
                                    if (Objects.nonNull(catalog)) {
                                        data.setCatalogId(catalog.getCatalogId());
                                        data.setCatalogAncestors(catalog.getAncestors());
                                    } else {
                                        this.addErrorMessage("页面部件栏目关联失败：" + data.getName());
                                    }
                                }
                                data.createBy(context.getOperator());
                                pageWidgetService.save(data);
                                context.getPageWidgetIdMap().put(oldPageWidgetId, data.getPageWidgetId());
                            } catch (Exception e) {
                                this.addErrorMessage("导入页面部件数据失败：" + data.getName());
                                e.printStackTrace();
                            }
                        });
                    });
                    // 导入内容数据
                    List<CmsContent> linkContents = new ArrayList<>();
                    AsyncTaskManager.setTaskProgressInfo(50, "正在导入内容数据");
                    files = context.readDataFiles(CmsContent.TABLE_NAME);
                    files.forEach(file -> {
                        List<CmsContent> list = JacksonUtils.fromList(file, CmsContent.class);
                        list.forEach(content -> {
                            Long sourceContentId = content.getContentId();
                            try {
                                CmsCatalog catalog = catalogService.getCatalog(context.getCatalogIdMap().get(content.getCatalogId()));

                                content.setContentId(IdUtils.getSnowflakeId());
                                content.setSiteId(site.getSiteId());
                                content.setCatalogId(catalog.getCatalogId());
                                content.setCatalogAncestors(catalog.getAncestors());
                                String topCatalogIdStr = catalog.getAncestors().split(CatalogUtils.ANCESTORS_SPLITER)[0];
                                content.setTopCatalog(Long.valueOf(topCatalogIdStr));
                                content.setDeptId(0L);
                                content.setDeptCode(StringUtils.EMPTY);
                                content.createBy(operator.getUsername());
                                // 处理logo
                                content.setLogo(context.dealInternalUrl(content.getLogo()));
                                contentService.save(content);
                                context.getContentIdMap().put(sourceContentId, content.getContentId());
                                if (content.isLinkContent()) {
                                    linkContents.add(content);
                                }
                            } catch (Exception e) {
                                this.addErrorMessage("导入内容数据失败：" + sourceContentId);
                                e.printStackTrace();
                            }
                        });
                    });
                    // 处理链接栏目的内部链接地址
                    linkCatalogs.forEach(catalog -> {
                        String iurl = context.dealInternalUrl(catalog.getRedirectUrl());
                        catalogService.lambdaUpdate().set(CmsCatalog::getRedirectUrl, iurl)
                                .eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
                    });
                    linkContents.forEach(content -> {
                        String iurl = context.dealInternalUrl(content.getRedirectUrl());
                        contentService.lambdaUpdate().set(CmsContent::getRedirectUrl, iurl)
                                .eq(CmsContent::getContentId, content.getContentId()).update();
                    });
                    // 其他模块数据处理
                    contentCoreHandlers.forEach(h -> h.onSiteImport(context));
                    // 复制文件
                    context.copySiteFiles();
                } finally {
                    FileUtils.deleteDirectory(new File(destDir));
                    this.setProgressInfo(100, "导入完成");
                }
            }
        };
        asyncTask.setType("SiteTheme");
        asyncTask.setTaskId("SiteThemeImport-" + site.getSiteId());
        this.asyncTaskManager.execute(asyncTask);
        return asyncTask;
    }

    public static final String ThemeZipPath = "_export/SiteTheme.zip";

    public AsyncTask exportSiteTheme(CmsSite site) {
        AsyncTask asyncTask = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
                SiteExportContext context = new SiteExportContext(site);
                // cms_site
                {
                    AsyncTaskManager.setTaskProgressInfo(0, "正在导出站点数据");
                    String json = JacksonUtils.to(site);
                    context.saveData(CmsSite.TABLE_NAME, json);
                    // 记录资源引用
                    InternalURL internalURL = InternalUrlUtils.parseInternalUrl(site.getLogo());
                    if (Objects.nonNull(internalURL)) {
                        context.getResourceIds().add(internalURL.getId());
                    }
                }
                // cms_site_property
                {
                    AsyncTaskManager.setTaskProgressInfo(5, "正在导出站点扩展属性数据");
                    List<CmsSiteProperty> list = sitePropertyService.lambdaQuery()
                            .eq(CmsSiteProperty::getSiteId, site.getSiteId())
                            .list();
                    String json = JacksonUtils.to(list);
                    context.saveData(CmsSiteProperty.TABLE_NAME, json);
                }
                List<String> catalogPaths = new ArrayList<>();
                // cms_catalog
                {
                    AsyncTaskManager.setTaskProgressInfo(30, "正在导出栏目数据");
                    List<CmsCatalog> list = catalogService.lambdaQuery()
                            .eq(CmsCatalog::getSiteId, site.getSiteId())
                            .orderByAsc(CmsCatalog::getAncestors) // 必须保证顺序
                            .list();
                    String json = JacksonUtils.to(list);
                    context.saveData(CmsCatalog.TABLE_NAME, json);
                    list.forEach(catalog -> {
                        InternalURL internalURL = InternalUrlUtils.parseInternalUrl(catalog.getLogo());
                        if (Objects.nonNull(internalURL)) {
                            context.getResourceIds().add(internalURL.getId());
                        }
                        catalogPaths.add(catalog.getPath());
                    });
                }
                // cms_page_widget
                {
                    AsyncTaskManager.setTaskProgressInfo(40, "正在导出页面部件数据");
                    List<CmsPageWidget> list = pageWidgetService.lambdaQuery()
                            .eq(CmsPageWidget::getSiteId, site.getSiteId())
                            .list();
                    String json = JacksonUtils.to(list);
                    context.saveData(CmsPageWidget.TABLE_NAME, json);
                }
                // cms_content
                {
                    AsyncTaskManager.setTaskProgressInfo(40, "正在导出内容数据");
                    long offset = 0;
                    int pageSize = 200;
                    int fileIndex = 1;
                    while (true) {
                        LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
                                .eq(CmsContent::getSiteId, site.getSiteId())
                                .gt(CmsContent::getContentId, offset)
                                .orderByAsc(CmsContent::getContentId);
                        Page<CmsContent> page = contentService.page(new Page<>(1, pageSize, false), q);
                        if (page.getRecords().size() > 0) {
                            context.saveData(CmsContent.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                            offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                            fileIndex++;
                            page.getRecords().forEach(content -> {
                                InternalURL internalURL = InternalUrlUtils.parseInternalUrl(content.getLogo());
                                if (Objects.nonNull(internalURL)) {
                                    context.getResourceIds().add(internalURL.getId());
                                }
                            });
                            if (page.getRecords().size() < pageSize) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                // 导出关联资源cms_resource
                {
                    AsyncTaskManager.setTaskProgressInfo(40, "正在导出素材数据");
                    long offset = 0;
                    int pageSize = 200;
                    int fileIndex = 1;
                    while (true) {
                        LambdaQueryWrapper<CmsResource> q = new LambdaQueryWrapper<CmsResource>()
                                .eq(CmsResource::getSiteId, site.getSiteId())
                                .gt(CmsResource::getResourceId, offset)
                                .orderByAsc(CmsResource::getResourceId);
                        Page<CmsResource> page = resourceService.page(new Page<>(1, pageSize, false), q);
                        if (page.getRecords().size() > 0) {
                            context.saveData(CmsResource.TABLE_NAME, JacksonUtils.to(page.getRecords()), fileIndex);
                            offset = page.getRecords().get(page.getRecords().size() - 1).getResourceId();
                            fileIndex++;
                            if (page.getRecords().size() < pageSize) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                // cms_publishpipe
                AsyncTaskManager.setTaskProgressInfo(20, "正在导出发布通道数据");
                List<CmsPublishPipe> publishPipes = publishPipeService.lambdaQuery()
                        .eq(CmsPublishPipe::getSiteId, site.getSiteId())
                        .list();
                context.saveData(CmsPublishPipe.TABLE_NAME, JacksonUtils.to(publishPipes));
                // 发布通道模板资源保存到导出临时目录
                final String resourceRoot = CMSConfig.getResourceRoot();
                publishPipes.forEach(pp -> {
                    String ppPath = SiteUtils.getSitePublishPipePath(site.getPath(), pp.getCode());
                    File[] files = new File(resourceRoot + ppPath).listFiles((dir, name) -> {
                        // 过滤掉栏目目录和include目录
                        for (String catalogPath : catalogPaths) {
                            if (catalogPath.startsWith(name + "/") || "include".equals(name)) {
                                return false;
                            }
                        }
                        return true;
                    });
                    if (files != null) {
                        for (File file : files) {
                            context.saveFile(file, file.getAbsolutePath().substring(resourceRoot.length()));
                        }
                    }
                });
                // 素材资源目录复制到导出临时目录
                context.saveFile(new File(siteResourceRoot + IResourceType.UploadResourceDirectory),
                        SiteUtils.getSiteResourcePath(site.getPath()) + IResourceType.UploadResourceDirectory);
                // 其他模块数据处理
                contentCoreHandlers.forEach(h -> h.onSiteExport(context));
                // ############ 导出压缩文件 #################
                context.createZipFile(ThemeZipPath);
//                context.clearTempFiles();
                AsyncTaskManager.setTaskProgressInfo(100, "导出成功");
            }
        };
        asyncTask.setType("SiteTheme");
        asyncTask.setTaskId("SiteThemeExport-" + site.getSiteId());
        this.asyncTaskManager.execute(asyncTask);
        return asyncTask;
    }
}
