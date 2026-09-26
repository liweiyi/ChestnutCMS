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

	/**
	 * 冗余字段
	 */
	private String userName;

	/**
	 * 冗余字段
	 */
	private String nickName;

	/**
	 * 冗余字段
	 */
	private String realName;

	/**
	 * 冗余字段
	 */
	private String phoneNumber;

	/**
	 * 冗余字段
	 */
	private String email;

	public static SysUserRole of(SysUser user, Long roleId) {
		SysUserRole ur = new SysUserRole();
		ur.setRoleId(roleId);
		ur.setUserId(user.getUserId());
		ur.setUserName(user.getUserName());
		ur.setNickName(user.getNickName());
		ur.setRealName(user.getRealName());
		ur.setPhoneNumber(user.getPhoneNumber());
		ur.setEmail(user.getEmail());
		return ur;
	}
}
