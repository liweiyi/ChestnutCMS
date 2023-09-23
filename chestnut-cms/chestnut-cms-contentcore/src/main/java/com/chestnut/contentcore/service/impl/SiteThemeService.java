package com.chestnut.contentcore.service.impl;

import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IPageWidget;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.core.impl.InternalDataType_Site;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.CatalogAddDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.listener.event.SiteThemeExportEvent;
import com.chestnut.contentcore.listener.event.SiteThemeImportEvent;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import jodd.io.ZipBuilder;
import jodd.io.ZipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站点导入导出
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SiteThemeService implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final AsyncTaskManager asyncTaskManager;

    private final IPublishPipeService publishPipeService;

    private final ICatalogService catalogService;

    private final IPageWidgetService pageWidgetService;

    public AsyncTask importSiteTheme(CmsSite site, final File zipFile, LoginUser operator) throws IOException {
        String taskId = "SiteThemeImport-" + site.getPath();
        AsyncTask task = asyncTaskManager.getTask(taskId);
        if (task != null) {
            if (!task.isAlive()) {
                asyncTaskManager.removeById(task.getTaskId());
            } else {
                throw ContentCoreErrorCode.SITE_EXPORT_TASK_EXISTS.exception();
            }
        }

        AsyncTask asyncTask = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                // 解压导入zip包
                AsyncTaskManager.setTaskProgressInfo(10, "正在解压");
                String destDir = SiteUtils.getSiteResourceRoot(site) + "siteTheme/";
                ZipUtil.unzip(zipFile, new File(destDir));
                try {
                    // 校验可能冲突的数据
                    Map<Long, CmsCatalog> catalogIdMap = new HashMap<>(); // <原ID, 新ID>
                    // 导入数据
                    AsyncTaskManager.setTaskProgressInfo(20, "正在导入栏目数据");
                    File file = new File(destDir + "db/" + CmsCatalog.TABLE_NAME + ".json");
                    if (file.exists()) {
                        List<CmsCatalog> list = JacksonUtils.fromList(file, CmsCatalog.class);
                        list.sort(Comparator.comparing(CmsCatalog::getAncestors));
                        list.forEach(data -> {
                            try {
                                CatalogAddDTO dto = new CatalogAddDTO();
                                dto.setSiteId(site.getSiteId());
                                dto.setName(data.getName());
                                dto.setPath(data.getPath());
                                dto.setAlias(data.getAlias());
                                dto.setCatalogType(data.getCatalogType());
                                if (data.getParentId() > 0) {
                                    dto.setParentId(catalogIdMap.get(data.getParentId()).getCatalogId());
                                }
                                dto.setOperator(operator);
                                CmsCatalog catalog = catalogService.addCatalog(dto);
                                catalog.setConfigProps(data.getConfigProps());
                                catalog.setPublishPipeProps(data.getPublishPipeProps());
                                catalog.setDescription(data.getDescription());
                                catalog.setSeoTitle(data.getSeoTitle());
                                catalog.setSeoKeywords(data.getSeoKeywords());
                                catalog.setSeoDescription(data.getSeoDescription());
                                if (CatalogType_Link.ID.equals(catalog.getCatalogType())) {
                                    // 链接栏目统一设置成站点链接
                                    catalog.setRedirectUrl(InternalUrlUtils.getInternalUrl(InternalDataType_Site.ID, site.getSiteId()));
                                }
                                catalogService.updateById(catalog);
                                catalogIdMap.put(data.getCatalogId(), catalog);
                            } catch (Exception e) {
                                this.addErrorMessage("栏目添加失败：" + data.getName() + " > " + e.getMessage());
                            }
                        });
                    }
                    AsyncTaskManager.setTaskProgressInfo(30, "正在导入发布通道数据");
                    file = new File(destDir + "db/" + CmsPublishPipe.TABLE_NAME + ".json");
                    if (file.exists()) {
                        List<CmsPublishPipe> list = JacksonUtils.fromList(file, CmsPublishPipe.class);
                        for (CmsPublishPipe data : list) {
                            try {
                                data.setSiteId(site.getSiteId());
                                data.setCreateBy(operator.getUsername());
                                publishPipeService.addPublishPipe(data);
                            } catch (Exception e) {
                                this.addErrorMessage("发布通道添加失败：" + data.getName() + " > " + e.getMessage());
                            }
                        }
                    }
                    AsyncTaskManager.setTaskProgressInfo(50, "正在导入页面部件数据");
                    file = new File(destDir + "db/" + CmsPageWidget.TABLE_NAME + ".json");
                    if (file.exists()) {
                        List<CmsPageWidget> list = JacksonUtils.fromList(file, CmsPageWidget.class);
                        list.forEach(data -> {
                            try {
                                IPageWidgetType pwt = ContentCoreUtils.getPageWidgetType(data.getType());
                                IPageWidget pw = pwt.newInstance();
                                pw.setPageWidgetEntity(data);
                                pw.setOperator(operator);
                                if (IdUtils.validate(data.getCatalogId())) {
                                    CmsCatalog catalog = catalogIdMap.get(data.getCatalogId());
                                    pw.getPageWidgetEntity().setCatalogId(catalog.getCatalogId());
                                    pw.getPageWidgetEntity().setCatalogAncestors(catalog.getAncestors());
                                }
                                pw.getPageWidgetEntity().setSiteId(site.getSiteId());
                                pw.add();
                            } catch (Exception e) {
                                this.addErrorMessage("页面部件添加失败：" + data.getName() + " > " + e.getMessage());
                            }
                        });
                    }
                    // 复制文件
                    AsyncTaskManager.setTaskProgressInfo(50, "正在导入发布通道相关文件");
                    String sitePath = FileUtils.readFileToString(new File(destDir + "site.txt"), Charset.defaultCharset());
                    File wwwroot = new File(destDir + "wwwroot");
                    File[] wwwrootFiles = wwwroot.listFiles();
                    if (wwwrootFiles != null) {
                        for (File f : wwwrootFiles) {
                            String dest = CMSConfig.getResourceRoot() + f.getName().replaceFirst(sitePath, site.getPath());
                            FileUtils.copyDirectory(f, new File(dest));
                        }
                    }
                    applicationContext.publishEvent(new SiteThemeImportEvent(this, site, destDir, operator));
                } finally {
                    FileUtils.deleteDirectory(new File(destDir));
                    this.setProgressInfo(100, "导入完成");
                }
            }
        };
        asyncTask.setTaskId(taskId);
        asyncTask.setType("SiteThemeImport");
        this.asyncTaskManager.execute(asyncTask);
        return asyncTask;
    }

    public static final String ThemeFileName = "SiteTheme.zip";

    public AsyncTask exportSiteTheme(CmsSite site, final List<String> directories) {
        String taskId = "SiteExportTheme-" + site.getPath();
        AsyncTask task = asyncTaskManager.getTask(taskId);
        if (task != null) {
            if (!task.isAlive()) {
                asyncTaskManager.removeById(task.getTaskId());
            } else {
                throw ContentCoreErrorCode.SITE_EXPORT_TASK_EXISTS.exception();
            }
        }

        AsyncTask asyncTask = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
                String zipFile = siteResourceRoot + ThemeFileName;
                ZipBuilder zipBuilder = ZipBuilder.createZipFile(new File(zipFile));
                // 记录下原站点目录
                zipBuilder.add(site.getPath()).path("site.txt").save();
                // 发布通道文件
                AsyncTaskManager.setTaskProgressInfo(10, "正在导出站点相关文件");
                final String prefix = "wwwroot/";
                for (int i = 0; i < directories.size(); i++) {
                    String directory = directories.get(i);
                    File files = new File(CMSConfig.getResourceRoot() + directory);
                    if (files.exists()) {
                        zipBuilder.add(files).path(prefix + directory).recursive().save();
                    }
                }
                // 仅导出发布通道、栏目和页面部件基础数据
                // cms_publishpipe
                {
                    AsyncTaskManager.setTaskProgressInfo(20, "正在导出发布通道数据");
                    List<CmsPublishPipe> list = publishPipeService.lambdaQuery()
                            .eq(CmsPublishPipe::getSiteId, site.getSiteId())
                            .list();
                    String json = JacksonUtils.to(list);
                    zipBuilder.add(json.getBytes(StandardCharsets.UTF_8))
                            .path("db/" + CmsPublishPipe.TABLE_NAME + ".json")
                            .save();
                }
                // cms_catalog
                {
                    AsyncTaskManager.setTaskProgressInfo(30, "正在导出栏目数据");
                    List<CmsCatalog> list = catalogService.lambdaQuery()
                            .eq(CmsCatalog::getSiteId, site.getSiteId())
                            .orderByAsc(CmsCatalog::getAncestors)
                            .list();
                    String json = JacksonUtils.to(list);
                    zipBuilder.add(json.getBytes(StandardCharsets.UTF_8))
                            .path("db/" + CmsCatalog.TABLE_NAME + ".json")
                            .save();
                }
                // cms_page_widget
                {
                    AsyncTaskManager.setTaskProgressInfo(40, "正在导出页面部件数据");
                    List<CmsPageWidget> list = pageWidgetService.lambdaQuery()
                            .eq(CmsPageWidget::getSiteId, site.getSiteId())
                            .list();
                    String json = JacksonUtils.to(list);
                    zipBuilder.add(json.getBytes(StandardCharsets.UTF_8))
                            .path("db/" + CmsPageWidget.TABLE_NAME + ".json")
                            .save();
                }
                applicationContext.publishEvent(new SiteThemeExportEvent(this, site, zipBuilder));
                zipBuilder.toZipFile();
                AsyncTaskManager.setTaskProgressInfo(100, "导出成功");
            }
        };
        asyncTask.setTaskId(taskId);
        asyncTask.setType("SiteExportTheme");
        this.asyncTaskManager.execute(asyncTask);
        return asyncTask;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
