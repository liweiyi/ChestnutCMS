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
package com.chestnut.common.log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 日志类型
	 */
	private String logType;
	
	/**
	 * 日志标题
	 */
	private String logTitle;
	
	/**
	 * 日志业务类型
	 */
	private String businessType;
	
	/**
	 * 日志时间
	 */
	private LocalDateTime logTime;

	/**
	 * 耗时，单位（毫秒）
	 */
	private Long cost;
	
	/**
	 * 日志详情
	 */
	private Map<String, Object> details;
}
