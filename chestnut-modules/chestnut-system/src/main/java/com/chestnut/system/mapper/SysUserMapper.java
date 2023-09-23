package com.chestnut.system.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.system.domain.SysUser;
import com.chestnut.system.domain.SysUserRole;

/**
 * 用户表 数据层
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

	/**
	 * 关联指定角色的用户列表
	 * 
	 * @param page 分页信息
	 * @return 角色ID
	 */
	@Select("SELECT sys_user.* FROM " + SysUserRole.TABLE_NAME + " LEFT JOIN " + SysUser.TABLE_NAME
			+ " ON sys_user_role.user_id = sys_user.user_id WHERE sys_user_role.role_id = #{roleId}")
	public Page<SysUser> selectAllocatedList(Page<SysUserRole> page, @Param("roleId") Long roleId);

	/**
	 * 未关联指定角色列表
	 * 
	 * @param page 分页信息
	 * @return 角色ID
	 */
	@Select("SELECT u.* FROM sys_user as u WHERE u.user_id not in(SELECT user_id from sys_user_role where role_id = #{roleId})")
	public Page<SysUser> selectUnallocatedList(Page<SysUser> page, @Param("roleId") Long roleId);
}
