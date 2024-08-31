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
package com.chestnut.search.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 搜索日志
 */
@Getter
@Setter
@TableName(SearchLog.TABLE_NAME)
public class SearchLog implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "search_log";

	@TableId(value = "log_id", type = IdType.INPUT)
	private Long logId;

	/**
	 * 搜索词
	 */
	private String word;

	/**
	 * Header:UserAgent
	 */
	private String userAgent;

	/**
	 * IP地址
	 */
	private String ip;

	private String location;

	/**
	 * Header:Referer
	 */
	private String referer;

	/**
	 * 搜索客户端类型
	 */
	private String clientType;

	/**
	 * 来源
	 */
	private String source;

	/**
	 * 搜索时间
	 */
	private LocalDateTime logTime;
}