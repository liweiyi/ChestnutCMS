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

import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.ITagWordGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TAG词分组前端控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/tagword/group")
public class CMSTagWordGroupController extends BaseRestController {

	private final ITagWordGroupService tagWordGroupService;

	private final ISiteService siteService;

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/treedata")
	public R<?> getTreeData() {
		CmsSite currentSite = siteService.getCurrentSite(ServletUtils.getRequest());
		List<TagWordGroup> groups = this.tagWordGroupService.lambdaQuery()
				.eq(TagWordGroup::getOwner, currentSite.getSiteId())
				.list();
		List<TreeNode<String>> treeData = this.tagWordGroupService.buildTreeData(groups);
		return R.ok(treeData);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping
	public R<?> add(@RequestBody @Validated TagWordGroup group) {
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		CmsSite currentSite = siteService.getCurrentSite(ServletUtils.getRequest());
		group.setOwner(currentSite.getSiteId().toString());
		return R.ok(this.tagWordGroupService.addTagWordGroup(group));
	}
}
