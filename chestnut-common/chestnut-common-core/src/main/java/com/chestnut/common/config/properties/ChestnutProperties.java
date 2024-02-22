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
package com.chestnut.common.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 读取项目相关配置
 *
 * @author 兮玥（190785909@qq.com）
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "chestnut")
public class ChestnutProperties {
	
	/** 项目名称 */
	private String name;

	/** 项目代号 */
	private String alias;

	/** 版本 */
	private String version;

	/** 版权年份 */
	private String copyrightYear;
	
	/**
	 * 雪花算法参数配置
	 */
	private Snowflake snowflake;

	public static class Snowflake {

		private short workerId = 1;

		public short getWorkerId() {
			return workerId;
		}

		public void setWorkerId(short workerId) {
			this.workerId = workerId;
		}
	}
}
