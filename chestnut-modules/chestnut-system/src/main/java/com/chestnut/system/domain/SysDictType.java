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

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.converters.longconverter.LongStringConverter;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.common.i18n.I18nField;
import com.chestnut.system.annotation.ExcelDictField;
import com.chestnut.system.config.converter.DictConverter;
import com.chestnut.system.fixed.dict.YesOrNo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;

/**
 * 字典类型表 sys_dict_type
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysDictType.TABLE_NAME)
public class SysDictType extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_dict_type";

	@XComment("{ENT.SYS.DICT_TYPE.ID}")
	@ExcelProperty(value = "{ENT.SYS.DICT_TYPE.ID}" ,converter = LongStringConverter.class)
	@TableId(value = "dict_id", type = IdType.INPUT)
	private Long dictId;

	@XComment("{ENT.SYS.DICT_TYPE.NAME}")
	@ExcelProperty("{ENT.SYS.DICT_TYPE.NAME}")
	@I18nField("{DICT.#{dictType}}")
	private String dictName;

	@XComment("{ENT.SYS.DICT_TYPE.TYPE}")
	@ExcelProperty("{ENT.SYS.DICT_TYPE.TYPE}")
	private String dictType;

	@XComment("{ENT.SYS.DICT_TYPE.FIXED}")
	@ExcelDictField(YesOrNo.TYPE)
	@ExcelProperty(value = "{ENT.SYS.DICT_TYPE.FIXED}", converter = DictConverter.class)
	@TableField(exist = false)
	private String fixed;
}
