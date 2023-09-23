package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.*;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.PublishPipeProp;
import com.chestnut.contentcore.domain.dto.SiteDTO;
import com.chestnut.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.listener.event.AfterSiteDeleteEvent;
import com.chestnut.contentcore.listener.event.AfterSiteSaveEvent;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.contentcore.mapper.CmsSiteMapper;
import com.chestnut.contentcore.perms.SitePermissionType;
import com.chestnut.contentcore.perms.SitePermissionType.SitePrivItem;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.enums.PermissionOwnerType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl extends ServiceImpl<CmsSiteMapper, CmsSite> implements ISiteService {

	/**
	 * 缓存key前缀
	 */
	private static final String CACHE_PREFIX = CMSConfig.CachePrefix + "site:";

	private final ApplicationContext applicationContext;

	private final ISysPermissionService permissionService;

	private final RedisCache redisCache;

	@Override
	public CmsSite getSite(Long siteId) {
		CmsSite site = this.redisCache.getCacheObject(CACHE_PREFIX + siteId);
		if (Objects.isNull(site)) {
			site = this.getById(siteId);
			Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
			this.redisCache.setCacheObject(CACHE_PREFIX + siteId, site);
		}
		return site;
	}

	@Override
	public CmsSite getCurrentSite(HttpServletRequest request) {
		CmsSite site = null;
		String siteId = ServletUtils.getHeader(request, ContentCoreConsts.Header_CurrentSite);
		if (NumberUtils.isDigits(siteId)) {
			try {
				site = this.getSite(Long.valueOf(siteId));
			} catch (Exception e) {
			}
		}
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		// 无当前站点或当前站点无权限则取数据库查找一条有权限的站点数据作为当前站点
		if (Objects.isNull(site) || !loginUser.hasPermission(SitePrivItem.View.getPermissionKey(site.getSiteId()))) {
			Optional<CmsSite> opt = this.lambdaQuery().list().stream().filter(s ->
					loginUser.hasPermission(SitePrivItem.View.getPermissionKey(s.getSiteId()))).findFirst();
			if (opt.isPresent()) {
				site = opt.get();
			}
		}
		Assert.notNull(site, ContentCoreErrorCode.NO_SITE::exception);
		return site;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsSite addSite(SiteDTO dto) {
		boolean checkSiteUnique = this.checkSiteUnique(dto.getName(), dto.getPath(), 0L);
		Assert.isTrue(checkSiteUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("name", "path"));

		String siteRoot = SiteUtils.getSiteResourceRoot(dto.getPath());
		Assert.isFalse(new File(siteRoot).exists(), ContentCoreErrorCode.EXISTS_SITE_PATH::exception);
		// 创建站点目录
		FileExUtils.mkdirs(siteRoot);

		CmsSite site = new CmsSite();
		site.setSiteId(IdUtils.getSnowflakeId());
		BeanUtils.copyProperties(dto, site, "siteId");
		if (StringUtils.isNotEmpty(site.getResourceUrl()) && !site.getResourceUrl().endsWith("/")) {
			site.setResourceUrl(site.getResourceUrl() + "/");
		}
		site.setSortFlag(SortUtils.getDefaultSortValue());
		site.createBy(dto.getOperator().getUsername());
		this.save(site);
		this.clearCache(site.getSiteId());
		// 授权给添加人
		this.permissionService.grantPermission(PermissionOwnerType.User.name(),
				dto.getOperator().getUserId().toString(), SitePermissionType.ID,
				CmsPrivUtils.getAllSitePermissions(site.getSiteId()));
		this.permissionService.resetLoginUserPermissions(dto.getOperator());
		return site;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsSite saveSite(SiteDTO dto) {
		boolean checkSiteUnique = this.checkSiteUnique(dto.getName(), dto.getPath(), dto.getSiteId());
		Assert.isTrue(checkSiteUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("name", "path"));

		CmsSite site = this.getById(dto.getSiteId());

		BeanUtils.copyProperties(dto, site, "path");
		if (StringUtils.isNotEmpty(site.getResourceUrl()) && !site.getResourceUrl().endsWith("/")) {
			site.setResourceUrl(site.getResourceUrl() + "/");
		}
		// 发布通道数据处理
		Map<String, Map<String, Object>> publishPipeProps = dto.getPublishPipeDatas().stream()
				.collect(Collectors.toMap(PublishPipeProp::getPipeCode, PublishPipeProp::getProps));
		site.setPublishPipeProps(publishPipeProps);
		site.updateBy(dto.getOperator().getUsername());
		this.updateById(site);
		this.clearCache(site.getSiteId());

		this.applicationContext.publishEvent(new AfterSiteSaveEvent(this, site, dto));
		return site;
	}

	/**
	 * 删除站点数据，不删除文件系统站点相关目录。一般站点目录不建议修改，必须删除时建议去文件系统直接删除。
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteSite(Long siteId) throws IOException {
		CmsSite site = this.getById(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

		applicationContext.publishEvent(new BeforeSiteDeleteEvent(this, site));

		AsyncTaskManager.setTaskMessage("正在删除站点数据");
		this.removeById(site.getSiteId());
		this.clearCache(site.getSiteId());

		applicationContext.publishEvent(new AfterSiteDeleteEvent(this, site));
	}

	/**
	 * 校验站点名称/目录是否唯一
	 *
	 * @param siteName 站点名称
	 * @param sitePath 站点目录
	 * @param siteId   站点ID
	 * @return 结果
	 */
	@Override
	public boolean checkSiteUnique(String siteName, String sitePath, Long siteId) {
		return this.lambdaQuery()
				.and(wrapper -> wrapper.eq(CmsSite::getName, siteName).or().eq(CmsSite::getPath, sitePath))
				.ne(IdUtils.validate(siteId), CmsSite::getSiteId, siteId).count() == 0;
	}

	@Override
	public void saveSiteDefaultTemplate(SiteDefaultTemplateDTO dto) {
		CmsSite site = this.getSite(dto.getSiteId());
		List<PublishPipeProp> publishPipeProps = dto.getPublishPipeProps();
		for (PublishPipeProp ppp : publishPipeProps) {
			Map<String, Object> sitePublishPipeProps = site.getPublishPipeProps(ppp.getPipeCode());
			sitePublishPipeProps.putAll(ppp.getProps());
		}
		site.updateBy(dto.getOperator().getUsername());
		this.updateById(site);

		this.clearCache(site.getSiteId());
	}

	@Override
	public void saveSiteExtend(CmsSite site, Map<String, String> configs, String operator) {
		ConfigPropertyUtils.filterConfigProps(configs, site.getConfigProps(), IProperty.UseType.Site);

		site.setConfigProps(configs);
		site.updateBy(operator);
		this.updateById(site);
		this.clearCache(site.getSiteId());
	}

	@Override
	public void clearCache(long siteId) {
		this.redisCache.deleteObject(CACHE_PREFIX + siteId);
	}
}
