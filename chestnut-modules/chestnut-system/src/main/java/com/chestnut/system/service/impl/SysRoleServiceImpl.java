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
package com.chestnut.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.redis.RedisCache;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.SysConstants;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUserRole;
import com.chestnut.system.exception.SysErrorCode;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.mapper.SysRoleMapper;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

	private final RedisCache redisCache;

	@Override
	public SysRole getRole(String roleCode) {
		SysRole role = redisCache.getCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + roleCode, SysRole.class);
		if (Objects.nonNull(role)) {
			return role;
		}
		role = this.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, roleCode));
		if (Objects.nonNull(role)) {
			redisCache.setCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + roleCode, role);
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
	public void insertRole(SysRole role) {
		boolean checkRoleUnique = this.checkRoleUnique(role.getRoleName(), role.getRoleKey(), null);
		Assert.isTrue(checkRoleUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("RoleName,RoleKey"));
		
		// 新增角色信息
		role.setRoleId(IdUtils.getSnowflakeId());
		role.setCreateTime(LocalDateTime.now());
		this.save(role);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_POST_KEY + role.getRoleKey());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateRole(SysRole role) {
		SysRole db = this.getById(role.getRoleId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(role.getRoleId()));
		boolean checkRoleUnique = this.checkRoleUnique(role.getRoleName(), role.getRoleKey(), role.getRoleId());
		Assert.isTrue(checkRoleUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("RoleName,RoleKey"));
		
		// 修改角色信息
		db.setRoleName(role.getRoleName());
		db.setRoleKey(role.getRoleKey());
		db.setStatus(role.getStatus());
		db.setRoleSort(role.getRoleSort());
		db.setRemark(role.getRemark());
		db.setUpdateTime(LocalDateTime.now());
		this.updateById(db);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_ROLE_KEY + role.getRoleKey());
	}

	@Override
	public void updateRoleStatus(SysRole role) {
		SysRole db = this.getById(role.getRoleId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(role.getRoleId()));
		
		db.setStatus(role.getStatus());
		db.setUpdateTime(LocalDateTime.now());
		this.updateById(db);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_ROLE_KEY + db.getRoleKey());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteRoleByIds(List<Long> roleIds) {
		for (Long roleId : roleIds) {
			SysRole role = this.getById(roleId);
			Long userCount = userRoleMapper
					.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
			Assert.isTrue(userCount == 0, () -> SysErrorCode.ROLE_USER_NOT_EMPTY.exception(role.getRoleKey()));
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_POST_KEY + role.getRoleKey());
		}
		this.removeByIds(roleIds);
	}

	@Override
	public void deleteAuthUsers(Long roleId, List<Long> userIds) {
		LambdaQueryWrapper<SysUserRole> q = new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
				.in(SysUserRole::getUserId, userIds);
		userRoleMapper.delete(q);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void insertAuthUsers(Long roleId, List<Long> userIds) {
		// 新增用户与角色管理
		for (Long userId : userIds) {
			SysUserRole ur = new SysUserRole();
			ur.setUserId(userId);
			ur.setRoleId(roleId);
			userRoleMapper.insert(ur);
		}
	}
}
