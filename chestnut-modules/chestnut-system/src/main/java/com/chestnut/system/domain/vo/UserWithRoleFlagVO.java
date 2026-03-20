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
package com.chestnut.system.domain.vo;

import com.chestnut.common.annotation.XComment;
import com.chestnut.system.domain.SysUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 未分配指定角色的用户列表
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@XComment("{API.DOC.SYS.ROLE.USER_WITH_FLAG_VO}")
public class UserWithRoleFlagVO {

	/**
	 * 用户ID
	 */
	@XComment("{ENT.SYS.USER.ID}")
	private Long userId;

	/**
	 * 用户名
	 */
	@XComment("{ENT.SYS.USER.USER_NAME}")
	private String userName;

	/**
	 * 昵称
	 */
	@XComment("{ENT.SYS.USER.NICK_NAME}")
	private String nickName;

	/**
	 * 手机号
	 */
	@XComment("{ENT.SYS.USER.PHONE}")
	private String phoneNumber;

    /**
     * email
     */
    @XComment("{ENT.SYS.USER.MAIL}")
    private String email;

    /**
     * 状态
     */
    @XComment("{ENT.SYS.USER.STATUS}")
    private String status;

	/**
	 * 创建时间
	 */
	@XComment("{CC.ENTITY.CREATE_TIME}")
	private LocalDateTime createTime;

	/**
	 * 角色ID
	 */
	@XComment("{ENT.SYS.ROLE.ID}")
	private Long roleId;

	/**
	 * 用户是否已分配到roleId角色下
	 */
	@XComment("{API.DOC.SYS.ROLE.USER_ALLOCATED}")
	private Boolean allocated;

	public static UserWithRoleFlagVO newInstance(SysUser user, Long roleId, Boolean allocated) {
		UserWithRoleFlagVO vo = new UserWithRoleFlagVO();
		vo.setUserId(user.getUserId());
		vo.setUserName(user.getUserName());
		vo.setNickName(user.getNickName());
		vo.setPhoneNumber(user.getPhoneNumber());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
		vo.setCreateTime(user.getCreateTime());
		vo.setRoleId(roleId);
		vo.setAllocated(allocated);
		return vo;
	}
}
