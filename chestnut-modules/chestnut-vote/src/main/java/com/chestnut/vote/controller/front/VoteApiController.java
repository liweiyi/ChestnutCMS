package com.chestnut.vote.controller.front;

import com.chestnut.system.annotation.IgnoreDemoMode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.system.validator.LongId;
import com.chestnut.vote.domain.dto.VoteSubmitDTO;
import com.chestnut.vote.domain.vo.VoteVO;
import com.chestnut.vote.service.IVoteApiService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vote")
public class VoteApiController extends BaseRestController {

	private final IVoteApiService voteApiService;

	@GetMapping("/{voteId}")
	public R<?> getVoteDetail(@PathVariable @LongId Long voteId) {
		VoteVO vote = this.voteApiService.getVote(voteId);
		return R.ok(vote);
	}

	@GetMapping("/result/{voteId}")
	public R<?> getVoteResult(@PathVariable @LongId Long voteId) {
		VoteVO vote = this.voteApiService.getVote(voteId);
		return R.ok(vote);
	}

	@IgnoreDemoMode
	@PostMapping
	public R<?> submitVote(@RequestBody @Validated VoteSubmitDTO dto, HttpServletRequest request) {
		dto.setIp(ServletUtils.getIpAddr(request));
		dto.setUserAgent(ServletUtils.getUserAgent(request));
		this.voteApiService.submitVote(dto);
		return R.ok();
	}
}
