package com.chestnut.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUserRole;

/**
 * 角色表 数据层
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

	/**
	 * 根据用户ID查询角色
	 * 
	 * @param userName 用户名
	 * @return 角色列表
	 */
	@Select("SELECT r.* FROM " + SysUserRole.TABLE_NAME + " ur LEFT JOIN " + SysRole.TABLE_NAME
			+ " r ON ur.role_id = r.role_id WHERE ur.user_id = #{userId}")
	public List<SysRole> selectRolesByUserId(Long userId);
}
