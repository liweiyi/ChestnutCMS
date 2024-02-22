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

import org.hibernate.validator.constraints.Length;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 国际化翻译字典表 sys_i18n_dict
 * 
 * <p>
 * 适用字符长度不超过100的字段，在表字段添加@I18nFied注解，查询数据后根据此表自动转换成当前语言环境字符
 * </p>
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysI18nDict.TABLE_NAME)
public class SysI18nDict {

	public static final String TABLE_NAME = "sys_i18n_dict";

	@TableId(value = "dict_id", type = IdType.INPUT)
	private Long dictId;
	
	/**
	 * 各国语言环境简称字符串，例如：zh-CN=中文（简体），en-US=英文（美国）
	 */
	@NotBlank
	@Length(max = 10)
	private String langTag;

	/**
	 * 需要翻译的字符串唯一标识
	 */
	@NotBlank
	@Length(max = 100)
	private String langKey;

	/**
	 * 当前语言黄精langId对应的翻译
	 */
	@NotBlank
	@Length(max = 100)
	private String langValue;
}
