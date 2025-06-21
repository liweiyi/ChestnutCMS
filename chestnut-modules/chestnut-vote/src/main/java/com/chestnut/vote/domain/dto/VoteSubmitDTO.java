/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.vote.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VoteSubmitDTO {

	/**
	 * ID
	 */
	private Long voteId;

	/**
	 * 问卷调查主题信息
	 */
	private List<SubjectResult> subjects;
	
	/**
	 * IP
	 */
	private String ip;
	
	/**
	 * UserAgent
	 */
	private String userAgent;

	@Getter
	@Setter
	public static class SubjectResult {

		/**
		 * 主题ID
		 */
		private Long subjectId;

		/**
		 * 主题类型
		 */
		private String type;

		/**
		 * 结果：itemId || inputText
		 */
		private ArrayList<String> result;
	}
}
