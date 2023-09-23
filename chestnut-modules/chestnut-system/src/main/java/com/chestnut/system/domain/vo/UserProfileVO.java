package com.chestnut.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.chestnut.system.domain.SysUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileVO {

	/**
	 * 用户基础信息
	 */
	@JsonIgnoreProperties({ "password" })
	private SysUser user;
	
	/**
	 * 角色
	 */
	private String roleGroup;
	
	/**
	 * 岗位
	 */
	private String postGroup;
}
