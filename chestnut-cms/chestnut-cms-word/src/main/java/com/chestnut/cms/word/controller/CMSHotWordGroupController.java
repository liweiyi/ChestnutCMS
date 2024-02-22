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
package com.chestnut.cms.word.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.IHotWordGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 热词分组前端控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/hotword/group")
public class CMSHotWordGroupController extends BaseRestController {

	private final IHotWordGroupService hotWordGroupService;

	private final ISiteService siteService;

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		CmsSite currentSite = siteService.getCurrentSite(ServletUtils.getRequest());
		Page<HotWordGroup> page = this.hotWordGroupService.lambdaQuery()
				.eq(HotWordGroup::getOwner, currentSite.getSiteId())
				.like(StringUtils.isNotEmpty(query), HotWordGroup::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/options")
	public R<?> getOptions() {
		CmsSite currentSite = siteService.getCurrentSite(ServletUtils.getRequest());
		List<HotWordGroup> list = this.hotWordGroupService.lambdaQuery()
				.eq(HotWordGroup::getOwner, currentSite.getSiteId())
				.list();
		List<Map<String, Object>> options = new ArrayList<>();
		list.forEach(g -> {
			options.add(Map.of("code", g.getCode(), "name", g.getName()));
		});
		return this.bindDataTable(options);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/treedata")
	public R<?> getTreeData() {
		CmsSite currentSite = siteService.getCurrentSite(ServletUtils.getRequest());
		List<HotWordGroup> groups = this.hotWordGroupService.lambdaQuery()
				.eq(HotWordGroup::getOwner, currentSite.getSiteId()).list();
		List<TreeNode<String>> list = new ArrayList<>();
		if (groups != null && groups.size() > 0) {
			groups.forEach(c -> {
				TreeNode<String> treeNode = new TreeNode<>(String.valueOf(c.getGroupId()), "", c.getName(), true);
				treeNode.setProps(Map.of("code", c.getCode()));
				list.add(treeNode);
			});
		}
		List<TreeNode<String>> treeData = TreeNode.build(list);
		return R.ok(treeData);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping
	public R<?> add(@RequestBody @Validated HotWordGroup group) {
		CmsSite site = siteService.getCurrentSite(ServletUtils.getRequest());
		group.setOwner(site.getSiteId().toString());
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		return R.ok(this.hotWordGroupService.addHotWordGroup(group));
	}
}
