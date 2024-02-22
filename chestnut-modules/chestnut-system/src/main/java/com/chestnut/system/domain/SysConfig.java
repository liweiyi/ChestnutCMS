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

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.i18n.I18nField;
import com.chestnut.common.db.domain.BaseEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 参数配置表 sys_config
 */
@Getter
@Setter
@TableName(SysConfig.TABLE_NAME)
public class SysConfig extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_config";

	/** 参数主键 */
	@ExcelProperty("参数主键")
	@TableId(value = "config_id", type = IdType.INPUT)
	private Long configId;

	/** 参数名称 */
	@ExcelProperty("参数名称")
	@I18nField("{CONFIG.#{configKey}}")
	private String configName;

	/** 参数键名 */
	@ExcelProperty("参数键名")
	private String configKey;

	/** 参数键值 */
	@ExcelProperty("参数键值")
	private String configValue;

	/**
	 * 是否系统固定配置参数
	 */
	@TableField(exist = false)
	private String fixed;

	@NotBlank(message = "参数名称不能为空")
	@Size(min = 0, max = 100, message = "参数名称不能超过100个字符")
	public String getConfigName() {
		return configName;
	}

	@NotBlank(message = "参数键名长度不能为空")
	@Size(min = 0, max = 100, message = "参数键名长度不能超过100个字符")
	public String getConfigKey() {
		return configKey;
	}

	@NotBlank(message = "参数键值不能为空")
	@Size(min = 0, max = 500, message = "参数键值长度不能超过500个字符")
	public String getConfigValue() {
		return configValue;
	}
}
