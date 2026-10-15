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

import com.chestnut.common.security.domain.BaseDTO;
import com.chestnut.system.fixed.dict.Gender;
import com.chestnut.system.validator.Dict;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateUserProfileRequest extends BaseDTO {

	@NotBlank
	@Length(max = 30)
	private String nickName;

	@Length(max = 50)
	private String realName;

	@NotBlank
	@Length(max = 50)
	private String email;

	@NotBlank
	@Length(max = 14)
	@Pattern(regexp = "[0-9]+")
	private String phoneNumber;

	@Length(max = 4)
	@Pattern(regexp = "^$|^\\+[1-9][0-9]{0,2}$")
	private String phoneCallingCode;

	@Dict(value = Gender.TYPE)
	private String sex;

	private LocalDateTime birthday;
}
