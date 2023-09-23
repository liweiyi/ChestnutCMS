package com.chestnut.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aliyun.oss.ServiceException;
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
import com.chestnut.system.mapper.SysRoleMapper;
import com.chestnut.system.mapper.SysUserRoleMapper;
import com.chestnut.system.service.ISysRoleService;

import lombok.RequiredArgsConstructor;

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
		SysRole role = redisCache.getCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + roleCode);
		if (Objects.nonNull(role)) {
			return role;
		}
		role = this.getOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleKey, roleCode));
		if (Objects.nonNull(role)) {
			redisCache.setCacheObject(SysConstants.CACHE_SYS_DEPT_KEY + roleCode, role);
		}
		return role;
	}

	/**
	 * 根据用户ID查询有效角色列表
	 * 
	 * @param userId
	 *            用户ID
	 * @return 角色列表
	 */
	@Override
	public List<SysRole> selectRolesByUserId(Long userId) {
		return roleMapper.selectRolesByUserId(userId).stream().filter(r -> r.isEnable()).toList();
	}

	/**
	 * 根据用户ID查询角色编码雷暴
	 * 
	 * @param userId
	 *            用户ID
	 * @return 权限列表
	 */
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

	/**
	 * 新增保存角色信息
	 * 
	 * @param role
	 *            角色信息
	 * @return 结果
	 */
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

	/**
	 * 修改保存角色信息
	 * 
	 * @param role
	 *            角色信息
	 * @return 结果
	 */
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

	/**
	 * 修改角色状态
	 * 
	 * @param role
	 *            角色信息
	 * @return 结果
	 */
	@Override
	public void updateRoleStatus(SysRole role) {
		SysRole db = this.getById(role.getRoleId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(role.getRoleId()));
		
		db.setStatus(role.getStatus());
		db.setUpdateTime(LocalDateTime.now());
		this.updateById(db);
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_ROLE_KEY + db.getRoleKey());
	}

	/**
	 * 批量删除角色信息
	 * 
	 * @param roleIds
	 *            需要删除的角色ID
	 * @return 结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteRoleByIds(List<Long> roleIds) {
		for (Long roleId : roleIds) {
			SysRole role = this.getById(roleId);
			Long userCount = userRoleMapper
					.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
			Assert.isTrue(userCount == 0, () -> SysErrorCode.ROLE_USER_NOT_EMPTY.exception(role.getRoleKey()));
			if (userCount > 0) {
				throw new ServiceException(StringUtils.messageFormat("角色`{0}`存在关联用户，请先移除关联用户。", role.getRoleName()));
			}
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_POST_KEY + role.getRoleKey());
		}
		this.removeByIds(roleIds);
	}

	/**
	 * 批量取消授权用户角色
	 * 
	 * @param roleId
	 *            角色ID
	 * @param userIds
	 *            需要取消授权的用户数据ID
	 * @return 结果
	 */
	@Override
	public void deleteAuthUsers(Long roleId, List<Long> userIds) {
		LambdaQueryWrapper<SysUserRole> q = new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId)
				.in(SysUserRole::getUserId, userIds);
		userRoleMapper.delete(q);
	}

	/**
	 * 批量选择授权用户角色
	 * 
	 * @param roleId
	 *            角色ID
	 * @param userIds
	 *            需要授权的用户数据ID
	 * @return 结果
	 */
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
