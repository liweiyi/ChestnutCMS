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
package com.chestnut.system.domain.dto;

import java.util.Set;

import com.alibaba.excel.annotation.ExcelProperty;
import com.chestnut.common.utils.poi.converter.StringToSetConverter;
import com.chestnut.system.annotation.ExcelDictField;
import com.chestnut.system.config.converter.DictConverter;
import com.chestnut.system.fixed.dict.UserStatus;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserImportData {

	@NotEmpty
	@ExcelProperty("用户名")
	private String userName;

	@NotEmpty
	@ExcelProperty("昵称")
	private String nickName;

	@NotEmpty
	@ExcelProperty("部门名称")
	private String deptName;

	@ExcelProperty("手机号")
	private String phoneNumber;

	@ExcelProperty("Email")
	private String email;

	@NotEmpty
	@ExcelProperty("密码")
	private String password;

	@ExcelProperty("性别")
	private String gender;

	@ExcelProperty(value = "状态", converter = DictConverter.class)
	@ExcelDictField(UserStatus.TYPE)
	private String status;

	@ExcelProperty(value = "岗位编码（多个英文分号分隔）", converter = StringToSetConverter.class)
	private Set<String> postCodes;

	@ExcelProperty(value = "角色编码（多个英文分号分隔）", converter = StringToSetConverter.class)
	private Set<String> roleCodes;
	
	private String remark;
}
