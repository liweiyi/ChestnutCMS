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
package com.chestnut.vote.controller.front;

import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.validator.LongId;
import com.chestnut.vote.domain.dto.VoteSubmitRequest;
import com.chestnut.vote.domain.vo.VoteVO;
import com.chestnut.vote.service.IVoteApiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vote")
public class VoteApiController extends BaseRestController {

	private final IVoteApiService voteApiService;

	@GetMapping("/{voteId}")
	public R<?> getVoteDetail(@PathVariable @LongId Long voteId) {
		VoteVO vote = this.voteApiService.getVote(voteId);
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteId));
		return R.ok(vote);
	}

	@GetMapping("/result/{voteId}")
	public R<?> getVoteResult(@PathVariable @LongId Long voteId) {
		VoteVO vote = this.voteApiService.getVote(voteId);
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteId));
		return R.ok(vote);
	}

	@IgnoreDemoMode
	@PostMapping
	public R<?> submitVote(@RequestBody @Validated VoteSubmitRequest req, HttpServletRequest request) {
		req.setIp(ServletUtils.getIpAddr(request));
		req.setUserAgent(ServletUtils.getUserAgent(request));
		this.voteApiService.submitVote(req);
		return R.ok();
	}
}
