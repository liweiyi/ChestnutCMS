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
package com.chestnut.contentcore.controller;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.core.impl.ResourceType_Image;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.config.AllowUploadFileType;
import com.chestnut.system.annotation.IgnoreDemoMode;


import com.chestnut.contentcore.core.IProperty.UseType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PublishSiteDTO;
import com.chestnut.contentcore.domain.dto.SiteDTO;
import com.chestnut.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.chestnut.contentcore.domain.dto.SiteExportDTO;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.contentcore.domain.vo.SiteDashboardVO;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.perms.SitePermissionType.SitePrivItem;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.service.impl.SiteThemeService;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import freemarker.template.TemplateException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 站点管理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CMS.SITE.MODULE}")
@RestController
@RequestMapping("/cms/site")
@RequiredArgsConstructor
public class SiteController extends CmsRestController {

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    private final IPublishService publishService;

    private final AsyncTaskManager asyncTaskManager;

    private final SiteThemeService siteExportService;

    private final IResourceService resourceService;

    /**
     * 获取当前站点数据
     *
     * @apiNote 读取request.header['cc-current-site']中的siteId，如果无header或无站点则取数据库第一条站点数据
     */
    @XComment("{API.DOC.CMS.SITE.GET_CURRENT_SITE}")
    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/getCurrentSite")
    public R<Map<String, Object>> getCurrentSiteOrDefault() {
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        CmsSite site = this.siteService.getCurrentSiteOrDefault(ServletUtils.getRequest(), loginUser);
        return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
    }

    @XComment("{API.DOC.CMS.SITE.GET_DASHBOARD_INFO}")
    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/getDashboardSiteInfo")
    public R<SiteDashboardVO> getDashboardSiteInfo() {
        CmsSite site = this.getCurrentSite();
        List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
        return R.ok(SiteDashboardVO.create(site, publishPipes));
    }

    /**
     * 设置当前站点
     *
     * @param siteId 站点ID
     */
    @IgnoreDemoMode
    @XComment("{API.DOC.CMS.SITE.SET_CURRENT_SITE}")
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @Log(title = "切换站点", businessType = BusinessType.UPDATE)
    @PostMapping("/setCurrentSite/{siteId}")
    public R<Map<String, Object>> setCurrentSite(@PathVariable @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
    }

    @XComment("{API.DOC.CMS.SITE.SELECT_LIST}")
    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/selectList")
    public R<TableData<CmsSite>> selectList(@RequestParam(value = "siteName", required = false) String siteName) {
        return this.list(siteName);
    }

