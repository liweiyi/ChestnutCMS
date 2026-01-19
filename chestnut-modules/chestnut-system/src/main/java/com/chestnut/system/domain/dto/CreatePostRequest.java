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
package com.chestnut.system.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.EnableOrDisable;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class CreatePostRequest extends BaseDTO {

	@NotBlank
	@Length(max = 64)
	@XComment("{ENT.SYS.POST.CODE}")
	private String postCode;

	/** 岗位名称 */
	@NotBlank
	@Length(max = 50)
	@XComment("{ENT.SYS.POST.NAME}")
	private String postName;

	@NotNull
	@XComment("{CC.ENTITY.SORT}")
	private Integer postSort;

	/** 状态（0正常 1停用） */
	@Dict(EnableOrDisable.TYPE)
	@XComment("{CC.ENTITY.STATUS}")
	private String status;

	/** 状态（0正常 1停用） */
	@XComment("{CC.ENTITY.REMARK}")
	@Length(max = 500)
	private String remark;
}

