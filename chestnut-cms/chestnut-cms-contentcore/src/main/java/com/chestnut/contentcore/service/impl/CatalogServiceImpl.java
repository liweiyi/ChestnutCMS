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
package com.chestnut.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.*;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.config.CMSConfig;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.core.IProperty;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.core.impl.CatalogType_Common;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.core.impl.InternalDataType_Catalog;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.*;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.listener.event.AfterCatalogDeleteEvent;
import com.chestnut.contentcore.listener.event.AfterCatalogMoveEvent;
import com.chestnut.contentcore.listener.event.AfterCatalogSaveEvent;
import com.chestnut.contentcore.listener.event.BeforeCatalogDeleteEvent;
import com.chestnut.contentcore.mapper.CmsCatalogMapper;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.*;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl extends ServiceImpl<CmsCatalogMapper, CmsCatalog> implements ICatalogService {

	public static final String CACHE_PREFIX_ID = CMSConfig.CachePrefix + "catalog:id:";

	public static final String CACHE_PREFIX_ALIAS = CMSConfig.CachePrefix + "catalog:alias:";

	private final ApplicationContext applicationContext;

	private final ISiteService siteService;

	private final RedisCache redisCache;

	private final RedissonClient redissonClient;

	private final CmsContentMapper contentMapper;

	private final ISysPermissionService permissionService;

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public CmsCatalog getCatalog(Long catalogId) {
		if (!IdUtils.validate(catalogId)) {
			return null;
		}
		CmsCatalog catalog = this.redisCache.getCacheObject(CACHE_PREFIX_ID + catalogId);
		if (Objects.isNull(catalog)) {
			catalog = this.getById(catalogId);
			if (Objects.nonNull(catalog)) {
				this.setCatalogCache(catalog);
			}
		}
		return catalog;
	}

	@Override
	public CmsCatalog getCatalogByAlias(Long siteId, String catalogAlias) {
		if (!IdUtils.validate(siteId) || StringUtils.isEmpty(catalogAlias)) {
			return null;
		}
		Assert.notNull(catalogAlias, () -> CommonErrorCode.NOT_EMPTY.exception("CatalogAlias: " + catalogAlias));
		CmsCatalog catalog = this.redisCache.getCacheObject(CACHE_PREFIX_ALIAS + siteId + ":" + catalogAlias);
		if (Objects.isNull(catalog)) {
			catalog = this.lambdaQuery().eq(CmsCatalog::getSiteId, siteId).eq(CmsCatalog::getAlias, catalogAlias).one();
			if (Objects.nonNull(catalog)) {
				this.setCatalogCache(catalog);
			}
		}
		return catalog;
	}

	@Override
	public boolean checkCatalogUnique(Long siteId, Long catalogId, String alias, String path) {
		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, siteId)
				.and(wrapper -> wrapper.eq(CmsCatalog::getAlias, alias).or().eq(CmsCatalog::getPath, path))
				.ne(catalogId != null && catalogId > 0, CmsCatalog::getCatalogId, catalogId);
		return this.count(q) == 0;
	}

	@Override
	public List<TreeNode<String>> buildCatalogTreeData(List<CmsCatalog> catalogs, BiConsumer<CmsCatalog, TreeNode<String>> consumer) {
		if (Objects.isNull(catalogs)) {
			return List.of();
		}
		List<TreeNode<String>> list = catalogs.stream().map(c -> {
			TreeNode<String> treeNode = new TreeNode<>(String.valueOf(c.getCatalogId()),
					String.valueOf(c.getParentId()), c.getName(), c.getParentId() == 0);
			String internalUrl = InternalUrlUtils.getInternalUrl(InternalDataType_Catalog.ID, c.getCatalogId());
			String logoSrc = InternalUrlUtils.getActualPreviewUrl(c.getLogo());
			Map<String, Object> props = Map.of("path", c.getPath(), "internalUrl", internalUrl, "logo",
					c.getLogo() == null ? "" : c.getLogo(), "logoSrc", logoSrc == null ? "" : logoSrc, "description",
					c.getDescription() == null ? "" : c.getDescription());
			treeNode.setProps(props);
			consumer.accept(c, treeNode);
			return treeNode;
		}).toList();
		return TreeNode.build(list);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsCatalog addCatalog(CatalogAddDTO dto) {
		dto.setPath(CatalogUtils.formatCatalogPath(dto.getPath()));
		Assert.isFalse("/".equals(dto.getPath()), () -> CommonErrorCode.NOT_EMPTY.exception("path"));

		boolean checkCatalogUnique = this.checkCatalogUnique(dto.getSiteId(), null, dto.getAlias(),
				dto.getPath());
		Assert.isTrue(checkCatalogUnique, ContentCoreErrorCode.CONFLICT_CATALOG::exception);

		if (dto.getParentId() == null) {
			dto.setParentId(0L);
		}
		CmsCatalog catalog = new CmsCatalog();
		catalog.setCatalogId(IdUtils.getSnowflakeId());
		catalog.setSiteId(dto.getSiteId());
		catalog.setParentId(dto.getParentId());
		catalog.setName(dto.getName());
		catalog.setAlias(dto.getAlias());
		catalog.setPath(dto.getPath());
		catalog.setCatalogType(dto.getCatalogType());
		catalog.setTreeLevel(1);
		String parentAncestors = StringUtils.EMPTY;
		if (catalog.getParentId() > 0) {
			CmsCatalog parentCatalog = this.getById(catalog.getParentId());
			boolean maxTreeLevelFlag = parentCatalog.getTreeLevel() + 1 <= ContentCoreConsts.CATALOG_MAX_TREE_LEVEL;
			Assert.isTrue(maxTreeLevelFlag, ContentCoreErrorCode.CATALOG_MAX_TREE_LEVEL::exception);

			catalog.setTreeLevel(parentCatalog.getTreeLevel() + 1);
			parentCatalog.setChildCount(parentCatalog.getChildCount() + 1);
			this.updateById(parentCatalog);

			parentAncestors = parentCatalog.getAncestors();
		}
		catalog.setAncestors(CatalogUtils.getCatalogAncestors(parentAncestors, catalog.getCatalogId()));
		catalog.setSortFlag(SortUtils.getDefaultSortValue());
		catalog.setContentCount(0);
		catalog.setChildCount(0);
		catalog.setStaticFlag(YesOrNo.YES);
		catalog.setVisibleFlag(YesOrNo.YES);
		catalog.setTagIgnore(YesOrNo.NO);
		catalog.createBy(dto.getOperator().getUsername());
		this.save(catalog);
		// 授权给添加人
		this.permissionService.grantUserPermission(
				dto.getOperator(),
				CatalogPermissionType.ID,
				CmsPrivUtils.getAllCatalogPermissions(catalog.getCatalogId())
		);
		return catalog;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void batchAddCatalog(CatalogBatchAddDTO dto) {
		CmsCatalog rootParent = null;
		if (IdUtils.validate(dto.getParentId())) {
			rootParent = getCatalog(dto.getParentId());
		}
		List<CmsCatalog> list = this.lambdaQuery()
				.select(CmsCatalog::getAlias, CmsCatalog::getPath)
				.eq(CmsCatalog::getSiteId, dto.getSiteId())
				.list();
		Set<String> aliasSet = list.stream().map(CmsCatalog::getAlias).collect(Collectors.toSet());
		Set<String> pathSet = list.stream().map(CmsCatalog::getPath).collect(Collectors.toSet());

		List<CmsCatalog> catalogs = new ArrayList<>();
		String[] arr = dto.getCatalogs().split("\n");
		Map<Integer, CmsCatalog> lastTreeLevelCatalogs = new HashMap<>();
        for (String item : arr) {
			CmsCatalog catalog = new CmsCatalog();
			catalog.setCatalogId(IdUtils.getSnowflakeId());
			catalog.setName(item.trim());
			catalog.setParentId(0L);
			catalog.setTreeLevel(1);
			catalog.setAlias(ChineseSpelling.getCapitalizedSpelling(catalog.getName()).toLowerCase());
			catalog.setPath(catalog.getAlias() + StringUtils.SLASH);
			catalog.setChildCount(0);

			int treeLevel = StringUtils.countMatches(item, " ") / 2 + 1;
			CmsCatalog parent = rootParent;
			if (treeLevel > 1) {
				parent = lastTreeLevelCatalogs.get(treeLevel - 1);
			}
			if (Objects.nonNull(parent)) {
				catalog.setParentId(parent.getCatalogId());
				catalog.setTreeLevel(parent.getTreeLevel() + 1) ;

				catalog.setAlias(parent.getAlias() + StringUtils.Underline + catalog.getAlias());
				catalog.setPath(parent.getPath() + catalog.getPath());

				parent.setChildCount(parent.getChildCount() + 1);
			}
			int index = 1;
			while(aliasSet.contains(catalog.getAlias())) {
				index++;
				catalog.setAlias(catalog.getAlias() + index);
			}
			index = 1;
			if (pathSet.contains(catalog.getPath())) {
				index++;
				String path = StringUtils.substringBeforeLast(catalog.getPath(), StringUtils.SLASH);
				catalog.setPath(path + index + StringUtils.SLASH);
			}
			catalog.setAncestors(CatalogUtils.getCatalogAncestors(parent, catalog.getCatalogId()));
			catalog.setSiteId(dto.getSiteId());
			catalog.setCatalogType(CatalogType_Common.ID);
			catalog.setSortFlag(SortUtils.getDefaultSortValue());
			catalog.setContentCount(0);
			catalog.setStaticFlag(YesOrNo.YES);
			catalog.setVisibleFlag(YesOrNo.YES);
			catalog.setTagIgnore(YesOrNo.NO);
			catalog.createBy(dto.getOperator().getUsername());
			catalogs.add(catalog);

			lastTreeLevelCatalogs.put(treeLevel, catalog);
			aliasSet.add(catalog.getAlias());
			pathSet.add(catalog.getPath());
        }

		if (saveBatch(catalogs)) {
			// 授权给添加人
			catalogs.forEach(catalog -> {
				this.permissionService.grantUserPermission(
						dto.getOperator(),
						CatalogPermissionType.ID,
						CmsPrivUtils.getAllCatalogPermissions(catalog.getCatalogId())
				);
			});
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsCatalog editCatalog(CatalogUpdateDTO dto) {
		dto.setPath(CatalogUtils.formatCatalogPath(dto.getPath()));
		Assert.isFalse("/".equals(dto.getPath()), () -> CommonErrorCode.NOT_EMPTY.exception("path"));

		CmsCatalog catalog = this.getById(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));

		boolean checkCatalogUnique = this.checkCatalogUnique(catalog.getSiteId(), catalog.getCatalogId(),
				dto.getAlias(), dto.getPath());
		Assert.isTrue(checkCatalogUnique, ContentCoreErrorCode.CONFLICT_CATALOG::exception);
		// 校验内链
		checkRedirectUrl(dto.getCatalogType(), dto.getRedirectUrl());

		String oldPath = catalog.getPath();
		BeanUtils.copyProperties(dto, catalog);
		// 发布通道数据处理
		Map<String, Map<String, Object>> publishPipeProps = dto.getPublishPipeDatas().stream()
				.collect(Collectors.toMap(PublishPipeProp::getPipeCode, PublishPipeProp::getProps));
		catalog.setPublishPipeProps(publishPipeProps);
		catalog.updateBy(dto.getOperator().getUsername());
		this.updateById(catalog);

		this.clearCache(catalog);
		this.applicationContext.publishEvent(new AfterCatalogSaveEvent(this, catalog, oldPath, dto.getParams()));
		return catalog;
	}

	void checkRedirectUrl(String catalogType, String redirectUrl) {
		if (CatalogType_Link.ID.equals(catalogType)) {
			// 校验redirectUrl是否是链接到了内部链接数据
			InternalURL internalURL = InternalUrlUtils.parseInternalUrl(redirectUrl);
			if (Objects.nonNull(internalURL)) {
				IInternalDataType idt = ContentCoreUtils.getInternalDataType(internalURL.getType());
				Assert.isFalse(idt.isLinkData(internalURL.getId()),
						ContentCoreErrorCode.DENY_LINK_TO_LINK_INTERNAL_DATA::exception);
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsCatalog deleteCatalog(long catalogId, LoginUser operator) {
		CmsCatalog catalog = this.getById(catalogId);
		long childCount = lambdaQuery().eq(CmsCatalog::getParentId, catalog.getCatalogId()).count();
		Assert.isTrue(childCount == 0, ContentCoreErrorCode.DEL_CHILD_FIRST::exception);
		// 删除前事件发布
		applicationContext.publishEvent(new BeforeCatalogDeleteEvent(this, catalog, operator));
		// 删除栏目
		AsyncTaskManager.setTaskMessage("正在删除栏目数据");
		if (catalog.getParentId() > 0) {
			CmsCatalog parentCatalog = getById(catalog.getParentId());
			parentCatalog.setChildCount(parentCatalog.getChildCount() - 1);
			updateById(parentCatalog);
		}
		removeById(catalogId);
		clearCache(catalog);

		applicationContext.publishEvent(new AfterCatalogDeleteEvent(this, catalog));
		return catalog;
	}

	@Override
	public String getCatalogLink(CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		return CatalogUtils.getCatalogLink(site, catalog, pageIndex, publishPipeCode, isPreview);
	}

	@Override
	public String getCatalogListLink(CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		return CatalogUtils.getCatalogListLink(site, catalog, pageIndex, publishPipeCode, isPreview);
	}

	@Override
	public void applyConfigPropsToChildren(CatalogApplyConfigPropsDTO dto) {
		CmsCatalog catalog = this.getCatalog(dto.getCatalogId());

		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<>();
		if (StringUtils.isEmpty(dto.getToCatalogIds())) {
			q.likeRight(CmsCatalog::getAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
		} else {
			q.in(CmsCatalog::getCatalogId, dto.getToCatalogIds());
		}
		List<CmsCatalog> toCatalogs = this.list(q);
		for (CmsCatalog toCatalog : toCatalogs) {
			if (dto.isAllExtends()) {
				toCatalog.setConfigProps(catalog.getConfigProps());
			} else if (StringUtils.isNotEmpty(dto.getConfigPropKeys())) {
				dto.getConfigPropKeys().forEach(propKey -> {
					toCatalog.getConfigProps().put(propKey, catalog.getConfigProps().get(propKey));
				});
			}
			toCatalog.updateBy(dto.getOperator().getUsername());
		}
		this.updateBatchById(toCatalogs);
		toCatalogs.forEach(this::clearCache);
	}

	@Override
	public void applyPublishPipePropsToChildren(CatalogApplyPublishPipeDTO dto) {
		CmsCatalog catalog = this.getCatalog(dto.getCatalogId());

		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<>();
		q.likeRight(CmsCatalog::getAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
		List<CmsCatalog> toCatalogs = this.list(q);
		for (CmsCatalog toCatalog : toCatalogs) {
			Map<String, Object> publishPipeProps = toCatalog.getPublishPipeProps(dto.getPublishPipeCode());
			for (String propKey : dto.getPublishPipePropKeys()) {
				publishPipeProps.put(propKey, catalog.getPublishPipeProps(dto.getPublishPipeCode()).get(propKey));
			}
			toCatalog.updateBy(dto.getOperator().getUsername());
		}
		this.updateBatchById(toCatalogs);
		toCatalogs.forEach(this::clearCache);
	}

	@Override
	public void applySiteDefaultTemplateToCatalog(SiteDefaultTemplateDTO dto) {
		CmsSite site = this.siteService.getSite(dto.getSiteId());

		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>().in(CmsCatalog::getCatalogId,
				dto.getToCatalogIds());
		List<CmsCatalog> toCatalogs = this.list(q);
		if (!toCatalogs.isEmpty()) {
			for (CmsCatalog toCatalog : toCatalogs) {
				List<PublishPipeProp> publishPipeProps = dto.getPublishPipeProps();
				for (PublishPipeProp publishPipeProp : publishPipeProps) {
					Map<String, Object> sitePublishPipeProp = site.getPublishPipeProps()
							.get(publishPipeProp.getPipeCode());
					Map<String, Object> catalogPublishPipeProp = toCatalog
							.getPublishPipeProps(publishPipeProp.getPipeCode());
					publishPipeProp.getProps().keySet().forEach(key -> {
						catalogPublishPipeProp.put(key, sitePublishPipeProp.get(key));
					});
				}
				toCatalog.updateBy(dto.getOperator().getUsername());
			}
			this.updateBatchById(toCatalogs);
		}
	}

	@Override
	public void clearCache(CmsCatalog catalog) {
		this.redisCache.deleteObject(CACHE_PREFIX_ID + catalog.getCatalogId());
		this.redisCache.deleteObject(CACHE_PREFIX_ALIAS + catalog.getSiteId() + ":" + catalog.getAlias());
	}

	private void setCatalogCache(CmsCatalog catalog) {
		this.redisCache.setCacheObject(CACHE_PREFIX_ID + catalog.getCatalogId(), catalog);
		this.redisCache.setCacheObject(CACHE_PREFIX_ALIAS + catalog.getSiteId() + ":" + catalog.getAlias(), catalog);
	}

	@Override
	public void changeVisible(Long catalogId, String visible) {
		CmsCatalog catalog = this.getCatalog(catalogId);
		if (StringUtils.equals(visible, catalog.getVisibleFlag())) {
			return;
		}
		catalog.setVisibleFlag(YesOrNo.isYes(visible) ? YesOrNo.YES : YesOrNo.NO);
		this.updateById(catalog);
		this.clearCache(catalog);
	}

	@Override
	public AsyncTask moveCatalog(CmsCatalog fromCatalog, CmsCatalog toCatalog) {
		// 所有需要迁移的子栏目，按Ancestors排序依次处理
		List<CmsCatalog> children = this.lambdaQuery().ne(CmsCatalog::getCatalogId, fromCatalog.getCatalogId())
				.likeRight(CmsCatalog::getAncestors, fromCatalog.getAncestors()).orderByAsc(CmsCatalog::getAncestors)
				.list();
		// 判断栏目ancestors长度是否会超过限制
		int baseTreeLevel = Objects.isNull(toCatalog) ? 1 : toCatalog.getTreeLevel() + 1;
		int maxTreelevel = baseTreeLevel;
		for (CmsCatalog catalog : children) {
			maxTreelevel = Math.max(maxTreelevel, catalog.getTreeLevel() - fromCatalog.getTreeLevel() + baseTreeLevel);
		}
		Assert.isTrue(ContentCoreConsts.CATALOG_MAX_TREE_LEVEL >= maxTreelevel,
				ContentCoreErrorCode.CATALOG_MAX_TREE_LEVEL::exception);
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() {
				moveCatalog0(fromCatalog, toCatalog, children);
			}
		};
		// 设置唯一任务ID避免同步执行，可能会导致数据错乱。
		task.setTaskId("CatalogMove");
		this.asyncTaskManager.execute(task);
		return task;
	}

	@Transactional(rollbackFor = Throwable.class)
	private void moveCatalog0(CmsCatalog fromCatalog, CmsCatalog toCatalog, List<CmsCatalog> children) {
		Map<Long, CmsCatalog> invokedCatalogs = new HashMap<>();
		AsyncTaskManager.setTaskPercent(10);
		// 1、原父级栏目子节点数-1
		if (fromCatalog.getParentId() > 0) {
			AsyncTaskManager.setTaskMessage("更新转移栏目原父级栏目数据");
			CmsCatalog parent = this.getById(fromCatalog.getParentId());
			parent.setChildCount(parent.getChildCount() - 1);
			invokedCatalogs.put(parent.getCatalogId(), parent);
		}
		// 2、来源栏目修改相关属性
		AsyncTaskManager.setTaskMessage("更新转移栏目数据");
		fromCatalog.setParentId(Objects.isNull(toCatalog) ? 0 : toCatalog.getCatalogId());
		String fromCatalogAncestors = CatalogUtils.getCatalogAncestors(toCatalog, fromCatalog.getCatalogId());
		fromCatalog.setAncestors(fromCatalogAncestors);
		fromCatalog.setTreeLevel(Objects.isNull(toCatalog) ? 1 : toCatalog.getTreeLevel() + 1);
		fromCatalog.setSortFlag(SortUtils.getDefaultSortValue());
		invokedCatalogs.put(fromCatalog.getCatalogId(), fromCatalog);
		// 3、依次处理所有栏目
        for (CmsCatalog child : children) {
            AsyncTaskManager.setTaskMessage("更新转移栏目子栏目数据");
            CmsCatalog parent = invokedCatalogs.get(child.getParentId());
            child.setAncestors(CatalogUtils.getCatalogAncestors(parent.getAncestors(), child.getCatalogId()));
            child.setTreeLevel(parent.getTreeLevel() + 1);
            invokedCatalogs.put(child.getCatalogId(), child);
        }
		// 4、目标栏目子栏目数+1
		if (Objects.nonNull(toCatalog)) {
			AsyncTaskManager.setTaskMessage("更新转移目标栏目数据");
			toCatalog.setChildCount(toCatalog.getChildCount() + 1);
			invokedCatalogs.put(toCatalog.getCatalogId(), toCatalog);
		}
		AsyncTaskManager.setTaskPercent(20);
		// 5、批量更新数据库
		invokedCatalogs.values().forEach(catalog -> {
			this.lambdaUpdate().set(CmsCatalog::getParentId, catalog.getParentId())
					.set(CmsCatalog::getAncestors, catalog.getAncestors())
					.set(CmsCatalog::getTreeLevel, catalog.getTreeLevel())
					.set(CmsCatalog::getChildCount, catalog.getChildCount())
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
		});
		AsyncTaskManager.setTaskPercent(30);
		// 6、更新缓存
		invokedCatalogs.values().forEach(this::clearCache);
		// 7、更新除目标栏目外的所有栏目内容数据
		AsyncTaskManager.setTaskProgressInfo(40, "更新转移栏目及其子栏目内容");
		invokedCatalogs.values().forEach(catalog -> {
			if (toCatalog == null || !Objects.equals(catalog.getCatalogId(), toCatalog.getCatalogId())) {
				new LambdaUpdateChainWrapper<>(contentMapper)
						.set(CmsContent::getCatalogAncestors, catalog.getAncestors())
						.eq(CmsContent::getCatalogId, catalog.getCatalogId()).update();
			}
		});
		// 8、其他扩展，例如：重建栏目内容索引
		this.applicationContext.publishEvent(new AfterCatalogMoveEvent(this, fromCatalog, toCatalog, children));
	}

	@Override
	public void setStaticPath(CmsCatalog catalog, TemplateContext context, boolean hasIndex) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
		context.setDirectory(siteRoot + catalog.getPath());
		String suffix = site.getStaticSuffix(context.getPublishPipeCode());
		String name = hasIndex ? "list" : "index";
		context.setFirstFileName(name + StringUtils.DOT + suffix);
		context.setOtherFileName(name + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + suffix);
	}

	@Override
	public void saveCatalogExtends(Long catalogId, Map<String, String> configs, String operator) {
		CmsCatalog catalog = this.getCatalog(catalogId);
		ConfigPropertyUtils.filterConfigProps(configs, catalog.getConfigProps(), IProperty.UseType.Catalog);

		catalog.setConfigProps(configs);
		catalog.updateBy(operator);
		this.updateById(catalog);
		this.clearCache(catalog);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sortCatalog(Long catalogId, Integer sort) {
		CmsCatalog catalog = this.getCatalog(catalogId);
		if (sort < 0) {
			// 上移
			List<CmsCatalog> beforeCatalogs = this.lambdaQuery()
					.select(List.of(CmsCatalog::getSiteId, CmsCatalog::getCatalogId, CmsCatalog::getAlias,
							CmsCatalog::getSortFlag))
					.eq(CmsCatalog::getSiteId, catalog.getSiteId()).eq(CmsCatalog::getParentId, catalog.getParentId())
					.lt(CmsCatalog::getSortFlag, catalog.getSortFlag()).orderByDesc(CmsCatalog::getSortFlag)
					.page(new Page<>(1, Math.abs(sort), false)).getRecords();
			if (beforeCatalogs.isEmpty()) {
				return; // 无需排序
			}
			CmsCatalog targetCatalog = beforeCatalogs.get(beforeCatalogs.size() - 1);
			// 更新排序值
			this.catalogSortPlusOne(targetCatalog.getSortFlag(), catalog.getSortFlag());
			this.lambdaUpdate().set(CmsCatalog::getSortFlag, targetCatalog.getSortFlag())
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
			beforeCatalogs.forEach(this::clearCache);
		} else {
			// 下移
			List<CmsCatalog> afterCatalogs = this.lambdaQuery()
					.select(List.of(CmsCatalog::getSiteId, CmsCatalog::getCatalogId, CmsCatalog::getAlias,
							CmsCatalog::getSortFlag))
					.eq(CmsCatalog::getSiteId, catalog.getSiteId()).eq(CmsCatalog::getParentId, catalog.getParentId())
					.gt(CmsCatalog::getSortFlag, catalog.getSortFlag()).orderByAsc(CmsCatalog::getSortFlag)
					.page(new Page<>(1, sort, false)).getRecords();
			if (afterCatalogs.isEmpty()) {
				return; // 无需排序
			}
			CmsCatalog targetCatalog = afterCatalogs.get(afterCatalogs.size() - 1);
			// 更新排序值
			this.catalogSortMinusOne(catalog.getSortFlag(), targetCatalog.getSortFlag());
			this.lambdaUpdate().set(CmsCatalog::getSortFlag, targetCatalog.getSortFlag())
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
			afterCatalogs.forEach(this::clearCache);
		}
		this.clearCache(catalog);
	}

	/**
	 * 排序标识范围内[startSort, endSort)的所有栏目排序值+1
	 */
	private void catalogSortPlusOne(long startSort, long endSort) {
		List<CmsCatalog> catalogs = this.lambdaQuery()
				.select(CmsCatalog::getCatalogId, CmsCatalog::getSortFlag)
				.ge(CmsCatalog::getSortFlag, startSort)
				.lt(CmsCatalog::getSortFlag, endSort)
				.list();
		catalogs.forEach(catalog -> {
			this.lambdaUpdate().set(CmsCatalog::getSortFlag, catalog.getSortFlag() + 1)
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
		});
	}

	/**
	 * 排序标识范围内(startSort, endSort]的所有栏目排序值-1
	 */
	private void catalogSortMinusOne(long startSort, long endSort) {
		List<CmsCatalog> catalogs = this.lambdaQuery()
				.select(CmsCatalog::getCatalogId, CmsCatalog::getSortFlag)
				.gt(CmsCatalog::getSortFlag, startSort)
				.le(CmsCatalog::getSortFlag, endSort)
				.list();
		catalogs.forEach(catalog -> {
			this.lambdaUpdate().set(CmsCatalog::getSortFlag, catalog.getSortFlag() - 1)
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
		});
	}

	@Override
	public void changeContentCount(Long catalogId, int delta) {
		RLock lock = redissonClient.getLock("Catalog-" + catalogId);
		lock.lock();
		try {
			CmsCatalog catalog = getById(catalogId);
			lambdaUpdate().set(CmsCatalog::getContentCount, catalog.getContentCount() + delta)
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
		} finally {
			lock.unlock();
		}
	}
}
