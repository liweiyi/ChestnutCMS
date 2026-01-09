package com.chestnut.cms.cdn.controller;

import com.chestnut.cloud.domain.CcCloudConfig;
import com.chestnut.cloud.service.ICloudConfigService;
import com.chestnut.cms.cdn.properties.CdnCloudConfigProperty;
import com.chestnut.common.cloud.CdnRefreshType;
import com.chestnut.common.cloud.CloudService;
import com.chestnut.common.cloud.ICloudProvider;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.exception.GlobalException;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsPublishPipe;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishPipeService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/cdn")
public class CmsCdnController extends BaseRestController {

    private final ISiteService siteService;

    private final ICatalogService catalogService;

    private final IContentService contentService;

    private final IPublishPipeService publishPipeService;

    private final CloudService cloudService;

    private final ICloudConfigService CcCloudConfigService;

    @PostMapping("/refresh/site")
    public R<?> refreshSite(@RequestParam @LongId Long siteId, @RequestParam Boolean refreshAll) {
        CmsSite site = siteService.getSite(siteId);
        CcCloudConfig cloudConfig = this.getCcCloudConfig(site);
        ICloudProvider cloudProvider = cloudService.getCloudProvider(cloudConfig.getType());

        List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(siteId);
        List<String> urls = publishPipes.stream()
                .map(pp -> site.getUrl(pp.getCode()))
                .filter(StringUtils::isNotEmpty).toList();
        if (refreshAll) {
            cloudProvider.refreshCdn(cloudConfig.getConfigProps(), CdnRefreshType.DIR, urls);
        } else {
            cloudProvider.refreshCdn(cloudConfig.getConfigProps(), CdnRefreshType.FILE, urls);
        }
        return R.ok();
    }

    @PostMapping("/refresh/catalog")
    public R<?> refreshCatalog(@RequestParam @LongId Long catalogId, @RequestParam Boolean refreshAll) {
        CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
        Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(catalog));

        CmsSite site = siteService.getSite(catalog.getSiteId());
        CcCloudConfig cloudConfig = this.getCcCloudConfig(site);
        ICloudProvider cloudProvider = cloudService.getCloudProvider(cloudConfig.getType());

        List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(site.getSiteId());
        if (publishPipes.isEmpty()) {
            return R.fail("未配置发布通道");
        }
        List<String> urls = new ArrayList<>();
        for (CmsPublishPipe publishPipe : publishPipes) {
            String catalogLink = CatalogUtils.getCatalogLink(site, catalog, 0, publishPipe.getCode(), false);
            if (!ServletUtils.isHttpUrl(catalogLink)) {
                // 相对路径需要补域名
                String url = site.getUrl(publishPipe.getCode());
                catalogLink = url + StringUtils.removeStart(catalogLink, "/");
            }
            urls.add(catalogLink);
        }
        if (refreshAll) {
            cloudProvider.refreshCdn(cloudConfig.getConfigProps(), CdnRefreshType.DIR, urls);
        } else {
            cloudProvider.refreshCdn(cloudConfig.getConfigProps(), CdnRefreshType.FILE, urls);
        }
        return R.ok();
    }

    @PostMapping("/refresh/content")
    public R<?> refreshContent(@RequestParam @LongId Long contentId) {
        CmsContent content = this.contentService.dao().getById(contentId);
        Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(contentId));

        CmsSite site = siteService.getSite(content.getSiteId());
        CcCloudConfig cloudConfig = this.getCcCloudConfig(site);
        ICloudProvider cloudProvider = cloudService.getCloudProvider(cloudConfig.getType());

        List<CmsPublishPipe> publishPipes = publishPipeService.getPublishPipes(site.getSiteId());
        if (publishPipes.isEmpty()) {
            return R.fail("未配置发布通道");
        }
        List<String> urls = new ArrayList<>();
        for (CmsPublishPipe publishPipe : publishPipes) {
            String contentLink = contentService.getContentLink(content, 0, publishPipe.getCode(), false);
            if (!ServletUtils.isHttpUrl(contentLink)) {
                // 相对路径需要补域名
                String url = site.getUrl(publishPipe.getCode());
                contentLink = url + StringUtils.removeStart(contentLink, "/");
            }
            urls.add(contentLink);
        }
        cloudProvider.refreshCdn(cloudConfig.getConfigProps(), CdnRefreshType.FILE, urls);
        return R.ok();
    }

    private CcCloudConfig getCcCloudConfig(CmsSite site) {
        Long configId = CdnCloudConfigProperty.getValue(site);

        if (!IdUtils.validate(configId)) {
            throw new GlobalException("未配置CDN云服务");
        }
        CcCloudConfig cloudConfig = CcCloudConfigService.getCloudConfig(configId);
        if (Objects.isNull(cloudConfig)) {
            throw new GlobalException("云服务配置不存在");
        }
        return cloudConfig;
    }
}
