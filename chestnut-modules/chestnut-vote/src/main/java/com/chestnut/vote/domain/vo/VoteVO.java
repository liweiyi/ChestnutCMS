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
package com.chestnut.vote.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.chestnut.common.annotation.XComment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XComment("{CC.VOTE.ENTITY}")
public class VoteVO {

	@XComment("{CC.VOTE.ID}")
	private Long voteId;

	@XComment("{CC.VOTE.TITLE}")
	private String title;

	@XComment("{CC.VOTE.START_TIME}")
	private LocalDateTime startTime;

	@XComment("{CC.VOTE.END_TIME}")
	private LocalDateTime endTime;

	@XComment("{CC.VOTE.USER_TYPE}")
	private String userType;

	@XComment("{CC.VOTE.DAY_LIMIT}")
	private Integer dayLimit;

	@XComment("{CC.VOTE.TOTAL_LIMIT}")
	private Integer totalLimit;

	@XComment("{CC.VOTE.VIEW_TYPE}")
	private String viewType;

	@XComment("{CC.VOTE.TOTAL}")
	private Integer total;

	@XComment("{CC.VOTE.SUBJECT_LIST}")
	private List<VoteSubjectVO> subjects;
}
