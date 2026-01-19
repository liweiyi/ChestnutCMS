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
package com.chestnut.word.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.domain.dto.CreateHotWordGroupRequest;
import com.chestnut.word.domain.dto.UpdateHotWordGroupRequest;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.IHotWordGroupService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 热词分组前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/hotword/group")
public class HotWordGroupController extends BaseRestController {

	private final IHotWordGroupService hotWordGroupService;

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/list")
	public R<?> getPageList(@RequestParam(required = false) @Length(max = 255) String query) {
		PageRequest pr = this.getPageRequest();
		Page<HotWordGroup> page = this.hotWordGroupService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), HotWordGroup::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/options")
	public R<?> getOptions() {
		List<HotWordGroup> list = this.hotWordGroupService.list();
		return bindSelectOptions(list, HotWordGroup::getCode, HotWordGroup::getName);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/treedata")
	public R<?> getTreeData() {
		List<TreeNode<String>> treeData = this.hotWordGroupService.getGroupTreeData(q -> {});
		return R.ok(treeData);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/add")
	public R<?> add(@RequestBody @Validated CreateHotWordGroupRequest req) {
		return R.ok(this.hotWordGroupService.addHotWordGroup(req));
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/update")
	public R<String> edit(@RequestBody @Validated UpdateHotWordGroupRequest req) {
		this.hotWordGroupService.updateHotWordGroup(req);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/delete")
	public R<?> remove(@RequestBody @NotEmpty List<Long> groupIds) {
		this.hotWordGroupService.deleteHotWordGroups(groupIds);
		return R.ok();
	}
}
