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
package com.chestnut.system.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.utils.poi.converter.LocalDateTimeConverter;
import com.chestnut.system.annotation.ExcelDictField;
import com.chestnut.system.config.converter.DictConverter;
import com.chestnut.system.fixed.dict.Gender;
import com.chestnut.system.fixed.dict.UserStatus;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.ISecurityUser;
import com.chestnut.system.validator.Dict;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户对象 sys_user
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = SysUser.TABLE_NAME, autoResultMap = true)
public class SysUser extends BaseEntity implements ISecurityUser {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_user";

	/** 用户ID */
	@ExcelProperty
	@TableId(value = "user_id", type = IdType.INPUT)
	private Long userId;

	/** 部门ID */
	@ExcelIgnore
	private Long deptId;

	@ExcelProperty
	@TableField(exist = false)
	private String deptName;

	/** 用户账号 */
	@ExcelProperty
	private String userName;

	/** 用户昵称 */
	@ExcelProperty
	private String nickName;

	/** 真实姓名 */
	@ExcelProperty
	private String realName;

	/** 用户邮箱 */
	@ExcelProperty
	private String email;

	/** 手机号码 */
	@ExcelProperty
	private String phoneNumber;

	/** 用户性别 */
	@ExcelProperty(converter = DictConverter.class)
	@ExcelDictField(Gender.TYPE)
	@Dict(value = Gender.TYPE, message = "{VALIDATOR.SYSTEM.USER_GENDER}")
	private String sex;

	/** 出生日期 */
	@ExcelProperty
	private LocalDateTime birthday;

	/** 用户头像 */
	@ExcelIgnore
	private String avatar;

	@ExcelIgnore
	@TableField(exist = false)
	private String avatarSrc;

	/** 密码 */
	@ExcelIgnore
	private String password;

	/** 帐号状态 */
	@ExcelProperty(converter = DictConverter.class)
	@ExcelDictField(UserStatus.TYPE)
	@Dict(UserStatus.TYPE)
	private String status;

	/** 最后登录IP */
	@ExcelProperty
	private String loginIp;

	/** 最后登录时间 */
	@ColumnWidth(16)
	@ExcelProperty(converter = LocalDateTimeConverter.class)
	private LocalDateTime loginDate;

	/**
	 * 最近修改密码时间
	 */
	@ExcelIgnore
	private LocalDateTime passwordModifyTime;

	/**
	 * 是否需要登录修改密码标识
	 */
	@ExcelIgnore
	private String forceModifyPassword;

	@ColumnWidth(16)
	@ExcelProperty(converter = LocalDateTimeConverter.class)
	private LocalDateTime lockEndTime;

    @TableField(typeHandler = JacksonTypeHandler.class)
	private Map<String, Object> preferences;

	/** 角色对象 */
	@ExcelIgnore
	@TableField(exist = false)
	private List<SysRole> roles;

	/** 角色组 */
	@ExcelIgnore
	@TableField(exist = false)
	private Long[] roleIds;

	/** 岗位组 */
	@ExcelIgnore
	@TableField(exist = false)
	private Long[] postIds;

	/** 角色ID */
	@ExcelIgnore
	@TableField(exist = false)
	private Long roleId;

	@Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
	public String getNickName() {
		return nickName;
	}

	@NotBlank(message = "用户账号不能为空")
	@Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
	public String getUserName() {
		return userName;
	}

	@Email(message = "邮箱格式不正确")
	@Size(min = 0, max = 50, message = "邮箱长度不能超过50个字符")
	public String getEmail() {
		return email;
	}

	@Size(min = 0, max = 11, message = "手机号码长度不能超过11个字符")
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public boolean checkForceModifyPassword() {
		return YesOrNo.isYes(this.forceModifyPassword);
	}

	public boolean isAccountNonLocked() {
		// 状态未锁定状态，并且锁定解锁时间未空或者未过期
		return !UserStatus.isLocked(this.status) || (Objects.nonNull(this.getLockEndTime()) && LocalDateTime.now().isAfter(this.getLockEndTime()));
	}
	
	/**
	 * 数据变更标识
	 */
	@TableField(exist = false)
	private boolean modified;

	@Override
	public String getType() {
		return AdminUserType.TYPE;
	}

	@Override
	public void disableUser() {
		this.status = UserStatus.DISABLE;
		this.modified = true;
	}
	
	@Override
	public void lockUser(LocalDateTime lockEndTime) {
		this.status = UserStatus.LOCK;
		this.lockEndTime = lockEndTime;
		this.modified = true;
	}

	@Override
	public void forceModifyPassword() {
		this.forceModifyPassword = YesOrNo.YES;
		this.modified = true;
	}
}
