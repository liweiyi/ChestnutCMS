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
package com.chestnut.system.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.domain.TreeNode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.dto.CreateDeptRequest;
import com.chestnut.system.domain.dto.QueryDeptRequest;
import com.chestnut.system.domain.dto.UpdateDeptRequest;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.utils.SysDeptUtils;
import com.chestnut.system.validator.LongId;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 部门信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.DEPT.MODULE}")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class SysDeptController extends BaseRestController {

	private final ISysDeptService deptService;

    @XComment("{API.DOC.SYS.DEPT.GET_TREE}")
    @Priv(type = AdminUserType.TYPE, value = {SysMenuPriv.SysDeptList, SysMenuPriv.SysUserList,
            SysMenuPriv.SysRoleList}, mode = SaMode.OR)
    @GetMapping("/tree")
    public R<List<TreeNode<Long>>> tree(@Validated QueryDeptRequest req) {
        LoginUser operator = StpAdminUtil.getLoginUser();
		List<SysDept> departments = deptService.getDepartmentsByUserId(operator.getUserId(), q -> {
			if (StringUtils.isNotEmpty(req.getDeptName())) {
				q.like(SysDept::getDeptName, req.getDeptName());
			}
			if (Objects.nonNull(req.getStatus())) {
				q.eq(SysDept::getStatus, req.getStatus());
			}
        });

		List<TreeNode<Long>> list = departments.stream().map(dept -> new TreeNode<>(
				dept.getDeptId(),
				dept.getParentId(),
				dept.getDeptName(),
				operator.isSuperAdministrator() ? dept.getParentId().equals(0L) : dept.getDeptId().equals(operator.getDeptId())
		)).toList();
		List<TreeNode<Long>> tree = TreeNode.build(list);
		return R.ok(tree);
    }

	@XComment("{API.DOC.SYS.DEPT.GET_LIST}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptList)
	@GetMapping("/list")
	public R<TableData<SysDept>> list(@Validated QueryDeptRequest req) {
        LoginUser operator = StpAdminUtil.getLoginUser();
		List<SysDept> departments = deptService.getDepartmentsByUserId(operator.getUserId(), q -> {
			if (StringUtils.isNotEmpty(req.getDeptName())) {
				q.like(SysDept::getDeptName, req.getDeptName());
			}
			if (Objects.nonNull(req.getStatus())) {
				q.eq(SysDept::getStatus, req.getStatus());
			}
		});
		List<SysDept> depts = this.deptService.buildDeptTree(departments);
		return bindDataTable(depts);
	}

	@XComment("{API.DOC.SYS.DEPT.GET_INFO}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptList)
	@GetMapping(value = "/detail/{deptId}")
	public R<SysDept> getInfo(@PathVariable @LongId @XComment("{API.DOC.SYS.DEPT.ID}") Long deptId) {
        LoginUser operator = StpAdminUtil.getLoginUser();
		SysDept dept = deptService.getDept(deptId);
		SysDeptUtils.checkDeptScope(operator, dept::getDeptId, dept::getAncestors);
		if (IdUtils.validate(dept.getParentId())) {
			SysDept parent = deptService.getDept(dept.getParentId());
			dept.setParentName(parent.getDeptName());
		}
		return R.ok(dept);
	}

	@XComment("{API.DOC.SYS.DEPT.CREATE_DEPT}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptAdd)
	@Log(title = "部门管理", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> add(@Validated @RequestBody CreateDeptRequest req) {
		deptService.insertDept(req);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.DEPT.UPDATE_DEPT}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptEdit)
	@Log(title = "部门管理", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> edit(@Validated @RequestBody UpdateDeptRequest req) {
		deptService.updateDept(req);
		return R.ok();
	}

	/**
	 * 删除部门
	 */
	@XComment("{API.DOC.SYS.DEPT.DELETE_DEPT}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDeptRemove)
	@Log(title = "部门管理", businessType = BusinessType.DELETE)
	@PostMapping("/delete/{deptId}")
	public R<Void> remove(@PathVariable @LongId @XComment("{API.DOC.SYS.DEPT.ID}") Long deptId) {
        LoginUser operator = StpAdminUtil.getLoginUser();

		deptService.deleteDeptById(deptId, operator);
		return R.ok();
	}
}
