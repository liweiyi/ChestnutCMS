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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.fixed.dict.YesOrNo;

import lombok.Getter;
import lombok.Setter;

/**
 * 安全配置
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = SysSecurityConfig.TABLE_NAME, autoResultMap = true)
public class SysSecurityConfig extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public final static String TABLE_NAME = "sys_security_config";
	
	@TableId(value = "config_id", type = IdType.INPUT)
	private Long configId;
	
	/**
	 * 是否启用
	 */
	private String status;
	
	/**
	 * 密码最小长度
	 */
	private Integer passwordLenMin;

	/**
	 * 密码最大长度
	 */
	private Integer passwordLenMax;
	
	/**
	 * 密码校验规则，默认：包含字母数字<br/>
	 */
	private String passwordRule;
	
	/**
	 * 密码校验规则正则表达式
	 */
	@TableField(exist = false)
	private String passwordRulePattern;
	
	/**
	 * 密码中不允许包含的用户信息<br/>
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private String[] passwordSensitive;
	
	/**
	 * 禁用弱密码数组<br/>
	 * 例如：123456, 666666, qweqwe等常见密码
	 */
	private String weakPasswords;
	
	/**
	 * 后台添加的用户首次登陆是否需要强制修改密码
	 */
	private String forceModifyPwdAfterAdd;
	
	/**
	 * 后台重置密码后首次登陆是否需要强制修改密码
	 */
	private String forceModifyPwdAfterReset;
	
	/**
	 * 密码过期时间长度，单位：秒
	 */
	private Integer passwordExpireSeconds;
	
	/**
	 * 触发密码重试安全策略的次数上限
	 */
	private Integer passwordRetryLimit;
	
	/**
	 * 密码重试安全策略<br/>
	 * @see com.chestnut.system.fixed.dict.PasswordRetryStrategy.auth.enums.PasswordErrStrategy
	 */
	private String passwordRetryStrategy;
	
	/**
	 * 密码重试安全策略锁定时长，单位：秒
	 */
	private Integer passwordRetryLockSeconds;
	
	@JsonIgnore
	@TableField(exist = false)
	private LoginUser operator;
	
	public boolean isEnable() {
		return EnableOrDisable.isEnable(this.status);
	}
	
	public boolean checkForceModifyPwdAfterAdd() {
		return YesOrNo.isYes(this.forceModifyPwdAfterAdd);
	}
	
	public boolean checkForceModifyPwdAfterReset() {
		return YesOrNo.isYes(this.forceModifyPwdAfterReset);
	}
}
