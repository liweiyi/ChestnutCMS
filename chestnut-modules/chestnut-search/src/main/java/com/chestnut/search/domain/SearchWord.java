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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.chestnut.common.db.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 搜索词统计
 */
@Getter
@Setter
@TableName(SearchWord.TABLE_NAME)
public class SearchWord extends BaseEntity {

	@Serial
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "search_word";

	@TableId(value = "word_id", type = IdType.AUTO)
	private Long wordId;

	/**
	 * 搜索词
	 */
	private String word;

	/**
	 * 置顶标识
	 */
	private Long topFlag;

	/**
	 * 置顶结束时间
	 */
	private LocalDateTime topDate;

	/**
	 * 历史总搜索次数
	 */
	private Long searchTotal;

	/**
	 * 来源标识，例如：cms:siteId
	 */
	private String source;
}