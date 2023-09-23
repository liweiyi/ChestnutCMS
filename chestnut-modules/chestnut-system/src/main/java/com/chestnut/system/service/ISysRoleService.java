package com.chestnut.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.domain.SysRole;

/**
 * 角色业务层
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISysRoleService extends IService<SysRole> {

	/**
	 * 根据用户ID查询角色列表
	 * 
	 * @param userId
	 *            用户ID
	 * @return 角色列表
	 */
	public List<SysRole> selectRolesByUserId(Long userId);

	/**
	 * 根据用户ID查询角色权限
	 * 
	 * @param userId
	 *            用户ID
	 * @return 权限列表
	 */
	public List<String> selectRoleKeysByUserId(Long userId);

	/**
	 * 新增保存角色信息
	 * 
	 * @param role
	 *            角色信息
	 * @return 结果
	 */
	public void insertRole(SysRole role);

	/**
	 * 修改保存角色信息
	 * 
	 * @param role
	 *            角色信息
	 * @return 结果
	 */
	public void updateRole(SysRole role);

	/**
	 * 修改角色状态
	 * 
	 * @param role
	 *            角色信息
	 * @return 结果
	 */
	public void updateRoleStatus(SysRole role);

	/**
	 * 批量删除角色信息
	 * 
	 * @param roleIds
	 *            需要删除的角色ID
	 * @return 结果
	 */
	public void deleteRoleByIds(List<Long> roleIds);

	/**
	 * 批量取消授权用户角色
	 * 
	 * @param roleId
	 *            角色ID
	 * @param userIds
	 *            需要取消授权的用户数据ID
	 * @return 结果
	 */
	public void deleteAuthUsers(Long roleId, List<Long> userIds);

	/**
	 * 批量选择授权用户角色
	 * 
	 * @param roleId
	 *            角色ID
	 * @param userIds
	 *            需要删除的用户数据ID
	 * @return 结果
	 */
	public void insertAuthUsers(Long roleId, List<Long> userIds);

	/**
	 * 获取缓存角色信息
	 * 
	 * @param roleCode
	 * @return
	 */
	SysRole getRole(String roleCode);
}