    /**
     * 查询站点数据列表
     *
     * @param siteName 站点名称
     */
    @XComment("{API.DOC.CMS.SITE.GET_LIST}")
    @Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteView)
    @GetMapping("/list")
    public R<TableData<CmsSite>> list(@RequestParam(value = "siteName", required = false) String siteName) {
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        List<CmsSite> list = siteService.lambdaQuery()
                .like(StringUtils.isNotEmpty(siteName), CmsSite::getName, siteName)
                .list().stream()
                .filter(site -> loginUser.hasPermission(SitePrivItem.View.getPermissionKey(site.getSiteId())))
                .toList();
        list.forEach(site -> {
            if (StringUtils.isNotEmpty(site.getLogo())) {
                site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
            }
        });
        return this.bindDataTable(list);
    }

    /**
     * 获取站点详情
     *
     * @param siteId 站点ID
     */
    @XComment("{API.DOC.CMS.SITE.GET_DETAIL}")
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping(value = "/detail/{siteId}")
    public R<SiteDTO> getInfo(@PathVariable @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId) {
        CmsSite site = siteService.getById(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        if (StringUtils.isNotEmpty(site.getLogo())) {
            site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
        }
        SiteDTO dto = SiteDTO.newInstance(site);
        resourceService.dealDefaultThumbnail(site, dto.getLogo(), dto::setLogoSrc);
        // 发布通道数据
        List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
                PublishPipePropUseType.Site, site.getPublishPipeProps());
        dto.setPublishPipeDatas(publishPipeProps);
        return R.ok(dto);
    }

    @XComment("{API.DOC.CMS.SITE.GET_OPTIONS}")
    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/options")
    public R<TableData<Map<String, Object>>> getSiteOptions() {
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        List<Map<String, Object>> list = this.siteService.lambdaQuery().select(List.of(CmsSite::getSiteId, CmsSite::getName))
                .list().stream()
                .filter(site -> loginUser.hasPermission(SitePrivItem.View.getPermissionKey(site.getSiteId())))
                .map(site -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", site.getSiteId());
                    map.put("name", site.getName());
                    return map;
                }).toList();
        return this.bindDataTable(list);
    }

    /**
     * 新增站点数据
     */
    @XComment("{API.DOC.CMS.SITE.ADD}")
    @Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteAdd)
    @Log(title = "新增站点", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<CmsSite> addSave(@RequestBody @Validated SiteDTO dto) throws IOException {
        CmsSite site = this.siteService.addSite(dto);
        return R.ok(site);
    }

    /**
     * 修改站点数据
     */
    @XComment("{API.DOC.CMS.SITE.UPDATE}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#dto.siteId}")
    @Log(title = "编辑站点", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    public R<Void> editSave(@RequestBody @Validated SiteDTO dto) throws IOException {
        this.siteService.saveSite(dto);
        return R.ok();
    }

    /**
     * 删除站点数据
     *
     * @param siteId 站点ID
     */
    @XComment("{API.DOC.CMS.SITE.DELETE}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Delete:${#siteId}")
    @Log(title = "删除站点", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{siteId}")
    public R<String> remove(@PathVariable @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId) throws IOException {
        CmsSite site = siteService.getById(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        AsyncTask task = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                siteService.deleteSite(siteId);
            }
        };
        task.setLocale(LocaleContextHolder.getLocale());
        task.setTaskId("DeleteSite_" + siteId);
        this.asyncTaskManager.execute(task);
        return R.ok(task.getTaskId());
    }

    /**
     * 发布站点
     */
    @XComment("{API.DOC.CMS.SITE.PUBLISH}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Publish:${#dto.siteId}")
    @Log(title = "发布站点", businessType = BusinessType.OTHER)
    @PostMapping("/publish")
    public R<String> publishAll(@RequestBody @Validated PublishSiteDTO dto) throws IOException, TemplateException {
        CmsSite site = siteService.getById(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        if (!dto.isPublishIndex()) {
            AsyncTask task = publishService.publishAll(site, dto.getContentStatus(), Operator.of(StpAdminUtil.getLoginUser()));
            return R.ok(task.getTaskId());
        }
        publishService.publishSiteIndex(site);
        return R.ok();
    }

    /**
     * 获取站点扩展配置数据
     *
     * @param siteId 站点ID
     */
    @XComment("{API.DOC.CMS.SITE.GET_EXTENDS}")
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping("/extends")
    public R<Map<String, Object>> getSiteExtends(@RequestParam("siteId") @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(site.getConfigProps(), UseType.Site);
        configProps.put("PreviewPrefix", SiteUtils.getResourcePreviewPrefix(site));
        return R.ok(configProps);
    }

    /**
     * 保存站点扩展配置数据
     *
     * @param siteId  站点ID
     * @param configs 扩展配置数据
     */
    @XComment("{API.DOC.CMS.SITE.SAVE_EXTENDS}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "站点扩展", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    @PostMapping("/extends/{siteId}")
    public R<Void> saveSiteExtends(@PathVariable @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId, @RequestBody Map<String, String> configs) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        this.siteService.saveSiteExtend(site, configs, StpAdminUtil.getLoginUser().getUsername());
        return R.ok();
    }

    /**
     * 获取站点默认模板配置
     *
     * @param siteId 站点ID
     */
    @XComment("{API.DOC.CMS.SITE.GET_DEFAULT_TEMPLATE}")
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping("/default_template")
    public R<SiteDefaultTemplateDTO> getDefaultTemplates(@RequestParam("siteId") @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        SiteDefaultTemplateDTO dto = new SiteDefaultTemplateDTO();
        dto.setSiteId(siteId);
        // 发布通道数据
        List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
                PublishPipePropUseType.Site, site.getPublishPipeProps());
        dto.setPublishPipeProps(publishPipeProps);
        return R.ok(dto);
    }

    /**
     * 保存站点默认模板配置
     */
    @XComment("{API.DOC.CMS.SITE.SAVE_DEFAULT_TEMPLATE}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#dto.siteId}")
    @Log(title = "默认模板", businessType = BusinessType.UPDATE)
    @PostMapping("/default_template")
    public R<Void> saveDefaultTemplates(@RequestBody @Validated SiteDefaultTemplateDTO dto) {
        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        this.siteService.saveSiteDefaultTemplate(dto);
        return R.ok();
    }

    /**
     * 应用站点默认模板配置到指定栏目
     */
    @XComment("{API.DOC.CMS.SITE.APPLY_DEFAULT_TEMPLATE}")
    @Priv(type = AdminUserType.TYPE)
    @Log(title = "应用默认模板", businessType = BusinessType.UPDATE)
    @PostMapping("/apply_default_template")
    public R<Void> applyDefaultTemplateToCatalog(@RequestBody @Validated SiteDefaultTemplateDTO dto) {
        Assert.isTrue(IdUtils.validate(dto.getToCatalogIds()), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("toCatalogIds"));

        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        this.catalogService.applySiteDefaultTemplateToCatalog(dto);
        return R.ok();
    }

    /**
     * 上传水印图片
     *
     * @param siteId        站点ID
     * @param multipartFile 上传文件
     */
    @XComment("{API.DOC.CMS.SITE.UPLOAD_WATERMARK}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "上传水印图", businessType = BusinessType.UPDATE)
    @PostMapping("/upload_watermarkimage")
    public R<Map<String, Object>> uploadFile(@RequestParam("siteId") @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId, @RequestParam("file") @NotNull MultipartFile multipartFile) {
        try {
            CmsSite site = this.siteService.getSite(siteId);
            Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

            String dir = SiteUtils.getSiteResourceRoot(site.getPath());
            String ext = FileExUtils.getExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            if (!ResourceType_Image.isImageExt(ext)) {
                throw ContentCoreErrorCode.UNSUPPORTED_FILE_TYPE.exception(ext);
            }
            String filename = "watermaker" + StringUtils.DOT + ext;
            File file = new File(dir + filename);
            FileExUtils.mkdirs(file.getParentFile().getAbsolutePath());
            FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());
            String src = SiteUtils.getResourcePreviewPrefix(site) + filename;
            return R.ok(Map.of("path", filename, "src", src));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @XComment("{API.DOC.CMS.SITE.IMPORT_THEME}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "导入主题模板", businessType = BusinessType.UPDATE)
    @PostMapping("/importTheme")
    public R<String> importSiteTheme(@RequestParam("siteId") @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId, @RequestParam("file") @NotNull MultipartFile multipartFile) {
        try {
            CmsSite site = this.siteService.getSite(siteId);
            Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

            String path = SiteUtils.getSiteResourceRoot(site.getPath()) + "importTheme.zip";
            File file = new File(path);
            FileUtils.forceMkdirParent(file);
            FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());
            AsyncTask asyncTask = this.siteExportService.importSiteTheme(site, file, StpAdminUtil.getLoginUser());
            return R.ok(asyncTask.getTaskId());
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @XComment("{API.DOC.CMS.SITE.EXPORT_THEME}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#dto.siteId}")
    @PostMapping("/exportTheme")
    public R<String> exportSiteTheme(@Validated @RequestBody SiteExportDTO dto) {
        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        AsyncTask asyncTask = this.siteExportService.exportSiteTheme(site);
        return R.ok(asyncTask.getTaskId());
    }

    @XComment("{API.DOC.CMS.SITE.DOWNLOAD_THEME}")
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @GetMapping("/downloadTheme/{siteId}")
    public ResponseEntity<StreamingResponseBody> downloadTheme(@PathVariable @LongId @XComment("{API.DOC.CMS.SITE.SITE_ID}") Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        File file = new File(SiteUtils.getSiteResourceRoot(site) + SiteThemeService.ThemeZipPath);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream -> {
                    long timeout = 600_000;
                    Instant startTime = Instant.now();
                    try (InputStream is = new FileInputStream(file)) {
                        byte[] data = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = is.read(data)) != -1) {
                            outputStream.write(data, 0, bytesRead);
                            outputStream.flush();
                            if (Duration.between(startTime, Instant.now()).toMillis() > timeout) {
                                throw new RuntimeException("Timed out");
                            }
                        }
                    }
                });
    }
}