/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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

import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.converters.longconverter.LongStringConverter;
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
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_user";

	@ExcelProperty(value = "{ENT.SYS.USER.ID}" ,converter = LongStringConverter.class)
	@TableId(value = "user_id", type = IdType.INPUT)
	private Long userId;

	/** 部门ID */
	@ExcelIgnore
	private Long deptId;

	@ExcelProperty("{ENT.SYS.USER.DEPT_NAME}")
	@TableField(exist = false)
	private String deptName;

	/** 用户账号 */
	@ExcelProperty("{ENT.SYS.USER.USER_NAME}")
	@NotBlank
	@Length(max = 30)
	@Pattern(regexp = "^[A-Za-z0-9_]+$")
	private String userName;

	/** 用户昵称 */
	@ExcelProperty("{ENT.SYS.USER.NICK_NAME}")
	@Length(max = 30)
	private String nickName;

	/** 真实姓名 */
	@ExcelProperty("{ENT.SYS.USER.REAL_NAME}")
	private String realName;

	/** 用户邮箱 */
	@ExcelProperty("{ENT.SYS.USER.MAIL}")
	@Email
	@Length(max = 50)
	private String email;

	/** 手机号码 */
	@ExcelProperty("{ENT.SYS.USER.PHONE}")
	@Length(max = 11)
	private String phoneNumber;

	/** 用户性别 */
	@ExcelProperty(value = "{ENT.SYS.USER.GENDER}", converter = DictConverter.class)
	@ExcelDictField(Gender.TYPE)
	@Dict(value = Gender.TYPE)
	private String sex;

	/** 出生日期 */
	@ExcelProperty("{ENT.SYS.USER.BIRTHDAY}")
	private LocalDateTime birthday;

	/** 用户头像 */
	@ExcelIgnore
	private String avatar;

	@ExcelIgnore
	@TableField(exist = false)
	private String avatarSrc;

	/** 密码 */
	@ExcelIgnore
	@NotBlank
	private String password;

	/** 帐号状态 */
	@ExcelProperty(value = "{ENT.SYS.USER.STATUS}", converter = DictConverter.class)
	@ExcelDictField(UserStatus.TYPE)
	@Dict(UserStatus.TYPE)
	private String status;

	/** 最后登录IP */
	@ExcelProperty("{ENT.SYS.USER.LOGIN_IP}")
	private String loginIp;

	/** 最后登录时间 */
	@ColumnWidth(16)
	@ExcelProperty(value = "{ENT.SYS.USER.LOGIN_TIME}", converter = LocalDateTimeConverter.class)
	private LocalDateTime loginDate;

	/**
	 * 最近修改密码时间
	 */
	@ExcelProperty(value = "{ENT.SYS.USER.PWD_MODIFY_TIME}", converter = LocalDateTimeConverter.class)
	private LocalDateTime passwordModifyTime;

	/**
	 * 是否需要登录修改密码标识
	 */
	@ExcelProperty("{ENT.SYS.USER.FORCE_MODIFY_PWD}")
	private String forceModifyPassword;

	/**
	 * 锁定结束时间
	 */
	@ExcelProperty(value = "{ENT.SYS.USER.LOCK_END_TIME}", converter = LocalDateTimeConverter.class)
	private LocalDateTime lockEndTime;

	/**
	 * 用户偏好配置
	 */
	@ExcelIgnore
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
