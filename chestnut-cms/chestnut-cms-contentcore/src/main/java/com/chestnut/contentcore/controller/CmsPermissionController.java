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
package com.chestnut.contentcore.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.SaveCatalogPermissionDTO;
import com.chestnut.contentcore.domain.dto.SavePageWidgetPermissionDTO;
import com.chestnut.contentcore.domain.dto.SaveSitePermissionDTO;
import com.chestnut.contentcore.domain.vo.CatalogPrivVO;
import com.chestnut.contentcore.domain.vo.PageWidgetPrivVO;
import com.chestnut.contentcore.domain.vo.SitePrivVO;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.chestnut.contentcore.perms.PageWidgetPermissionType;
import com.chestnut.contentcore.perms.PrivItem;
import com.chestnut.contentcore.perms.SitePermissionType;
import com.chestnut.contentcore.perms.SitePermissionType.SitePrivItem;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.domain.SysPermission;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 站点/栏目/内容权限
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/perms")
public class CmsPermissionController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPageWidgetService pageWidgetService;

	private final ISysPermissionService permissionService;

	private final SitePermissionType sitePermissionType;

	private final CatalogPermissionType catalogPermissionType;

	private final PageWidgetPermissionType pageWidgetPermissionType;

	@GetMapping("/site")
	public R<?> getSitePermissions(@RequestParam String ownerType, @RequestParam String owner) {
		List<CmsSite> sites = this.siteService.lambdaQuery().list();

		Set<String> permissionKeys = new HashSet<>();
		SysPermission permission = this.permissionService.getPermission(ownerType, owner);
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(SitePermissionType.ID);
			permissionKeys.addAll(this.sitePermissionType.deserialize(json));
		}
		Set<String> inheritedPerms = this.permissionService.getInheritedPermissionKeys(ownerType, owner, SitePermissionType.ID);
		List<SitePrivVO> sitePrivs = sites.stream().map(site -> {
			SitePrivVO vo = new SitePrivVO();
			vo.setSiteId(site.getSiteId());
			vo.setName(site.getName());
			SitePrivItem[] values = SitePrivItem.values();
			for (SitePrivItem privItem : values) {
				String permissionKey = privItem.getPermissionKey(site.getSiteId());
				boolean has = permissionKeys.contains(permissionKey);
				boolean inherited = inheritedPerms.contains(permissionKey);
				if (has || inherited) {
					vo.getPerms().put(privItem.name(), new PrivItem(true, inherited));
				}
			}
			return vo;
		}).toList();

		List<Map<String, String>> siteSubPrivs = Stream.of(SitePrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();

		return R.ok(Map.of("sitePrivs", sitePrivs, "sitePrivItems", siteSubPrivs));
	}

	@PutMapping("/site")
	public R<?> saveSitePermissions(@RequestBody @Validated SaveSitePermissionDTO dto) {
		Set<String> perms = new HashSet<>();
		dto.getPerms().forEach(vo -> {
			vo.getPerms().forEach((k, v) -> {
				if (v.isGranted() && !v.isInherited()) {
					perms.add(SitePrivItem.valueOf(k).getPermissionKey(vo.getSiteId()));
				}
			});
		});
		this.permissionService.savePermissions(dto.getOwnerType(), dto.getOwner(), perms,
				SitePermissionType.ID, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	@GetMapping("/site/options")
	public R<?> getSiteOptions(@RequestParam String ownerType, @RequestParam String owner) {
		List<CmsSite> list = this.siteService.lambdaQuery()
				.select(List.of(CmsSite::getSiteId, CmsSite::getName))
				.list();
		Set<String> permissionKeys = this.permissionService.getPermissionKeys(ownerType, owner, SitePermissionType.ID);
		Set<String> inheritedPermissionKeys = this.permissionService.getInheritedPermissionKeys(ownerType, owner, SitePermissionType.ID);
		permissionKeys.addAll(inheritedPermissionKeys);
		List<Map<String, Object>> data = list.stream()
				.filter(site -> permissionKeys.contains(SitePrivItem.View.getPermissionKey(site.getSiteId())))
				.map(site -> {
					Map<String, Object> map = new HashMap<>();
					map.put("id", site.getSiteId());
					map.put("name", site.getName());
					return map;
				}).toList();
		return this.bindDataTable(data);
	}

	@GetMapping("/catalog")
	public R<?> getCatalogPermissions(@RequestParam String ownerType, @RequestParam String owner,
			@RequestParam Long siteId) {
		List<Map<String, String>> privItems = Stream.of(CatalogPrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		if (!IdUtils.validate(siteId)) {
			return R.ok(Map.of("catalogPrivs", List.of(), "catalogPrivItems", privItems));
		}
		CmsSite site = this.siteService.getSite(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

		List<CmsCatalog> catalogs = this.catalogService.lambdaQuery()
				.eq(CmsCatalog::getSiteId, site.getSiteId()).list();
		SysPermission permission = this.permissionService.getPermission(ownerType, owner);
		Set<String> permissionKeys = new HashSet<>();
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(CatalogPermissionType.ID);
			permissionKeys.addAll(this.catalogPermissionType.deserialize(json));
		}
		Set<String> inheritedPerms = this.permissionService.getInheritedPermissionKeys(ownerType, owner, CatalogPermissionType.ID);
		List<CatalogPrivVO> catalogPrivs = catalogs.stream().map(catalog -> {
			CatalogPrivVO vo = new CatalogPrivVO();
			vo.setCatalogId(catalog.getCatalogId());
			vo.setParentId(catalog.getParentId());
			vo.setName(catalog.getName());
			CatalogPrivItem[] values = CatalogPrivItem.values();
			for (CatalogPrivItem privItem : values) {
				String permissionKey = privItem.getPermissionKey(catalog.getCatalogId());
				boolean granted = permissionKeys.contains(permissionKey);
				boolean inherited = inheritedPerms.contains(permissionKey);
				if (granted || inherited) {
					vo.getPerms().put(privItem.name(), new PrivItem(true, inherited));
				}
			}
			return vo;
		}).toList();
		List<CatalogPrivVO> treeTable = buildTreeTable(catalogPrivs);
		return R.ok(Map.of("catalogPrivs", treeTable, "catalogPrivItems", privItems));
	}

	private List<CatalogPrivVO> buildTreeTable(List<CatalogPrivVO> list) {
		Map<Long, List<CatalogPrivVO>> mapChildren = list.stream().filter(n -> n.getParentId() > 0)
				.collect(Collectors.groupingBy(CatalogPrivVO::getParentId));
		List<CatalogPrivVO> result = new ArrayList<>();
		list.forEach(n -> {
			n.setChildren(mapChildren.get(n.getCatalogId()));
			if (n.getParentId() == 0) {
				result.add(n);
			}
		});
		return result;
	}

	@PutMapping("/catalog")
	public R<?> saveCatalogPermissions(@RequestBody @Validated SaveCatalogPermissionDTO dto) {
		SysPermission permission = this.permissionService.getPermission(dto.getOwnerType(), dto.getOwner());
		Set<String> permissionKeys = new HashSet<>();
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(CatalogPermissionType.ID);
			permissionKeys.addAll(this.catalogPermissionType.deserialize(json));
		}
		this.invokeCatalogPerms(permissionKeys, dto.getPerms());
		this.permissionService.savePermissions(dto.getOwnerType(), dto.getOwner(), permissionKeys,
				CatalogPermissionType.ID, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	private void invokeCatalogPerms(Set<String> permissionKeys, List<CatalogPrivVO> list) {
		list.forEach(vo -> {
			vo.getPerms().forEach((k, v) -> {
				String permissionKey = CatalogPrivItem.valueOf(k).getPermissionKey(vo.getCatalogId());
				if (v.isGranted() && !v.isInherited()) {
					permissionKeys.add(permissionKey);
				} else {
					permissionKeys.remove(permissionKey);
				}
			});
			if (StringUtils.isNotEmpty(vo.getChildren())) {
				this.invokeCatalogPerms(permissionKeys, vo.getChildren());
			}
		});
	}

	@GetMapping("/pageWidget")
	public R<?> getPageWidgetPermissions(@RequestParam String ownerType, @RequestParam String owner,
										 @RequestParam Long siteId) {
		List<Map<String, String>> privItems = Stream.of(CatalogPrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		if (!IdUtils.validate(siteId)) {
			return R.ok(Map.of("pageWidgetPrivs", List.of(), "privItems", privItems));
		}
		CmsSite site = this.siteService.getSite(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

		List<CmsPageWidget> pageWidgets = this.pageWidgetService.lambdaQuery().eq(CmsPageWidget::getSiteId, site.getSiteId()).list();
		SysPermission permission = this.permissionService.getPermission(ownerType, owner);
		Set<String> permissionKeys = new HashSet<>();
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(PageWidgetPermissionType.ID);
			permissionKeys.addAll(this.pageWidgetPermissionType.deserialize(json));
		}
		Set<String> inheritedPerms = this.permissionService.getInheritedPermissionKeys(ownerType, owner, PageWidgetPermissionType.ID);
		List<PageWidgetPrivVO> pageWidgetPrivs = pageWidgets.stream().map(pageWidget -> {
			PageWidgetPrivVO vo = new PageWidgetPrivVO();
			vo.setPageWidgetId(pageWidget.getPageWidgetId());
			vo.setName(pageWidget.getName());
			PageWidgetPermissionType.PageWidgetPrivItem[] values = PageWidgetPermissionType.PageWidgetPrivItem.values();
			for (PageWidgetPermissionType.PageWidgetPrivItem privItem : values) {
				String permissionKey = privItem.getPermissionKey(pageWidget.getPageWidgetId());
				boolean has = permissionKeys.contains(permissionKey);
				boolean inherited = inheritedPerms.contains(permissionKey);
				if (has || inherited) {
					vo.getPerms().put(privItem.name(), new PrivItem(true, inherited));
				}
			}
			return vo;
		}).toList();

		List<Map<String, String>> pageWidgetPrivItems = Stream.of(PageWidgetPermissionType.PageWidgetPrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		return R.ok(Map.of("pageWidgetPrivs", pageWidgetPrivs, "privItems", pageWidgetPrivItems));
	}

	@PutMapping("/pageWidget")
	public R<?> savePageWidgetPermissions(@RequestBody @Validated SavePageWidgetPermissionDTO dto) {
		SysPermission permission = this.permissionService.getPermission(dto.getOwnerType(), dto.getOwner());
		Set<String> permissionKeys = new HashSet<>();
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(PageWidgetPermissionType.ID);
			permissionKeys.addAll(this.pageWidgetPermissionType.deserialize(json));
		}

		dto.getPerms().forEach(vo -> {
			vo.getPerms().forEach((k, v) -> {
				String permissionKey = PageWidgetPermissionType.PageWidgetPrivItem.valueOf(k).getPermissionKey(vo.getPageWidgetId());
				if (v.isGranted() && !v.isInherited()) {
					permissionKeys.add(permissionKey);
				} else {
					permissionKeys.remove(permissionKey);
				}
			});
		});
		this.permissionService.savePermissions(dto.getOwnerType(), dto.getOwner(), permissionKeys,
				PageWidgetPermissionType.ID, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}
}
