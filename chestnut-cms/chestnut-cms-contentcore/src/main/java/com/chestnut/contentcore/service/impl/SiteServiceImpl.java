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
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.storage.FileStorageService;
import com.chestnut.common.storage.IFileStorageType;
import com.chestnut.common.utils.*;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.cache.SiteMonitoredCache;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.core.IPublishPipeProp;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.SiteDTO;
import com.chestnut.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.chestnut.contentcore.domain.dto.SortSiteRequest;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.contentcore.enums.ContentCoreTips;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.listener.event.AfterSiteAddEvent;
import com.chestnut.contentcore.listener.event.AfterSiteDeleteEvent;
import com.chestnut.contentcore.listener.event.AfterSiteSaveEvent;
import com.chestnut.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.chestnut.contentcore.mapper.CmsSiteMapper;
import com.chestnut.contentcore.perms.SitePermissionType;
import com.chestnut.contentcore.perms.SitePermissionType.SitePrivItem;
import com.chestnut.contentcore.properties.EnableSiteDeleteBackupProperty;
import com.chestnut.contentcore.properties.FileStorageTypeProperty;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.ConfigPropertyUtils;
import com.chestnut.contentcore.util.FileStorageHelper;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.permission.PermissionUtils;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.service.impl.UserPermissionService;
import com.chestnut.system.utils.TransactionUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 站点数据与配置管理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl extends ServiceImpl<CmsSiteMapper, CmsSite> implements ISiteService {

	private static final Comparator<CmsSite> SITE_ORDER = Comparator.comparing(CmsSite::getSortFlag)
			.thenComparing(CmsSite::getSiteId);

	private final ApplicationContext applicationContext;

	private final ISysUserService userService;

	private final UserPermissionService userPermissionService;

	private final Map<String, IPublishPipeProp> publishPipeProps;

	private final SiteMonitoredCache siteCache;

	private final FileStorageService fileStorageService;

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public List<CmsSite> listSites(String siteName, LoginUser loginUser) {
		return this.listSites(siteName, null, loginUser);
	}

	@Override
	public List<CmsSite> listChildSites(long parentId, LoginUser loginUser) {
		if (parentId > 0) {
			PermissionUtils.checkPermission(SitePrivItem.View.getPermissionKey(parentId), loginUser);
		}
		return this.listSites(null, parentId, loginUser);
	}

	private List<CmsSite> listSites(String siteName, Long parentId, LoginUser loginUser) {
		LocalDateTime now = LocalDateTime.now();
		List<CmsSite> sites = this.lambdaQuery().like(StringUtils.isNotEmpty(siteName), CmsSite::getName, siteName)
				.eq(parentId != null, CmsSite::getParentId, parentId)
				.list().stream()
				.filter(site -> loginUser.hasPermission(SitePrivItem.View.getPermissionKey(site.getSiteId())))
				.toList();
		sites.forEach(site -> {
			if (!site.isTopAt(now)) {
				site.setTopFlag(0L);
				site.setTopDate(null);
			}
		});
		return sites.stream()
				.sorted(Comparator.comparing(CmsSite::getTopFlag).reversed().thenComparing(SITE_ORDER))
				.toList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sortSite(SortSiteRequest request, String operator) {
		if (request.getSiteId().equals(request.getTargetSiteId())) {
			return;
		}
		List<CmsSite> requestedSites = this.listByIds(List.of(request.getSiteId(), request.getTargetSiteId()));
		Long parentId = requireSite(requestedSites, request.getSiteId()).getParentId();
		Assert.isTrue(parentId.equals(requireSite(requestedSites, request.getTargetSiteId()).getParentId()),
				ContentCoreErrorCode.SITE_SORT_DIFFERENT_PARENTS::exception);

		// 按站点 ID 固定加锁顺序，使相邻排序值在本次事务内保持稳定。
		List<CmsSite> sites = new ArrayList<>(this.lambdaQuery()
				.select(CmsSite::getSiteId, CmsSite::getSortFlag, CmsSite::getTopFlag, CmsSite::getTopDate)
				.eq(CmsSite::getParentId, parentId)
				.orderByAsc(CmsSite::getSiteId)
				.last("FOR UPDATE")
				.list());
		sites.sort(SITE_ORDER);
		CmsSite site = requireSite(sites, request.getSiteId());
		CmsSite target = requireSite(sites, request.getTargetSiteId());
		LocalDateTime now = LocalDateTime.now();
		Assert.isFalse(site.isTopAt(now) || target.isTopAt(now), ContentCoreErrorCode.SITE_TOP_SORT_FORBIDDEN::exception);

		int oldIndex = sites.indexOf(site);
		sites.remove(oldIndex);
		int newIndex = sites.indexOf(target) + (request.getBeforeTarget() ? 0 : 1);
		if (oldIndex == newIndex) {
			return;
		}

		long previous = newIndex > 0 ? sites.get(newIndex - 1).getSortFlag() : sites.get(0).getSortFlag() - 2;
		long next = newIndex < sites.size() ? sites.get(newIndex).getSortFlag() : previous + 2;
		if (next - previous > 1) {
			this.updateSiteSortFlag(site, previous + (next - previous) / 2, operator);
			return;
		}

		// 相邻排序值相同或没有间隔时，按目标顺序重新分配排序值。
		sites.add(newIndex, site);
		for (int i = 0; i < sites.size(); i++) {
			CmsSite orderedSite = sites.get(i);
			long sortFlag = (i + 1L) * 100;
			if (orderedSite == site || orderedSite.getSortFlag() != sortFlag) {
				this.updateSiteSortFlag(orderedSite, sortFlag, orderedSite == site ? operator : null);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void moveSite(Long siteId, Long parentId, LoginUser operator) {
		long targetId = Objects.requireNonNullElse(parentId, 0L);
		Assert.isFalse(siteId.equals(targetId), ContentCoreErrorCode.SITE_MOVE_TO_SELF_OR_CHILD::exception);
		if (targetId > 0) {
			PermissionUtils.checkPermission(SitePrivItem.View.getPermissionKey(targetId), operator);
			PermissionUtils.checkPermission(SitePrivItem.Edit.getPermissionKey(targetId), operator);
		}

		long previousParentId = this.getSiteForHierarchyChange(siteId).getParentId();
		List<CmsSite> lockedSites = this.lockSites(List.of(siteId, previousParentId, targetId));
		CmsSite site = requireSite(lockedSites, siteId);
		Assert.isTrue(site.getParentId() == previousParentId, ContentCoreErrorCode.SITE_PARENT_CHANGED::exception);
		if (previousParentId == targetId) {
			return;
		}

		CmsSite target = targetId == 0 ? null : requireSite(lockedSites, targetId);
		String previousPath = site.getAncestors();
		Assert.isFalse(target != null && target.getAncestors().startsWith(previousPath + ","),
				ContentCoreErrorCode.SITE_MOVE_TO_SELF_OR_CHILD::exception);
		String newPath = target == null ? siteId.toString() : target.getAncestors() + "," + siteId;
		int newLevel = target == null ? 1 : target.getTreeLevel() + 1;
		int levelChange = newLevel - site.getTreeLevel();
		List<CmsSite> subtree = this.lockSubtree(site);
		LocalDateTime now = LocalDateTime.now();
		for (CmsSite node : subtree) {
			String ancestors = newPath + node.getAncestors().substring(previousPath.length());
			Assert.isTrue(ancestors.length() <= CmsSite.MAX_ANCESTORS_LENGTH, ContentCoreErrorCode.SITE_TREE_TOO_DEEP::exception);
			this.lambdaUpdate().eq(CmsSite::getSiteId, node.getSiteId())
					.set(node == site, CmsSite::getParentId, targetId)
					.set(CmsSite::getAncestors, ancestors)
					.set(CmsSite::getTreeLevel, node.getTreeLevel() + levelChange)
					.set(CmsSite::getUpdateBy, operator.getUsername())
					.set(CmsSite::getUpdateTime, now)
					.update();
		}
		if (previousParentId > 0) {
			this.changeChildCount(requireSite(lockedSites, previousParentId), -1);
		}
		if (target != null) {
			this.changeChildCount(target, 1);
		}
		TransactionUtils.afterCommit(() -> subtree.forEach(node -> this.clearCache(node.getSiteId())));
	}

	private List<CmsSite> lockSubtree(CmsSite site) {
		List<CmsSite> subtree = new ArrayList<>(List.of(site));
		List<CmsSite> level = List.of(site);
		// 锁定一层后再读取下一层，使新增、删除与转移看到一致的父子关系。
		while (!level.isEmpty()) {
			List<Long> parentIds = level.stream().filter(node -> node.getChildCount() > 0)
					.map(CmsSite::getSiteId).toList();
			if (parentIds.isEmpty()) {
				break;
			}
			level = this.lambdaQuery().in(CmsSite::getParentId, parentIds).orderByAsc(CmsSite::getSiteId)
					.last("FOR UPDATE").list();
			subtree.addAll(level);
		}
		return subtree;
	}

	private CmsSite requireSite(List<CmsSite> sites, Long siteId) {
		return sites.stream().filter(site -> siteId.equals(site.getSiteId())).findFirst()
				.orElseThrow(() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
	}

	private void updateSiteSortFlag(CmsSite site, long sortFlag, String operator) {
		this.lambdaUpdate().eq(CmsSite::getSiteId, site.getSiteId())
				.set(CmsSite::getSortFlag, sortFlag)
				.set(operator != null, CmsSite::getUpdateBy, operator)
				.set(operator != null, CmsSite::getUpdateTime, LocalDateTime.now())
				.update();
		TransactionUtils.afterCommit(() -> this.clearCache(site.getSiteId()));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setTop(Long siteId, LocalDateTime topEndTime, String operator) {
		this.updateSiteTop(siteId, Instant.now().toEpochMilli(), topEndTime, operator);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelTop(Long siteId, String operator) {
		this.updateSiteTop(siteId, 0L, null, operator);
	}

	private void updateSiteTop(Long siteId, long topFlag, LocalDateTime topDate, String operator) {
		boolean updated = this.lambdaUpdate().eq(CmsSite::getSiteId, siteId)
				.set(CmsSite::getTopFlag, topFlag)
				.set(CmsSite::getTopDate, topDate)
				.set(CmsSite::getUpdateBy, operator)
				.set(CmsSite::getUpdateTime, LocalDateTime.now())
				.update();
		Assert.isTrue(updated, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		TransactionUtils.afterCommit(() -> this.clearCache(siteId));
	}

	@Override
	public CmsSite getSite(Long siteId) {
		CmsSite site = siteCache.getCache(siteId, () -> this.getById(siteId));
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		return site;
	}

	@Override
	public Map<Long, CmsSite> getSites(Collection<Long> siteIds) {
		if (siteIds == null || siteIds.isEmpty()) {
			return Map.of();
		}
		List<Long> ids = siteIds.stream().filter(IdUtils::validate).distinct().toList();
		Map<Long, CmsSite> sites = new HashMap<>(siteCache.getCaches(ids));
		List<Long> missedIds = ids.stream().filter(id -> !sites.containsKey(id)).toList();
		if (!missedIds.isEmpty()) {
			this.listByIds(missedIds).forEach(site -> {
				this.siteCache.setCache(site.getSiteId(), site);
				sites.put(site.getSiteId(), site);
			});
		}
		List<Long> notFoundIds = ids.stream().filter(id -> !sites.containsKey(id)).toList();
		Assert.isTrue(notFoundIds.isEmpty(), () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", notFoundIds));
		return sites;
	}

    @Override
    public CmsSite getCurrentSiteOrDefault(HttpServletRequest request, LoginUser loginUser) {
        CmsSite site = null;
        Long siteId = ConvertUtils.toLong(ServletUtils.getHeader(request, ContentCoreConsts.Header_CurrentSite));
        log.info("header[cc_current_site]: {}", siteId);
        if (IdUtils.validate(siteId) && loginUser.hasPermission(SitePrivItem.View.getPermissionKey(siteId))) {
            site = this.getSite(siteId);
        }
        // 无当前站点或当前站点无权限则取数据库查找一条有权限的站点数据作为当前站点
        if (Objects.isNull(site)) {
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
	public CmsSite getSiteOrCurrent(Long siteId, HttpServletRequest request, LoginUser loginUser) {
		CmsSite site;
        // siteId有效以siteId为准
		if (IdUtils.validate(siteId) && loginUser.hasPermission(SitePrivItem.View.getPermissionKey(siteId))) {
			site = getSite(siteId);
            Assert.notNull(site, ContentCoreErrorCode.NO_SITE::exception);
            return site;
		}
        // 未指定siteId时获取当前站点
        siteId = ConvertUtils.toLong(ServletUtils.getHeader(request, ContentCoreConsts.Header_CurrentSite));
        if (!IdUtils.validate(siteId)) {
            throw ContentCoreErrorCode.MISSING_CURRENT_SITE_ID.exception();
        }
        boolean hasPriv = loginUser.hasPermission(SitePrivItem.View.getPermissionKey(siteId));
        if (!hasPriv) {
            throw ContentCoreErrorCode.NO_CURRENT_SITE_PRIV.exception(siteId);
        }
        site = this.getSite(siteId);
        Assert.notNull(site, ContentCoreErrorCode.NO_SITE::exception);
        return site;
	}

    @Override
	public CmsSite getCurrentSite(HttpServletRequest request, LoginUser loginUser) {
		CmsSite site;
		Long siteId = ConvertUtils.toLong(ServletUtils.getHeader(request, ContentCoreConsts.Header_CurrentSite));
        if (!IdUtils.validate(siteId)) {
            throw ContentCoreErrorCode.MISSING_CURRENT_SITE_ID.exception();
        }
        boolean hasPriv = loginUser.hasPermission(SitePrivItem.View.getPermissionKey(siteId));
        if (!hasPriv) {
            throw ContentCoreErrorCode.NO_CURRENT_SITE_PRIV.exception(siteId);
        }
        site = this.getSite(siteId);
        Assert.notNull(site, ContentCoreErrorCode.NO_SITE::exception);
		return site;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsSite addSite(SiteDTO dto) {
		boolean checkSiteUnique = this.checkSiteUnique(dto.getName(), dto.getPath(), 0L);
		Assert.isTrue(checkSiteUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("name", "path"));

		long parentId = Objects.requireNonNullElse(dto.getParentId(), 0L);
		CmsSite parent = null;
		if (parentId > 0) {
			PermissionUtils.checkPermission(SitePrivItem.View.getPermissionKey(parentId), dto.getOperator());
			PermissionUtils.checkPermission(SitePrivItem.Edit.getPermissionKey(parentId), dto.getOperator());
			parent = this.lockSite(parentId);
		}

		CmsSite site = new CmsSite();
		site.setSiteId(IdUtils.getSnowflakeId());
		BeanUtils.copyProperties(dto, site, "siteId");
		site.setParentId(parentId);
		site.setAncestors(parent == null ? site.getSiteId().toString() : parent.getAncestors() + "," + site.getSiteId());
		Assert.isTrue(site.getAncestors().length() <= CmsSite.MAX_ANCESTORS_LENGTH,
				ContentCoreErrorCode.SITE_TREE_TOO_DEEP::exception);
		site.setTreeLevel(parent == null ? 1 : parent.getTreeLevel() + 1);
		site.setChildCount(0);

		String siteRoot = SiteUtils.getSiteResourceRoot(dto.getPath());
		Assert.isFalse(new File(siteRoot).exists(), ContentCoreErrorCode.EXISTS_SITE_PATH::exception);
		FileExUtils.mkdirs(siteRoot);

		if (StringUtils.isNotBlank(site.getResourceUrl())) {
        	site.setResourceUrl(Strings.CS.appendIfMissing(site.getResourceUrl(), "/"));
		}
		site.setSortFlag(SortUtils.getDefaultSortValue());
		site.setTopFlag(0L);
		site.createBy(dto.getOperator().getUsername());
		this.save(site);
		if (parent != null) {
			this.changeChildCount(parent, 1);
		}
		this.clearCache(site.getSiteId());
		// 授权给添加人
		this.userPermissionService.grantUserPermissions(
				dto.getOperator().getUserId(),
				SitePermissionType.ID,
				CmsPrivUtils.getAllSitePermissions(site.getSiteId())
		);
		// 重置登录用户权限
		this.userPermissionService.resetLoginUser(dto.getOperator().getUserId());
		this.applicationContext.publishEvent(new AfterSiteAddEvent(this, site, dto));
		return site;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsSite saveSite(SiteDTO dto) {
		boolean checkSiteUnique = this.checkSiteUnique(dto.getName(), dto.getPath(), dto.getSiteId());
		Assert.isTrue(checkSiteUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("name", "path"));

		CmsSite site = this.getById(dto.getSiteId());

		BeanUtils.copyProperties(dto, site, "path", "parentId");
		if (StringUtils.isNotBlank(site.getResourceUrl())) {
			site.setResourceUrl(Strings.CS.appendIfMissing(site.getResourceUrl(), "/"));
		}
		// 发布通道数据处理
        dto.getPublishPipeDatas().forEach(prop -> {
            prop.getProps().entrySet().removeIf(e -> !publishPipeProps.containsKey(IPublishPipeProp.BEAN_PREFIX + e.getKey()));
        });
		Map<String, Map<String, Object>> publishPipeProps = dto.getPublishPipeDatas().stream()
				.collect(Collectors.toMap(PublishPipeProps::getPipeCode, PublishPipeProps::getProps));
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
	public void deleteSite(Long siteId) {
		long parentId = this.getSiteForHierarchyChange(siteId).getParentId();
		List<CmsSite> lockedSites = this.lockSites(List.of(siteId, parentId));
		CmsSite site = requireSite(lockedSites, siteId);
		Assert.isTrue(site.getParentId() == parentId, ContentCoreErrorCode.SITE_PARENT_CHANGED::exception);
		Assert.isTrue(site.getChildCount() == 0, ContentCoreErrorCode.SITE_HAS_CHILDREN::exception);

		applicationContext.publishEvent(new BeforeSiteDeleteEvent(this, site));

		AsyncTaskManager.setTaskMessage(ContentCoreTips.DELETING_SITE, site.getName());
		this.removeById(site.getSiteId());
		if (parentId > 0) {
			this.changeChildCount(requireSite(lockedSites, parentId), -1);
		}
		this.clearCache(site.getSiteId());

		if (EnableSiteDeleteBackupProperty.getValue(site.getConfigProps())) {
			// 备份站点资源目录
			String siteRoot = SiteUtils.getSiteResourceRoot(site);
			String bakDir = CMSConfig.getResourceRoot() + site.getPath() + "_bak_"
					+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			File siteRootFile = new File(siteRoot);
			if (siteRootFile.exists()) {
				try {
					FileUtils.moveDirectory(siteRootFile, new File(bakDir));
				} catch (IOException e) {
					log.error("Move directory {} to {} failed.", siteRoot, bakDir);
				}
			}
		}

		applicationContext.publishEvent(new AfterSiteDeleteEvent(this, site));
	}

	private CmsSite getSiteForHierarchyChange(long siteId) {
		CmsSite site = this.getById(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		return site;
	}

	private CmsSite lockSite(long siteId) {
		return requireSite(this.lockSites(List.of(siteId)), siteId);
	}

	private List<CmsSite> lockSites(List<Long> siteIds) {
		List<Long> ids = siteIds.stream().filter(id -> id > 0).distinct().toList();
		return this.lambdaQuery().in(CmsSite::getSiteId, ids).orderByAsc(CmsSite::getSiteId)
				.last("FOR UPDATE").list();
	}

	private void changeChildCount(CmsSite parent, int delta) {
		this.lambdaUpdate().eq(CmsSite::getSiteId, parent.getSiteId())
				.set(CmsSite::getChildCount, parent.getChildCount() + delta)
				.update();
		TransactionUtils.afterCommit(() -> this.clearCache(parent.getSiteId()));
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
		List<PublishPipeProps> publishPipeProps = dto.getPublishPipeProps();
		for (PublishPipeProps ppp : publishPipeProps) {
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
		// 重新加载StorageClient
		asyncTaskManager.execute(() -> {
			String fileStorageType = FileStorageTypeProperty.getValue(site.getConfigProps());
			IFileStorageType fst = fileStorageService.getFileStorageType(fileStorageType);
			FileStorageHelper fileStorageHelper = FileStorageHelper.of(fst, site);
			fileStorageHelper.reloadClient();
		});
	}

	@Override
	public void clearCache(long siteId) {
		this.siteCache.clear(siteId);
	}
}
