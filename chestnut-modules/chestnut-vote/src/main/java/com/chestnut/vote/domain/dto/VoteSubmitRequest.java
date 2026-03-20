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
package com.chestnut.vote.domain.dto;

import com.chestnut.common.annotation.XComment;
import com.chestnut.system.validator.LongId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@XComment("{API.DOC.VOTE.API.SUBMIT_REQ}")
public class VoteSubmitRequest {

	@LongId
	@XComment("{CC.VOTE.ID}")
	private Long voteId;

	@NotEmpty
	@Valid
	@XComment("{API.DOC.VOTE.API.SUBJECT_RESULTS}")
	private List<SubjectResult> subjects;

	@XComment("{API.DOC.VOTE.API.IP}")
	private String ip;

	@XComment("{API.DOC.VOTE.API.USER_AGENT}")
	private String userAgent;

	@Getter
	@Setter
	@XComment("{API.DOC.VOTE.API.SUBJECT_RESULT}")
	public static class SubjectResult {

		@LongId
		@XComment("{CC.VOTE_SUBJECT.ID}")
		private Long subjectId;

		@NotBlank
		@XComment("{CC.VOTE_SUBJECT.TYPE}")
		private String type;

		@NotEmpty
		@XComment("{API.DOC.VOTE.API.SUBJECT_ANSWER}")
		private ArrayList<String> result;
	}
}
