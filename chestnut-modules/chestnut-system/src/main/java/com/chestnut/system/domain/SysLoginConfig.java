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
 * 登录配置
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = SysLoginConfig.TABLE_NAME, autoResultMap = true)
public class SysLoginConfig extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public final static String TABLE_NAME = "sys_login_config";
	
	@TableId(value = "config_id", type = IdType.INPUT)
	private Long configId;

    /**
	 * 类型
	 */
    private String type;

	/**
	 * 名称
	 */
	private String configName;

    /**
	 * 描述
	 */
    private String configDesc;

    /**
	 * 状态
	 */
    private String status;

    /**
	 * 配置属性
	 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ObjectNode configProps;
}
