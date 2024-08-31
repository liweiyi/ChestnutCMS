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
package com.chestnut.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.ExcelExportable;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserRole;
import com.chestnut.system.domain.vo.UserWithRoleFlagVO;
import com.chestnut.system.mapper.SysUserMapper;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.service.ISysRoleService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色信息
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/role")
public class SysRoleController extends BaseRestController {

	private final ISysRoleService roleService;

	private final SysUserMapper userMapper;

	private final SysUserRoleMapper userRoleMapper;

	@ExcelExportable(SysRole.class)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping("/list")
	public R<?> list(SysRole role) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysRole> q = new LambdaQueryWrapper<SysRole>()
				.like(StringUtils.isNotEmpty(role.getRoleName()), SysRole::getRoleName, role.getRoleName())
				.like(StringUtils.isNotEmpty(role.getRoleKey()), SysRole::getRoleKey, role.getRoleKey())
				.eq(StringUtils.isNotEmpty(role.getStatus()), SysRole::getStatus, role.getStatus())
				.orderByAsc(SysRole::getRoleSort);
		Page<SysRole> page = roleService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	/**
	 * 根据角色编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping(value = "/{roleId}")
	public R<?> getInfo(@PathVariable Long roleId) {
		return R.ok(roleService.getById(roleId));
	}

	/**
	 * 新增角色
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleAdd)
	@Log(title = "角色管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysRole role) {
		role.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		roleService.insertRole(role);
		return R.ok();
	}

	/**
	 * 修改保存角色
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysRole role) {
		role.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		roleService.updateRole(role);
		return R.ok();
	}

	/**
	 * 状态修改
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.UPDATE)
	@PutMapping("/changeStatus")
	public R<?> changeStatus(@RequestBody SysRole role) {
		role.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		roleService.updateRoleStatus(role);
		return R.ok();
	}

	/**
	 * 删除角色
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleRemove)
	@Log(title = "角色管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody List<Long> roleIds) {
		boolean validate = IdUtils.validate(roleIds);
		Assert.isTrue(validate, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("roleIds"));

		roleService.deleteRoleByIds(roleIds);
		return R.ok();
	}

	/**
	 * 查询已分配指定角色用户列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping("/authUser/allocatedList")
	public R<?> allocatedList(@RequestParam("roleId") @LongId Long roleId,
			@RequestParam(required = false) String userName, @RequestParam(required = false) String phoneNumber) {
		PageRequest pr = this.getPageRequest();
		Page<SysUser> page = this.userMapper.selectAllocatedList(new Page<>(pr.getPageNumber(), pr.getPageSize()), roleId, userName, phoneNumber);
		return bindDataTable(page);
	}

	/**
	 * 查询未分配角色用户列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleList)
	@GetMapping("/authUser/unallocatedList")
	public R<?> unallocatedList(@RequestParam("roleId") @LongId Long roleId,
			@RequestParam(required = false) String userName, @RequestParam(required = false) String phoneNumber) {
		PageRequest pr = this.getPageRequest();
		Page<SysUser> page = this.userMapper.selectPage(
				new Page<>(pr.getPageNumber(), pr.getPageSize()),
				new LambdaQueryWrapper<SysUser>().like(StringUtils.isNotEmpty(userName), SysUser::getUserName, userName)
						.like(StringUtils.isNotEmpty(phoneNumber), SysUser::getPhoneNumber, phoneNumber)
		);
		List<Long> userIds = page.getRecords().stream().map(SysUser::getUserId).toList();
		LambdaQueryWrapper<SysUserRole> q = new LambdaQueryWrapper<SysUserRole>()
				.eq(SysUserRole::getRoleId, roleId)
				.in(SysUserRole::getUserId, userIds);
		List<Long> allocatedUserIds = this.userRoleMapper.selectList(q).stream().map(SysUserRole::getUserId).toList();

		List<UserWithRoleFlagVO> list = page.getRecords().stream()
				.map(user -> UserWithRoleFlagVO.newInstance(user, roleId, allocatedUserIds.contains(user.getUserId())))
				.toList();
		return bindDataTable(list, page.getTotal());
	}

	/**
	 * 批量取消授权用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.GRANT)
	@PutMapping("/authUser/cancel")
	public R<?> cancelAuthUserAll(@LongId Long roleId, @RequestBody @NotEmpty List<Long> userIds) {
		roleService.deleteAuthUsers(roleId, userIds);
		return R.ok();
	}

	/**
	 * 批量选择用户授权
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysRoleEdit)
	@Log(title = "角色管理", businessType = BusinessType.GRANT)
	@PutMapping("/authUser/grant")
	public R<?> grantAuthUserAll(@LongId Long roleId, @RequestBody @NotEmpty List<Long> userIds) {
		roleService.insertAuthUsers(roleId, userIds);
		return R.ok();
	}
}
