package com.chestnut.stat.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.stat.service.IStatService;
import com.chestnut.stat.user.preference.StatIndexPreference;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/stat")
public class StatController extends BaseRestController {
	
	private final IStatService statService;
	
	@GetMapping("/menu/tree")
	public R<?> bindStatTreeData() {
		List<TreeNode<String>> treeMenus = this.statService.getStatMenuTree();
		SysUser user = (SysUser) StpAdminUtil.getLoginUser().getUser();
		return R.ok(Map.of("treeData", treeMenus, "defaultMenu", StatIndexPreference.getValue(user.getPreferences())));
	}
	
	@GetMapping("/menu/options")
	public R<?> bindStatTreeSelector() {
		List<TreeNode<String>> treeMenus = this.statService.getStatMenuTree();
		return R.ok(treeMenus);
	}
}
