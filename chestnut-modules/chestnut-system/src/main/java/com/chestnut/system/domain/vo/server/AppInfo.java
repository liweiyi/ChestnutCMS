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
package com.chestnut.system.domain.vo.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppInfo {

	/**
	 * 系统名称
	 */
	private String name;

	/**
	 * 系统代号
	 */
	private String alias;

	/**
	 * 系统版本
	 */
	private String version;
}