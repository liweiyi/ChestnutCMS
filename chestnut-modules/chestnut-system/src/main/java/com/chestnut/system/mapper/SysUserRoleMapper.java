package com.chestnut.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chestnut.system.domain.SysUserRole;

/**
 * 用户与角色关联表 数据层
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

	/**
	 * 批量新增用户角色信息
	 * 
	 * @param userRoleList
	 *            用户角色列表
	 * @return 结果
	 */
	@Insert("""
			<script>
				insert into sys_user_role(user_id, role_id) values
				<foreach item="item" index="index" collection="list" separator=",">
					(#{item.userId},#{item.roleId})
				</foreach>
			</script>
			""")
	public int batchInserUserRoles(List<SysUserRole> userRoleList);
}
