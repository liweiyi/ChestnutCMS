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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName(CmsAdViewLog.TABLE_NAME)
public class CmsAdViewLog implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;
	
	public final static String TABLE_NAME = "cms_ad_view_log";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;
	
	/**
	 * 站点ID
	 */
	private Long siteId;
	
	/**
	 * 广告ID
	 */
	private Long adId;
	
	/**
	 * 广告名称
	 */
	@TableField(exist = false)
	private String adName;

	/**
	 * 请求域
	 */
	private String host;

	/**
	 * IP地址
	 */
	private String ip;

	/**
	 * IP所属地区
	 */
	private String address;

	/**
	 * 来源地址
	 */
	private String referer;

	/**
	 * 浏览器类型
	 */
	private String browser;

	/**
	 * UserAgent
	 */
	private String userAgent;

	/**
	 * 操作系统
	 */
	private String os;

	/**
	 * 设备类型
	 */
	private String deviceType;

	/**
	 * 语言
	 */
	private String locale;

	/**
	 * 发生时间
	 */
	private LocalDateTime evtTime;
}
