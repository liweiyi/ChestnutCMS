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
package com.chestnut.member.domain.dto;

import com.chestnut.common.security.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

/***
 * 会员等级配置DTO
 */
@Getter
@Setter
public class CreateLevelConfigRequest extends BaseDTO {
	
	/**
	 * 等级类型
	 */
	@NotBlank
	@Length(max = 30)
	private String levelType;

	/**
	 * 显示级别
	 */
	@NotNull
	private Integer level;
	
	/**
	 * 等级名称
	 */
	@NotBlank
	@Length(max = 30)
	private String name;
	
	/**
	 * 等级图标
	 */
	@Length(max = 100)
	private String icon;
	
	/**
	 * 升级到下一级所需经验值
	 */
	@NotNull
	private Long nextNeedExp;
	
	/**
	 * 备注
	 */
	@Length(max = 500)
	private String remark;
}
