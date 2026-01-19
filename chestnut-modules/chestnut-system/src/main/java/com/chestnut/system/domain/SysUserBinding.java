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
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * 用户绑定对象 sys_user_binding
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = SysUserBinding.TABLE_NAME, autoResultMap = true)
public class SysUserBinding extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_user_binding";

	@TableId(value = "binding_id", type = IdType.INPUT)
    private Long bindingId;

	private Long userId;

	private String bindingType;

	private String openId;

	private String unionId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private ObjectNode properties;

    private String refreshToken;

    private Long tokenExpireTime;
}
