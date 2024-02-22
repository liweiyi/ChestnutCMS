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
package com.chestnut.cms.stat.baidu.vo;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaiduTimeTrendVO extends LineChartVO {

	private Integer offset;
	
	/**
	 * 时间范围
	 */
	private String timeSpan;
	
	/**
	 * 指标字段
	 */
	private List<String> fields;
	
	/**
	 * 总数
	 */
	private Integer total;
	
	/**
	 * 指标合计
	 */
	private Map<String, Object> sum;
	
	/**
	 * 
	 */
	private Map<String, Object> pageSum;
}
