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
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;

/**
 * 角色表 sys_role
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(SysRole.TABLE_NAME)
public class SysRole extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "sys_role";

	/** 角色ID */
	@ExcelProperty(value = "{ENT.SYS.ROLE.ID}", converter = LongStringConverter.class)
	@TableId(value = "role_id", type = IdType.INPUT)
	private Long roleId;

	/** 角色名称 */
	@ExcelProperty("{ENT.SYS.ROLE.NAME}")
	@NotBlank
	@Length(max = 30)
	private String roleName;

	/** 角色权限 */
	@ExcelProperty("{ENT.SYS.ROLE.KEY}")
	@NotBlank
	@Length(max = 100)
	private String roleKey;

	/** 角色排序 */
	@ExcelProperty("{CC.ENTITY.SORT}")
	@NotNull
	private Integer roleSort;

	/** 角色状态（0正常 1停用） */
	@ExcelProperty("{CC.ENTITY.STATUS}")
	@Dict(EnableOrDisable.TYPE)
	private String status;

	public boolean isEnable() {
		return EnableOrDisable.isEnable(this.status);
	}
}
