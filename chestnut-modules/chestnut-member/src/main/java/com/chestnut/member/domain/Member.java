/*
 * Copyright 2022-2024 兮玥(190785909@qq.com)
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
package com.chestnut.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.member.fixed.dict.MemberStatus;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.system.security.ISecurityUser;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/***
 * 会员表
 */
@Getter
@Setter
@TableName(Member.TABLE_NAME)
public class Member extends BaseEntity implements ISecurityUser {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member";

	@TableId(value = "member_id", type = IdType.INPUT)
    private Long memberId;
	
	/**
	 * 会员用户名
	 */
	private String userName;
	
	/**
	 * 会员密码
	 */
	private String password;
	
	/**
	 * 昵称
	 */
	private String nickName;
	
	/**
	 * 头像
	 */
	private String avatar;

	/**
	 * 个人主页封面图
	 */
	private String cover;

	/**
	 * 个性签名
	 */
	private String slogan;

	/**
	 * 个人简介
	 */
	private String description;
	
	/**
	 * 手机号
	 */
	private String phoneNumber;
	
	/**
	 * Email
	 */
	private String email;
	
	/**
	 * 出生日期
	 */
	private LocalDateTime birthday;
	
	/**
	 * 状态
	 */
	private String status; 
	
	/**
	 * 来源类型
	 */
	private String sourceType;
	
	/**
	 * 来源ID
	 */
	private String sourceId;
	
	/**
	 * 最近登录IP
	 */
	private String lastLoginIp;
	
	/**
	 * 最近登录时间
	 */
	private LocalDateTime lastLoginTime;
	
	/**
	 * 数据变更标识
	 */
	@TableField(exist = false)
	private boolean modified;

	@Override
	public String getType() {
		return MemberUserType.TYPE;
	}

	@Override
	public Long getUserId() {
		return this.memberId;
	}

	@Override
	public String getRealName() {
		return null;
	}

	@Override
	public void disableUser() {
		this.status = MemberStatus.DISABLE;
	}

	@Override
	public void lockUser(LocalDateTime lockEndTime) {
		this.status = MemberStatus.LOCK;
	}

	@Override
	public void forceModifyPassword() {
	}
}
