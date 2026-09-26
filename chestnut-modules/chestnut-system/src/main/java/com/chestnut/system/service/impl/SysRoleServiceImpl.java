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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysDept;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUserRole;
import com.chestnut.system.domain.dto.CreateRoleRequest;
import com.chestnut.system.domain.dto.UpdateRoleRequest;
import com.chestnut.system.domain.dto.UpdateRoleStatusRequest;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.mapper.SysRoleMapper;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.service.ISysDeptService;
import com.chestnut.system.service.ISysRoleService;
import com.chestnut.system.service.ISysUserService;
import com.chestnut.system.utils.SysDeptUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色 业务层处理
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

	private final SysRoleMapper roleMapper;

	private final SysUserRoleMapper userRoleMapper;

	private final ISysUserService userService;

	private final RedisCache redisCache;

    private final ISysDeptService deptService;

	@Override
	public SysRole getRole(Long roleId) {
		String cacheKey = SysConstants.CACHE_SYS_ROLE_KEY + roleId;
		SysRole role = redisCache.getCacheObject(cacheKey, SysRole.class);
		if (Objects.nonNull(role)) {
			return role;
		}
		role = this.getById(roleId);
		if (Objects.nonNull(role)) {
			redisCache.setCacheObject(cacheKey, role);
		}
		return role;
	}

	@Override
	public List<SysRole> selectRolesByUserId(Long userId, String status) {
		List<Long> roleIds = this.userRoleMapper.selectList(
				new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
		).stream().map(SysUserRole::getRoleId).toList();
		if (roleIds.isEmpty()) {
			return List.of();
		}
		return roleMapper.selectList(
				new LambdaQueryWrapper<SysRole>()
						.eq(StringUtils.isNotEmpty(status), SysRole::getStatus, EnableOrDisable.ENABLE)
						.in(SysRole::getRoleId, roleIds)
		);
	}

	@Override
	public List<String> selectRoleKeysByUserId(Long userId) {
		List<SysRole> roles = this.selectRolesByUserId(userId);
		return roles.stream().map(SysRole::getRoleKey).toList();
	}

	private boolean checkRoleUnique(String roleName, String roleKey, Long roleId) {
		LambdaQueryWrapper<SysRole> q = new LambdaQueryWrapper<SysRole>()
				.and(wrapper -> wrapper
						.eq(StringUtils.isNotEmpty(roleName), SysRole::getRoleName, roleName).or()
						.eq(StringUtils.isNotEmpty(roleKey), SysRole::getRoleKey, roleKey))
				.ne(IdUtils.validate(roleId), SysRole::getRoleId, roleId);
		return this.count(q) == 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertRole(CreateRoleRequest req) {
		SysDept dept = this.deptService.getDept(req.getDeptId());
		Assert.notNull(dept, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("deptId", req.getDeptId()));
		SysDeptUtils.checkDeptScope(req.getOperator(), dept::getDeptId, dept::getAncestors);

		boolean checkRoleUnique = this.checkRoleUnique(req.getRoleName(), req.getRoleKey(), null);
		Assert.isTrue(checkRoleUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("RoleName,RoleKey"));

		SysRole role = new SysRole();
		BeanUtils.copyProperties(req, role);
		role.setRoleId(IdUtils.getSnowflakeId());
		role.setDeptId(dept.getDeptId());
		role.setDeptAncestors(dept.getAncestors());
		role.createBy(req.getOperator().getUsername());
		this.save(role);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_ROLE_KEY + role.getRoleId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateRole(UpdateRoleRequest req) {
		SysRole db = this.getById(req.getRoleId());

		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("roleId", req.getRoleId()));
		boolean checkRoleUnique = this.checkRoleUnique(req.getRoleName(), req.getRoleKey(), req.getRoleId());
		Assert.isTrue(checkRoleUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("RoleName,RoleKey"));
		// 用户是否有角色所属机构权限
		SysDeptUtils.checkDeptScope(req.getOperator(), db::getDeptId, db::getDeptAncestors);

		db.setRoleName(req.getRoleName());
		db.setRoleKey(req.getRoleKey());
		db.setRoleSort(req.getRoleSort());
		db.setRemark(req.getRemark());
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_ROLE_KEY + req.getRoleId());
	}

	@Override
    @Transactional(rollbackFor = Exception.class)
	public List<Long> updateRoleStatus(UpdateRoleStatusRequest req) {
        SysRole db = this.getById(req.getRoleId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(req.getRoleId()));

		SysDeptUtils.checkDeptScope(req.getOperator(), db::getDeptId, db::getDeptAncestors);

		// 影响用户列表
		List<Long> userIds = this.userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
						.select(SysUserRole::getUserId).eq(SysUserRole::getRoleId, req.getRoleId()))
						.stream().map(SysUserRole::getUserId).toList();

		db.setStatus(req.getStatus());
		db.updateBy(req.getOperator().getUsername());
		this.updateById(db);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_ROLE_KEY + db.getRoleId());
		return userIds;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Set<Long> deleteRoleByIds(List<Long> roleIds, LoginUser operator) {
		List<SysRole> roles = this.listByIds(roleIds);
		Assert.isTrue(!roles.isEmpty(), SysErrorCode.ROLES_EMPTY::exception);

		for (SysRole role : roles) {
			// 必须是当前登录用户所在部门或其下级部门的角色
			SysDeptUtils.checkDeptScope(operator, role::getDeptId, role::getDeptAncestors);
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_ROLE_KEY + role.getRoleId());
		}
		// 影响用户列表
		Set<Long> userIds = this.userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds))
				.stream().map(SysUserRole::getUserId).collect(Collectors.toSet());
		// 删除角色数据
		this.removeByIds(roles);
		// 删除角色用户关联关系
		this.userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getRoleId, roleIds));
		// 清理缓存
		List<String> roleCacheKeys = roles.stream().map(role ->
				SysConstants.CACHE_SYS_ROLE_KEY + role.getRoleId()).toList();
		this.redisCache.deleteObjects(roleCacheKeys);
        return userIds;
	}

	@Override
	public List<Long> getUserIdsByRole(Long roleId) {
		return this.userRoleMapper.selectList(
				new LambdaQueryWrapper<SysUserRole>().select(SysUserRole::getUserId)
						.eq(SysUserRole::getRoleId, roleId)
		).stream().map(SysUserRole::getUserId).toList();
	}
}
