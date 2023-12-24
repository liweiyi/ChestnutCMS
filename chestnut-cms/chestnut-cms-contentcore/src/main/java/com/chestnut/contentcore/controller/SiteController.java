package com.chestnut.contentcore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.core.IProperty.UseType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.*;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.perms.SitePermissionType.SitePrivItem;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.impl.SiteThemeService;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 站点管理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/site")
@RequiredArgsConstructor
public class SiteController extends BaseRestController {

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IPublishPipeService publishPipeService;

    private final IPublishService publishService;

    private final AsyncTaskManager asyncTaskManager;

    private final SiteThemeService siteExportService;

    /**
     * 获取当前站点数据
     *
     * @apiNote 读取request.header['CurrentSite']中的siteId，如果无header或无站点则取数据库第一条站点数据
     */
    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/getCurrentSite")
    public R<Map<String, Object>> getCurrentSite() {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
    }

    /**
     * 设置当前站点
     *
     * @param siteId 站点ID
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @Log(title = "切换站点", businessType = BusinessType.UPDATE)
    @PostMapping("/setCurrentSite/{siteId}")
    public R<Map<String, Object>> setCurrentSite(@PathVariable("siteId") @LongId Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
    }

    /**
     * 查询站点数据列表
     *
     * @param siteName 站点名称
     */
    @Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteView)
    @GetMapping("/list")
    public R<?> list(@RequestParam(value = "siteName", required = false) String siteName) {
        PageRequest pr = this.getPageRequest();
        LambdaQueryWrapper<CmsSite> q = new LambdaQueryWrapper<CmsSite>().like(StringUtils.isNotEmpty(siteName),
                CmsSite::getName, siteName);
        Page<CmsSite> page = siteService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
        LoginUser loginUser = StpAdminUtil.getLoginUser();
        List<CmsSite> list = page.getRecords().stream()
                .filter(site -> loginUser.hasPermission(SitePrivItem.View.getPermissionKey(site.getSiteId())))
                .toList();
        list.forEach(site -> {
            if (StringUtils.isNotEmpty(site.getLogo())) {
                site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
            }
        });
        return this.bindDataTable(list, page.getTotal());
    }

    /**
     * 获取站点详情
     *
     * @param siteId 站点ID
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping(value = "/{siteId}")
    public R<?> getInfo(@PathVariable @LongId Long siteId) {
        CmsSite site = siteService.getById(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        if (StringUtils.isNotEmpty(site.getLogo())) {
            site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
        }
        SiteDTO dto = SiteDTO.newInstance(site);
        // 发布通道数据
        List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
                PublishPipePropUseType.Site, site.getPublishPipeProps());
        dto.setPublishPipeDatas(publishPipeProps);
        return R.ok(dto);
    }

    @Priv(type = AdminUserType.TYPE)
    @GetMapping("/options")
    public R<?> getSiteOptions() {
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
    @Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteView)
    @Log(title = "新增站点", businessType = BusinessType.INSERT)
    @PostMapping
    public R<?> addSave(@RequestBody @Validated SiteDTO dto) throws IOException {
        dto.setOperator(StpAdminUtil.getLoginUser());
        CmsSite site = this.siteService.addSite(dto);
        return R.ok(site);
    }

    /**
     * 修改站点数据
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#dto.siteId}")
    @Log(title = "编辑站点", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<?> editSave(@RequestBody @Validated SiteDTO dto) throws IOException {
        dto.setOperator(StpAdminUtil.getLoginUser());
        this.siteService.saveSite(dto);
        return R.ok();
    }

    /**
     * 删除站点数据
     *
     * @param siteId 站点ID
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Delete:${#siteId}")
    @Log(title = "删除站点", businessType = BusinessType.DELETE)
    @DeleteMapping("/{siteId}")
    public R<String> remove(@PathVariable("siteId") @LongId Long siteId) throws IOException {
        CmsSite site = siteService.getById(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        AsyncTask task = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                siteService.deleteSite(siteId);
            }
        };
        task.setTaskId("DeleteSite_" + siteId);
        this.asyncTaskManager.execute(task);
        return R.ok(task.getTaskId());
    }

    /**
     * 发布站点
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Publish:${#dto.siteId}")
    @Log(title = "发布站点", businessType = BusinessType.OTHER)
    @PostMapping("/publish")
    public R<String> publishAll(@RequestBody @Validated PublishSiteDTO dto) throws IOException, TemplateException {
        CmsSite site = siteService.getById(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        if (!dto.isPublishIndex()) {
            AsyncTask task = publishService.publishAll(site, dto.getContentStatus(), StpAdminUtil.getLoginUser());
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
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping("/extends")
    public R<?> getSiteExtends(@RequestParam("siteId") @LongId Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(site.getConfigProps(), UseType.Site);
        configProps.put("PreviewPrefix", SiteUtils.getResourcePrefix(site, true));
        return R.ok(configProps);
    }

    /**
     * 保存站点扩展配置数据
     *
     * @param siteId  站点ID
     * @param configs 扩展配置数据
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "站点扩展", businessType = BusinessType.UPDATE, isSaveRequestData = false)
    @PostMapping("/extends/{siteId}")
    public R<?> saveSiteExtends(@PathVariable("siteId") @LongId Long siteId, @RequestBody Map<String, String> configs) {
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
    @Priv(type = AdminUserType.TYPE, value = "Site:View:${#siteId}")
    @GetMapping("/default_template")
    public R<?> getDefaultTemplates(@RequestParam("siteId") @LongId Long siteId) {
        CmsSite site = this.siteService.getSite(siteId);
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

        SiteDefaultTemplateDTO dto = new SiteDefaultTemplateDTO();
        dto.setSiteId(siteId);
        // 发布通道数据
        List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
                PublishPipePropUseType.Site, site.getPublishPipeProps());
        dto.setPublishPipeProps(publishPipeProps);
        return R.ok(dto);
    }

    /**
     * 保存站点默认模板配置
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#dto.siteId}")
    @Log(title = "默认模板", businessType = BusinessType.UPDATE)
    @PostMapping("/default_template")
    public R<?> saveDefaultTemplates(@RequestBody @Validated SiteDefaultTemplateDTO dto) {
        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        dto.setOperator(StpAdminUtil.getLoginUser());
        this.siteService.saveSiteDefaultTemplate(dto);
        return R.ok();
    }

    /**
     * 应用站点默认模板配置到指定栏目
     */
    @Priv(type = AdminUserType.TYPE)
    @Log(title = "应用默认模板", businessType = BusinessType.UPDATE)
    @PostMapping("/apply_default_template")
    public R<?> applyDefaultTemplateToCatalog(@RequestBody @Validated SiteDefaultTemplateDTO dto) {
        Assert.isTrue(IdUtils.validate(dto.getToCatalogIds()), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("toCatalogIds"));

        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        dto.setOperator(StpAdminUtil.getLoginUser());
        this.catalogService.applySiteDefaultTemplateToCatalog(dto);
        return R.ok();
    }

    /**
     * 上传水印图片
     *
     * @param siteId        站点ID
     * @param multipartFile 上传文件
     */
    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "上传水印图", businessType = BusinessType.UPDATE)
    @PostMapping("/upload_watermarkimage")
    public R<?> uploadFile(@RequestParam("siteId") @LongId Long siteId,
                           @RequestParam("file") @NotNull MultipartFile multipartFile) throws Exception {
        try {
            CmsSite site = this.siteService.getSite(siteId);
            Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

            String dir = SiteUtils.getSiteResourceRoot(site.getPath()) + "resources";
            String suffix = FileExUtils.getExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            String path = "watermaker" + StringUtils.DOT + suffix;
            File file = new File(dir + path);
            boolean mkdirs = file.getParentFile().mkdirs();
            if (!mkdirs) {
                return R.fail("mkdirs failed.");
            }
            FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());
            String src = SiteUtils.getResourcePrefix(site, true) + path;
            return R.ok(Map.of("path", path, "src", src));
        } catch (Exception e) {
            return R.fail(e.getMessage());
        }
    }

    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @Log(title = "导入主题模板", businessType = BusinessType.UPDATE)
    @PostMapping("/importTheme")
    public R<?> importSiteTheme(@RequestParam("siteId") @LongId Long siteId,
                           @RequestParam("file") @NotNull MultipartFile multipartFile) throws Exception {
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

    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @PostMapping("/exportTheme")
    public R<?> exportSiteTheme(@Validated @RequestBody SiteExportDTO dto) {
        CmsSite site = this.siteService.getSite(dto.getSiteId());
        Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

        AsyncTask asyncTask = this.siteExportService.exportSiteTheme(site);
        return R.ok(asyncTask.getTaskId());
    }

    @Priv(type = AdminUserType.TYPE, value = "Site:Edit:${#siteId}")
    @PostMapping("/theme_download")
    public void export(@RequestParam Long siteId, HttpServletResponse response) throws IOException {
        CmsSite site = this.siteService.getSite(siteId);
        File file = new File(SiteUtils.getSiteResourceRoot(site) + SiteThemeService.ThemeZipPath);
        if (!file.exists()) {
            response.getWriter().write("站点主题文件不存在");
            return;
        }
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        response.setHeader("Content-disposition", "attachment;filename="
                + StringUtils.substringAfterLast(SiteThemeService.ThemeZipPath, "/"));
        response.addHeader("Content-Length", "" + file.length());
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buff = new byte[1024];
            OutputStream os  = response.getOutputStream();
            int i;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            e.printStackTrace();
        }
    }
}