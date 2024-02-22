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
package com.chestnut.advertisement.domain;

import java.io.Serial;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 广告小时点击/展现统计数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(CmsAdHourStat.TABLE_NAME)
public class CmsAdHourStat implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_ad_hour_stat";

	@TableId(value = "stat_id", type = IdType.AUTO)
	private Long statId;
	
	/**
	 * 所属站点ID
	 */
	private Long siteId;
	
	/**
	 * 统计周期，格式：yyyyMMddHH
	 */
	private String hour;
	
	/**
	 * 广告ID
	 */
	private Long advertisementId;
	
	/**
	 * 点击数
	 */
	private Integer click;
	
	/**
	 * 展现数
	 */
	private Integer view;
	
	/**
	 * 广告名称
	 */
	@TableField(exist = false)
	private String adName;
}
