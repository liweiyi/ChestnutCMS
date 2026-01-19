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

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class CreateI18nDictRequest {

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
	 * 当前语言环境langKey对应的翻译
	 */
	@NotBlank
	@Length(max = 255)
	private String langValue;
}

