package com.chestnut.word.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.ITagWordGroupService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * TAG词分组前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/tagword/group")
public class TagWordGroupController extends BaseRestController {

	private final ITagWordGroupService tagWordGroupService;

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<TagWordGroup> page = this.tagWordGroupService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), TagWordGroup::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/treedata")
	public R<?> getTreeData() {
		List<TagWordGroup> groups = this.tagWordGroupService.list();
		List<TreeNode<String>> treeData = this.tagWordGroupService.buildTreeData(groups);
		return R.ok(treeData);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping
	public R<?> add(@RequestBody @Validated TagWordGroup group) {
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		return R.ok(this.tagWordGroupService.addTagWordGroup(group));
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PutMapping
	public R<?> edit(@RequestBody @Validated TagWordGroup group) {
		group.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.tagWordGroupService.editTagWordGroup(group);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> groupIds) {
		this.tagWordGroupService.deleteTagWordGroups(groupIds);
		return R.ok();
	}
}
