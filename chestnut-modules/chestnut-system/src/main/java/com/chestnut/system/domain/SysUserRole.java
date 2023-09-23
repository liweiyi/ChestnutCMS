package com.chestnut.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 用户和角色关联 sys_user_role
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@TableName(SysUserRole.TABLE_NAME)
public class SysUserRole {

	public static final String TABLE_NAME = "sys_user_role";
	
	/** 用户ID */
	private Long userId;

	/** 角色ID */
	private Long roleId;
}
