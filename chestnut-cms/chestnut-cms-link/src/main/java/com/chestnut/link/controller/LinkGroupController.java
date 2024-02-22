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
package com.chestnut.link.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.SortUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.link.domain.CmsLinkGroup;
import com.chestnut.link.domain.dto.LinkGroupDTO;
import com.chestnut.link.permission.FriendLinkPriv;
import com.chestnut.link.service.ILinkGroupService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 友情链接分组前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/link_group")
public class LinkGroupController extends BaseRestController {

	private final ISiteService siteService;

	private final ILinkGroupService linkGroupService;

	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<CmsLinkGroup> q = new LambdaQueryWrapper<CmsLinkGroup>()
				.eq(CmsLinkGroup::getSiteId, site.getSiteId())
				.like(StringUtils.isNotEmpty(query), CmsLinkGroup::getName, query)
				.orderByAsc(CmsLinkGroup::getSortFlag);
		Page<CmsLinkGroup> page = this.linkGroupService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增友链分组", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.Add)
	@PostMapping
	public R<?> add(@RequestBody @Validated LinkGroupDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		if (this.linkGroupService.lambdaQuery().eq(CmsLinkGroup::getSiteId, site.getSiteId())
				.eq(CmsLinkGroup::getCode, dto.getCode()).count() > 0) {
			return R.fail("友链分组编码重复");
		}
		CmsLinkGroup linkGroup = new CmsLinkGroup();
		BeanUtils.copyProperties(dto, linkGroup, "linkGroupId", "siteId", "sortFlag");
		linkGroup.setSiteId(site.getSiteId());
		linkGroup.setLinkGroupId(IdUtils.getSnowflakeId());
		linkGroup.setSortFlag(SortUtils.getDefaultSortValue());
		linkGroup.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkGroupService.save(linkGroup);
		return R.ok();
	}

	@Log(title = "编辑友链分组", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { FriendLinkPriv.Add, FriendLinkPriv.Edit })
	@PutMapping
	public R<String> edit(@RequestBody @Validated LinkGroupDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		if (this.linkGroupService.lambdaQuery().eq(CmsLinkGroup::getSiteId, site.getSiteId())
				.eq(CmsLinkGroup::getCode, dto.getCode()).ne(CmsLinkGroup::getLinkGroupId, dto.getLinkGroupId())
				.count() > 0) {
			return R.fail("友链分组编码重复");
		}
		CmsLinkGroup linkGroup = new CmsLinkGroup();
		BeanUtils.copyProperties(dto, linkGroup, "siteId");
		linkGroup.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.linkGroupService.updateById(linkGroup);
		return R.ok();
	}

	@Log(title = "删除友链分组", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = FriendLinkPriv.Delete)
	@DeleteMapping
	public R<String> remove(@RequestBody @Validated @NotEmpty List<LinkGroupDTO> dtoList) {
		List<Long> linkGroupIds = dtoList.stream().map(LinkGroupDTO::getLinkGroupId).toList();
		this.linkGroupService.deleteLinkGroup(linkGroupIds);
		return R.ok();
	}
}
