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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Groovy脚本表 sys_groovy_script
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysGroovyScript.TABLE_NAME)
public class SysGroovyScript extends BaseEntity {
	
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_groovy_script";

	@TableId(value = "script_id", type = IdType.INPUT)
	private Long scriptId;

	private String name;

	private String scriptText;
}
