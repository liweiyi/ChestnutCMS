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
package com.chestnut.cms.word.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.domain.dto.CreateHotWordGroupRequest;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.IHotWordGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 热词分组前端控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/hotword/group")
public class CMSHotWordGroupController extends CmsRestController {

	private final IHotWordGroupService hotWordGroupService;

    @Priv(
            type = AdminUserType.TYPE,
            value = { WordPriv.View, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		CmsSite currentSite = getCurrentSite();
		Page<HotWordGroup> page = this.hotWordGroupService.lambdaQuery()
				.eq(HotWordGroup::getOwner, currentSite.getSiteId().toString())
				.like(StringUtils.isNotEmpty(query), HotWordGroup::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

    @Priv(
            type = AdminUserType.TYPE,
            value = { CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@GetMapping("/options")
	public R<?> getOptions() {
		CmsSite currentSite = getCurrentSite();
		List<HotWordGroup> list = this.hotWordGroupService.lambdaQuery()
				.eq(HotWordGroup::getOwner, currentSite.getSiteId().toString())
				.list();
        return this.bindSelectOptions(list, HotWordGroup::getCode, HotWordGroup::getName);
	}

    @Priv(
            type = AdminUserType.TYPE,
            value = { WordPriv.View, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@GetMapping("/treedata")
	public R<?> getTreeData() {
		CmsSite currentSite = getCurrentSite();
        List<TreeNode<String>> treeData = this.hotWordGroupService.getGroupTreeData(q -> {
            q.eq(HotWordGroup::getOwner, currentSite.getSiteId().toString());
        });
		return R.ok(treeData);
	}

    @Priv(
            type = AdminUserType.TYPE,
            value = { WordPriv.View, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
            mode = SaMode.AND
    )
	@PostMapping
	public R<?> add(@RequestBody @Validated CreateHotWordGroupRequest req) {
		CmsSite site = getCurrentSite();
		req.setOwner(site.getSiteId().toString());
		return R.ok(this.hotWordGroupService.addHotWordGroup(req));
	}
}
