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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserRole;
import com.chestnut.system.domain.dto.*;
import com.chestnut.system.domain.vo.RoleUserVO;
import com.chestnut.system.mapper.SysUserMapper;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysRoleService;
import com.chestnut.system.service.impl.UserPermissionService;
import com.chestnut.system.utils.SysDeptUtils;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 角色信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.SYS.ROLE.MODULE}")
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class SysRoleController extends BaseRestController {

	private final SysUserRoleMapper userRoleMapper;

    private final ISysDeptService deptService;

	private final ISysRoleService roleService;

	private final SysUserMapper userMapper;

	private final UserPermissionService userPermissionService;

	@XComment("{API.DOC.SYS.ROLE.GET_LIST}")
	@ExcelExportable(SysRole.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping("/list")
	public R<TableData<SysRole>> list(@Validated QueryRoleRequest req) {
		SysUser loginUser = (SysUser) StpAdminUtil.getLoginUser().getUser();
		PageRequest pr = this.getPageRequest();
		SysDept dept = this.deptService.getDept(req.getDeptId());
		Assert.notNull(dept, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("deptId", req.getDeptId()));
		SysDeptUtils.checkDeptScope(loginUser, dept::getDeptId, dept::getAncestors);

		LambdaQueryWrapper<SysRole> q = new LambdaQueryWrapper<>();
		q.and(and -> and.eq(SysRole::getDeptId, dept.getDeptId())
				.or(!req.isOnlyCurrentDept())
				.likeRight(!req.isOnlyCurrentDept(), SysRole::getDeptAncestors, dept.getAncestors() + SysDeptUtils.ANCESTORS_SPLITTER));
		q.like(StringUtils.isNotEmpty(req.getRoleName()), SysRole::getRoleName, req.getRoleName())
			.like(StringUtils.isNotEmpty(req.getRoleKey()), SysRole::getRoleKey, req.getRoleKey())
			.eq(StringUtils.isNotEmpty(req.getStatus()), SysRole::getStatus, req.getStatus())
			.orderByAsc(SysRole::getRoleSort);
		Page<SysRole> page = roleService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
        page.getRecords().forEach(role -> {
			SysDept d = deptService.getDept(role.getDeptId());
			if (Objects.nonNull(d)) {
				role.setDeptName(d.getDeptName());
			}
        });
		return bindDataTable(page);
	}

	@XComment("{API.DOC.SYS.ROLE.GET_INFO}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping(value = "/detail/{roleId}")
	public R<SysRole> getInfo(@PathVariable @LongId @XComment("{API.DOC.SYS.ROLE.ID}") Long roleId) {
        LoginUser operator = StpAdminUtil.getLoginUser();
		SysRole role = this.roleService.getById(roleId);
		// 校验机构范围
		SysDeptUtils.checkDeptScope(operator, role::getDeptId, role::getDeptAncestors);
        return R.ok(role);
	}

	@XComment("{API.DOC.SYS.ROLE.CREATE_ROLE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleAdd)
	@Log(title = "角色管理", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> add(@Validated @RequestBody CreateRoleRequest req) {
		roleService.insertRole(req);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.ROLE.UPDATE_ROLE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> edit(@Validated @RequestBody UpdateRoleRequest req) {
		roleService.updateRole(req);
		return R.ok();
	}

	/**
	 * 状态修改
	 */
	@XComment("{API.DOC.SYS.ROLE.CHANGE_STATUS}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.UPDATE)
	@PostMapping("/changeStatus")
	public R<Void> changeStatus(@RequestBody @Validated UpdateRoleStatusRequest req) {
		List<Long> userIds = roleService.updateRoleStatus(req);
		// 重置关联用户登录TOKEN
		userIds.forEach(this.userPermissionService::resetLoginUser);
		return R.ok();
	}

	/**
	 * 删除角色
	 */
	@XComment("{API.DOC.SYS.ROLE.DELETE_ROLE}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleRemove)
	@Log(title = "角色管理", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.SYS.ROLE.IDS}") List<Long> roleIds) {
        LoginUser operator = StpAdminUtil.getLoginUser();
		Set<Long> userIds = roleService.deleteRoleByIds(roleIds, operator);
		// 重置关联用户登录TOKEN
		userIds.forEach(this.userPermissionService::resetLoginUser);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.ROLE.ALLOCATED_USER_LIST}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping("/authUser/allocatedList")
	public R<TableData<RoleUserVO>> getUserListByRole(@Validated QueryRoleUserRequest req) {
		LoginUser operator = StpAdminUtil.getLoginUser();
		SysRole role = this.roleService.getById(req.getRoleId());
		SysDeptUtils.checkDeptScope(operator, role::getDeptId, role::getDeptAncestors);

		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysUserRole> q = new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, req.getRoleId())
				.like(StringUtils.isNotEmpty(req.getUserName()), SysUserRole::getUserName, req.getUserName())
				.like(StringUtils.isNotEmpty(req.getRealName()), SysUserRole::getRealName, req.getRealName())
				.like(StringUtils.isNotEmpty(req.getNickName()), SysUserRole::getNickName, req.getNickName())
				.like(StringUtils.isNotEmpty(req.getPhoneNumber()), SysUserRole::getPhoneNumber, req.getPhoneNumber())
				.like(StringUtils.isNotEmpty(req.getEmail()), SysUserRole::getEmail, req.getEmail());
		Page<SysUserRole> page = this.userRoleMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		List<Long> userIds = page.getRecords().stream().map(SysUserRole::getUserId).toList();
		if (userIds.isEmpty()) {
			return bindDataTable(List.of());
		}
		List<RoleUserVO> users = this.userMapper.selectByIds(userIds).stream().map(RoleUserVO::create).toList();
		return bindDataTable(users, page.getTotal());
	}

	@XComment("{API.DOC.SYS.ROLE.UNALLOCATED_USER_LIST}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping("/authUser/unallocatedList")
	public R<TableData<RoleUserVO>> unallocatedList(@Validated QueryRoleUserRequest req) {
        LoginUser operator = StpAdminUtil.getLoginUser();
        SysRole role = this.roleService.getById(req.getRoleId());
		SysDeptUtils.checkDeptScope(operator, role::getDeptId, role::getDeptAncestors);

        PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<SysUser>().eq(SysUser::getDeptId, role.getDeptId())
				.like(StringUtils.isNotEmpty(req.getUserName()), SysUser::getUserName, req.getUserName())
				.like(StringUtils.isNotEmpty(req.getRealName()), SysUser::getRealName, req.getRealName())
				.like(StringUtils.isNotEmpty(req.getNickName()), SysUser::getNickName, req.getNickName())
				.like(StringUtils.isNotEmpty(req.getPhoneNumber()), SysUser::getPhoneNumber, req.getPhoneNumber())
				.like(StringUtils.isNotEmpty(req.getEmail()), SysUser::getEmail, req.getEmail());
		Page<SysUser> page = this.userMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		List<Long> userIds = page.getRecords().stream().map(SysUser::getUserId).toList();

		List<Long> allocatedUserIds;
		if (!userIds.isEmpty()) {
			allocatedUserIds = this.userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
					.eq(SysUserRole::getRoleId, req.getRoleId())
					.in(SysUserRole::getUserId, userIds)).stream().map(SysUserRole::getUserId).toList();
		} else {
		 	allocatedUserIds = List.of();
		}
		List<RoleUserVO> list = page.getRecords().stream().map(user -> {
			return RoleUserVO.create(user, allocatedUserIds.contains(user.getUserId()));
		}).toList();
        return bindDataTable(list, page.getTotal());
	}

	@XComment("{API.DOC.SYS.ROLE.CANCEL_AUTH_USERS}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.GRANT)
	@PostMapping("/authUser/cancel")
	public R<Void> cancelAuthUserAll(@LongId @XComment("{API.DOC.SYS.ROLE.ID}") Long roleId, @RequestBody @NotEmpty @XComment("{API.DOC.SYS.USER.IDS}") List<Long> userIds) {
        LoginUser operator = StpAdminUtil.getLoginUser();
		userPermissionService.removeUsersFromRole(roleId, userIds, operator);
		return R.ok();
	}

	@XComment("{API.DOC.SYS.ROLE.GRANT_AUTH_USERS}")
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.GRANT)
	@PostMapping("/authUser/grant")
	public R<Void> grantAuthUserAll(@LongId @XComment("{API.DOC.SYS.ROLE.ID}") Long roleId, @RequestBody @NotEmpty @XComment("{API.DOC.SYS.USER.IDS}") List<Long> userIds) {
        LoginUser operator = StpAdminUtil.getLoginUser();
		userPermissionService.addUsersToRole(roleId, userIds, operator);
		return R.ok();
	}

}
