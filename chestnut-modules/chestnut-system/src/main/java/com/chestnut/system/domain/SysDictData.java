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
import com.chestnut.system.fixed.dict.YesOrNo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典数据表 sys_dict_data
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysDictData.TABLE_NAME)
public class SysDictData extends BaseEntity {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "sys_dict_data";

	/** 字典编码 */
	@ExcelProperty("字典编码")
	@TableId(value = "dict_code", type = IdType.INPUT)
	private Long dictCode;

	/** 字典排序 */
	@ExcelProperty("字典排序")
	private Long dictSort;

	/** 字典标签 */
	@ExcelProperty("字典标签")
	@I18nField("{DICT.#{dictType}.#{dictValue}}")
	private String dictLabel;

	/** 字典键值 */
	@ExcelProperty("字典键值")
	private String dictValue;

	/** 字典类型 */
	@ExcelProperty("字典类型")
	private String dictType;

	/** 样式属性（其他样式扩展） */
	@ExcelProperty("样式属性")
	private String cssClass;

	/** 表格字典样式 */
	@ExcelProperty("表格字典样式")
	private String listClass;

	/** 是否默认（Y是 N否） */
	@ExcelProperty("是否默认")
	private String isDefault;
	
	/**
	 * 是否系统固定字典数据
	 */
	@TableField(exist = false)
	private String fixed;

	@NotBlank(message = "字典标签不能为空")
	@Size(min = 0, max = 100, message = "字典标签长度不能超过100个字符")
	public String getDictLabel() {
		return dictLabel;
	}

	@NotBlank(message = "字典键值不能为空")
	@Size(min = 0, max = 100, message = "字典键值长度不能超过100个字符")
	public String getDictValue() {
		return dictValue;
	}

	@NotBlank(message = "字典类型不能为空")
	@Size(min = 0, max = 100, message = "字典类型长度不能超过100个字符")
	public String getDictType() {
		return dictType;
	}

	@Size(min = 0, max = 100, message = "样式属性长度不能超过100个字符")
	public String getCssClass() {
		return cssClass;
	}

	public boolean isDefault() {
		return YesOrNo.isYes(this.isDefault);
	}
}
