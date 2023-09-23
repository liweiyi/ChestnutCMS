package com.chestnut.word.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.chestnut.word.domain.HotWordGroup;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.IHotWordGroupService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

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
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
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
		List<Map<String, Object>> options = new ArrayList<>();
		list.forEach(g -> {
			options.add(Map.of("code", g.getCode(), "name", g.getName()));
		});
		return this.bindDataTable(options);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/treedata")
	public R<?> getTreeData() {
		List<HotWordGroup> groups = this.hotWordGroupService.list();
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
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		return R.ok(this.hotWordGroupService.addHotWordGroup(group));
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PutMapping
	public R<String> edit(@RequestBody @Validated HotWordGroup group) {
		group.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.hotWordGroupService.updateHotWordGroup(group);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> groupIds) {
		this.hotWordGroupService.deleteHotWordGroups(groupIds);
		return R.ok();
	}
}
