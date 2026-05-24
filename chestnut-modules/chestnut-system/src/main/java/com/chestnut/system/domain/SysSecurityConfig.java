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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

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

	@Serial
	private static final long serialVersionUID = 1L;
	
	public final static String TABLE_NAME = "sys_security_config";
	
	@TableId(value = "config_id", type = IdType.INPUT)
	private Long configId;

	private String name;
	
	/**
	 * 是否启用
	 */
	private String status;

	/**
	 * 安全配置
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private ObjectNode configs;

	/**
	 * 密码最小长度
	 */
	@Deprecated
	private Integer passwordLenMin;

	/**
	 * 密码最大长度
	 */
	@Deprecated
	private Integer passwordLenMax;

	/**
	 * 密码校验规则，默认：包含字母数字<br/>
	 */
	@Deprecated
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
	@Deprecated
	private String weakPasswords;

	/**
	 * 后台添加的用户首次登陆是否需要强制修改密码
	 */
	@Deprecated
	private String forceModifyPwdAfterAdd;

	/**
	 * 后台重置密码后首次登陆是否需要强制修改密码
	 */
	@Deprecated
	private String forceModifyPwdAfterReset;

	/**
	 * 密码过期时间长度，单位：秒
	 */
	@Deprecated
	private Integer passwordExpireSeconds;

	/**
	 * 触发密码重试安全策略的次数上限
	 */
	@Deprecated
	private Integer passwordRetryLimit;

	/**
	 * 密码重试安全策略<br/>
	 * @see com.chestnut.system.fixed.dict.PasswordRetryStrategy
	 */
	@Deprecated
	private String passwordRetryStrategy;

	/**
	 * 密码重试安全策略锁定时长，单位：秒
	 */
	@Deprecated
	private Integer passwordRetryLockSeconds;

    /**
	 * 验证码是否启用
	 */
	@Deprecated
    private String captchaEnable;

    /**
	 * 验证码类型
     */
	@Deprecated
    private String captchaType;

    /**
	 * 验证码过期时长，单位：秒
	 */
	@Deprecated
    private Integer captchaExpires;

    /**
	 * 验证码重新生成间隔时长，单位：秒
	 */
	@Deprecated
    private Integer captchaDuration;

    /**
	 * 第三方登录配置ID，多个用逗号分隔
	 */
	@Deprecated
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Long> loginTypeConfigIds;

	public ObjectNode getConfigs() {
		if (Objects.isNull(this.configs)) {
			this.configs = JacksonUtils.objectNode();
		}
		return this.configs;
	}
	
	public boolean isEnable() {
		return EnableOrDisable.isEnable(this.status);
	}
}
