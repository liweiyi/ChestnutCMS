package com.chestnut.system.domain.vo;

import java.util.List;

import com.chestnut.system.domain.SysPost;
import com.chestnut.system.domain.SysRole;
import com.chestnut.system.domain.SysUser;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserInfoVO {

	private SysUser user;
	
	private List<SysRole> roles;
	
	private List<SysPost> posts;
}
