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
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.validator.Dict;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 微信配置表 sys_wechat_config
 */
@Getter
@Setter
@TableName(SysWeChatConfig.TABLE_NAME)
public class SysWeChatConfig extends BaseEntity {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_wechat_config";

	@TableId(value = "config_id", type = IdType.INPUT)
	private Long configId;

	/**
	 * 是否作为后台登录配置
	 */
	@Dict(YesOrNo.TYPE)
	private String backend;

	/**
	 * 微信应用ID
	 */
	private String appId;

	/**
	 * 微信应用秘钥
	 */
	private String appSecret;

	/**
	 * 用作生成签名
	 */
	private String token;

	/**
	 * 用作消息体加解密密钥
	 */
	private String encodingAesKey;

	@JsonIgnore
	@TableField(exist = false)
	private LoginUser operator;

	public boolean isForBackendLogin() {
		return YesOrNo.isYes(this.backend);
	}
}
