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
package com.chestnut.contentcore.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.chestnut.common.async.AsyncTask;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.extend.annotation.XssIgnore;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ChineseSpelling;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.core.ICatalogType;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.core.IProperty.UseType;
import com.chestnut.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.chestnut.contentcore.core.impl.CatalogType_Link;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.*;
import com.chestnut.contentcore.domain.pojo.PublishPipeProps;
import com.chestnut.contentcore.domain.vo.CatalogVO;
import com.chestnut.contentcore.enums.ContentTips;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.*;
import com.chestnut.contentcore.user.preference.CatalogTreeExpandModePreference;
import com.chestnut.contentcore.util.*;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 栏目管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/catalog")
public class CatalogController extends CmsRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IPublishService publishService;

	private final List<ICatalogType> catalogTypes;

	private final List<IContentType> contentTypes;

	private final AsyncTaskManager asyncTaskManager;

	private final IResourceService resourceService;

	/**
	 * 查询栏目数据列表
	 */
	@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.CatalogView)
	@GetMapping("/list")
	public R<?> list() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		List<CmsCatalog> list = catalogService.lambdaQuery().list().stream().filter(c ->
				loginUser.hasPermission(CatalogPrivItem.View.getPermissionKey(c.getCatalogId()))).toList();
		return this.bindDataTable(list);
	}

	/**
	 * 查询栏目详情数据
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:View:${#catalogId}")
	@GetMapping("/detail/{catalogId}")
	public R<?> catalogInfo(@PathVariable @LongId Long catalogId) {
		CmsCatalog catalog = catalogService.getById(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));

		CatalogVO dto = CatalogVO.newInstance(catalog);
		CmsSite site = siteService.getById(dto.getSiteId());
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
		resourceService.dealDefaultThumbnail(site, dto.getLogo(), dto::setLogoSrc);
		// 发布通道数据
		List<PublishPipeProps> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
				PublishPipePropUseType.Catalog, catalog.getPublishPipeProps());
		dto.setPublishPipeDatas(publishPipeProps);
		return R.ok(dto);
	}

	/**
	 * 新增栏目数据
	 */
	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.ResourceView, "Site:AddCatalog:${#_header['" + ContentCoreConsts.Header_CurrentSite + "']}"},
		mode = SaMode.AND
	)
	@Log(title = "新增栏目", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<?> addCatalog(@RequestBody @Validated CatalogAddDTO dto) {
		CmsSite currentSite = this.getCurrentSite();
		dto.setSiteId(currentSite.getSiteId());
		return R.ok(this.catalogService.addCatalog(dto));
	}

	/**
	 * 批量新增栏目数据
	 */
	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.ResourceView, "Site:AddCatalog:${#_header[" + ContentCoreConsts.Header_CurrentSite + "]}"},
			mode = SaMode.AND
	)
	@Log(title = "批量新增栏目", businessType = BusinessType.INSERT)
	@PostMapping("/batchAdd")
	public R<?> batchAddCatalog(@RequestBody @Validated CatalogBatchAddDTO dto) {
		CmsSite currentSite = this.getCurrentSite();
		dto.setSiteId(currentSite.getSiteId());
		this.catalogService.batchAddCatalog(dto);
		return R.ok();
	}

	/**
	 * 修改栏目数据
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:Edit:${#dto.catalogId}")
	@Log(title = "编辑栏目", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<?> editCatalog(@RequestBody @Validated CatalogUpdateDTO dto) throws IOException {
		this.catalogService.editCatalog(dto);
		return R.ok();
	}

	/**
	 * 删除栏目数据
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:Delete:${#catalogId}")
	@Log(title = "删除", businessType = BusinessType.DELETE)
	@PostMapping("/delete/{catalogId}")
	public R<String> deleteCatalog(@PathVariable("catalogId") @LongId Long catalogId) {
		CmsCatalog catalog = catalogService.getById(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalog));
		LoginUser operator = StpAdminUtil.getLoginUser();
		AsyncTask task = new AsyncTask("Catalog-" + catalogId) {

			@Override
			public void run0() {
				List<CmsCatalog> list = catalogService.lambdaQuery()
						.likeRight(CmsCatalog::getAncestors, catalog.getAncestors())
						.list();
				list.sort((c1, c2) -> c2.getTreeLevel() - c1.getTreeLevel());
				list.forEach(c -> {
					catalogService.deleteCatalog(c, operator);
				});
                this.setProgressInfo(100, ContentTips.DELETE_SUCCESS);
			}
		};
		this.asyncTaskManager.execute(task);
		return R.ok(task.getTaskId());
	}

	/**
	 * 显示/隐藏栏目
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:ShowHide:${#dto.catalogId}")
	@Log(title = "显隐栏目", businessType = BusinessType.UPDATE)
	@PostMapping("/visible")
	public R<String> changeVisible(@RequestBody @Validated ChangeCatalogVisibleDTO dto) {
		catalogService.changeVisible(dto.getCatalogId(), dto.getVisible());
		return R.ok();
	}

	/**
	 * 栏目树结构数据
	 */
	@Priv(type = AdminUserType.TYPE, value = CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER)
	@GetMapping("/treeData")
	public R<?> treeData(@RequestParam(required = false, defaultValue = "false") Boolean disableLink) {
		CmsSite site = this.getCurrentSite();
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		List<CmsCatalog> catalogs = this.catalogService.lambdaQuery().eq(CmsCatalog::getSiteId, site.getSiteId())
				.orderByAsc(CmsCatalog::getSortFlag).list().stream().filter(c -> loginUser
						.hasPermission(CatalogPrivItem.View.getPermissionKey(c.getCatalogId())))
				.toList();
		List<TreeNode<String>> treeData = catalogService.buildCatalogTreeData(catalogs, (catalog, node) -> {
			if (disableLink) {
				node.setDisabled(CatalogType_Link.ID.equals(catalog.getCatalogType()));
			}
		});
		return R.ok(Map.of(
				"rows", treeData,
				"siteName", site.getName(),
				"expandMode", CatalogTreeExpandModePreference.getValue(loginUser))
		);
	}

	/**
	 * 内容类型数据
	 */
	@GetMapping("/getContentTypes")
	public R<?> getContentTypes() {
		List<Map<String, String>> list = this.contentTypes.stream().sorted(Comparator.comparingInt(IContentType::getOrder))
				.map(ct -> Map.of("id", ct.getId(), "name", I18nUtils.get(ct.getName()))).toList();
		return R.ok(list);
	}

	/**
	 * 栏目类型数据
	 */
	@GetMapping("/getCatalogTypes")
	public R<?> getCatalogTypes() {
		List<Map<String, String>> list = this.catalogTypes.stream()
				.map(ct -> Map.of("id", ct.getId(), "name", I18nUtils.get(ct.getName()))).toList();
		return R.ok(list);
	}

	/**
	 * 发布栏目
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:Publish:${#dto.catalogId}")
	@Log(title = "发布栏目", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<String> publish(@RequestBody @Validated PublishCatalogDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));

		if (!dto.getPublishChild() && !dto.getPublishDetail() && (!catalog.isStaticize() || !catalog.isVisible()
				|| catalog.getCatalogType().equals(CatalogType_Link.ID))) {
			throw ContentCoreErrorCode.CATALOG_CANNOT_PUBLISH.exception();
		}
		AsyncTask task = this.publishService.publishCatalog(catalog, dto.getPublishChild(), dto.getPublishDetail(),
				dto.getPublishStatus(), Operator.of(StpAdminUtil.getLoginUser()));
		return R.ok(task.getTaskId());
	}

	/**
	 * 获取栏目扩展配置
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:View:${#catalogId}")
	@GetMapping("/extends")
	public R<?> getCatalogExtends(@RequestParam("catalogId") @LongId Long catalogId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));

		Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(catalog.getConfigProps(),
				UseType.Catalog);
		configProps.put("siteId", catalog.getSiteId());
		configProps.put("PreviewPrefix", SiteUtils.getResourcePreviewPrefix(this.siteService.getSite(catalog.getSiteId())));
		return R.ok(configProps);
	}

	/**
	 * 保存栏目扩展配置
	 */
	@XssIgnore
	@Priv(type = AdminUserType.TYPE, value = "Catalog:Edit:${#catalogId}")
	@Log(title = "栏目扩展", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PostMapping("/extends/{catalogId}")
	public R<?> saveCatalogExtends(@PathVariable("catalogId") @LongId Long catalogId,
			@RequestBody @NotNull Map<String, String> configs) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));

		this.catalogService.saveCatalogExtends(catalogId, configs, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 扩展配置应用到子栏目
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:Edit:${#dto.catalogId}")
	@Log(title = "扩展配置2子栏目", businessType = BusinessType.UPDATE)
	@PostMapping("/apply_children/config_props")
	public R<?> applyConfigPropsToChildren(@RequestBody @Validated CatalogApplyConfigPropsDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));

		this.catalogService.applyConfigPropsToChildren(dto);
		return R.ok();
	}

	/**
	 * 发布通道配置应用到子栏目
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:Edit:${#dto.catalogId}")
	@Log(title = "发布通道配置2子栏目", businessType = BusinessType.UPDATE)
	@PostMapping("/apply_children/publish_pipe")
	public R<?> applyPublishPipePropsToChildren(@RequestBody @Validated CatalogApplyPublishPipeDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));

		this.catalogService.applyPublishPipePropsToChildren(dto);
		return R.ok();
	}

	/**
	 * 移动栏目
	 */
	@Priv(type = AdminUserType.TYPE, value = { "Catalog:Move:${#fromCatalogId}", "Catalog:Move:${#toCatalogId}" }, mode = SaMode.AND)
	@Log(title = "移动栏目", businessType = BusinessType.UPDATE)
	@PostMapping("/move/{from}/{to}")
	public R<?> moveCatalog(@PathVariable("from") @LongId Long fromCatalogId,
			@PathVariable("to") @NotNull @Min(0) Long toCatalogId) {
		CmsCatalog fromCatalog = this.catalogService.getCatalog(fromCatalogId);
		Assert.notNull(fromCatalog,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("fromCatalogId", fromCatalogId));

		CmsCatalog toCatalog = null;
		if (IdUtils.validate(toCatalogId)) {
			toCatalog = this.catalogService.getCatalog(toCatalogId);
			Assert.notNull(toCatalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("toCatalogId", toCatalogId));

			// 目标栏目不能是源栏目的子栏目或自身，且目标栏目不是当前栏目的父级栏目
			boolean isSelfOfChild = toCatalog.getAncestors().startsWith(fromCatalog.getAncestors())
					|| toCatalog.getCatalogId().equals(fromCatalog.getParentId());
			Assert.isFalse(isSelfOfChild, ContentCoreErrorCode.CATALOG_MOVE_TO_SELF_OR_CHILD::exception);
		} else {
			Assert.isFalse(fromCatalog.getParentId() == 0, ContentCoreErrorCode.CATALOG_MOVE_TO_SELF_OR_CHILD::exception);
		}
		AsyncTask task = this.catalogService.moveCatalog(fromCatalog, toCatalog);
		return R.ok(task.getTaskId());
	}

	@Priv(type = AdminUserType.TYPE, value = "Catalog:Sort:${#dto.catalogId}")
	@Log(title = "栏目排序", businessType = BusinessType.UPDATE)
	@PostMapping("/sort")
	public R<?> sortCatalog(@RequestBody @Validated SortCatalogDTO dto) {
		if (dto.getSort() == 0) {
			return R.fail("排序数值不能为0");
		}
		this.catalogService.sortCatalog(dto.getCatalogId(), dto.getSort());
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/spelling")
	public R<?> getChineseCapitalizedSpellingForAliasAndPath(@RequestBody CatalogNameToSpellingDTO dto) {
		CmsCatalog parent = catalogService.getCatalog(dto.getParentId());
		String spelling = ChineseSpelling.getCapitalizedSpelling(dto.getName()).toLowerCase();
		String alias = spelling;
		String path = spelling + StringUtils.SLASH;
		if (Objects.nonNull(parent)) {
			alias = parent.getAlias() + StringUtils.Underline + alias;
			path = parent.getPath() + path;
		}
		return R.ok(Map.of("alias", alias, "path", path));
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/tree")
	public R<String> getCatalogTree(@RequestParam Long catalogId, HttpServletRequest request) {
		CmsSite site = getCurrentSite();
		CmsCatalog parent = null;
		if (IdUtils.validate(catalogId)) {
			parent = catalogService.getCatalog(catalogId);
		}
		LambdaQueryChainWrapper<CmsCatalog> q = catalogService.lambdaQuery()
				.select(CmsCatalog::getName, CmsCatalog::getAncestors, CmsCatalog::getTreeLevel)
				.eq(CmsCatalog::getSiteId, site.getSiteId());
		if (Objects.nonNull(parent)) {
			q.likeRight(CmsCatalog::getAncestors, parent.getAncestors());
		}
		List<CmsCatalog> list = q.list();
		list.sort(Comparator.comparing(CmsCatalog::getAncestors));
		StringBuilder sb = new StringBuilder();
		list.forEach(catalog -> {
			String prefix = StringUtils.leftPad("", (catalog.getTreeLevel() -1) * 2);
			sb.append(prefix).append(catalog.getName()).append(StringUtils.LF);
		});
		return R.ok(sb.toString());
	}

	@Priv(type = AdminUserType.TYPE, value = "Catalog:Edit:${#catalogId}")
	@Log(title = "清空", businessType = BusinessType.UPDATE)
	@PostMapping("/clear")
	public R<String> clearCatalog(@RequestBody ClearCatalogDTO dto) {
		AsyncTask task = catalogService.clearCatalog(dto);
		return R.ok(task.getTaskId());
	}

	@Priv(type = AdminUserType.TYPE, value = "Catalog:Edit:${#catalogId}")
	@Log(title = "合并", businessType = BusinessType.UPDATE)
	@PostMapping("/merge")
	public R<String> mergeCatalog(@RequestBody MergeCatalogDTO dto) {
		AsyncTask task = catalogService.mergeCatalogs(dto);
		return R.ok(task.getTaskId());
	}
}
