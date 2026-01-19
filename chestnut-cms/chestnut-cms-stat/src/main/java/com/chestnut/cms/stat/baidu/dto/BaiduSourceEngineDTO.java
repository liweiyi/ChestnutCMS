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
package com.chestnut.cms.stat.baidu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BaiduSourceEngineDTO {

	/**
	 * 站点ID
	 */
	@NotNull(message = "{VALIDATOR.CMS.STAT.BD_SITE_ID_NOT_NULL}")
	private Long siteId;
	
	/**
	 * 开始时间
	 */
	@NotNull(message = "{VALIDATOR.CMS.STAT.START_DATE_NOT_NULL}")
	private LocalDateTime startDate;
	
	/**
	 * 结束时间
	 */
	@NotNull(message = "{VALIDATOR.CMS.STAT.END_DATE_NOT_NULL}")
	private LocalDateTime endDate;
	
	/**
	 * 指标
	 */
	private List<String> metrics;

	/**
	 * 地域
	 */
	private String area;

	/**
	 * 设备类型
	 * pc = 计算机
	 * mobile = 移动设备
	 */
	private String clientDevice;

	/**
	 * 访客类型
	 * new = 新访客
	 * old = 老访客
	 */
	private String visitor;
}
