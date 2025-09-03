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
package com.chestnut.member.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.domain.MemberFollow;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberFollowService;
import com.chestnut.member.service.IMemberStatDataService;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Priv(type = MemberUserType.TYPE)
@RequiredArgsConstructor
@RestController
public class MemberFollowApiController extends BaseRestController {

	private final IMemberFollowService memberFollowService;

	private final IMemberStatDataService memberStatDataService;

	@GetMapping("/api/member/check_follow")
	public R<?> checkFollowMember(@RequestParam @NotBlank String targetIds) {
		String[] targetMemberStrIds = StringUtils.split(targetIds, ",");
		List<Long> targetMemberIds = Stream.of(targetMemberStrIds).map(Long::valueOf).toList();
		IdUtils.validate(targetMemberIds, true);
		long memberId = StpMemberUtil.getLoginIdAsLong();
		List<MemberFollow> list = this.memberFollowService.lambdaQuery().eq(MemberFollow::getMemberId, memberId)
				.in(MemberFollow::getFollowMemberId, targetMemberIds).list();
		Map<String, Boolean> map = new HashMap<>(list.size());
		list.forEach(mf -> map.put(mf.getFollowMemberId().toString(), true));
		return R.ok(map);
	}

	@IgnoreDemoMode
	@PostMapping("/api/member/follow")
	public R<?> followMember(@RequestParam @LongId Long targetId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFollowService.follow(memberId, targetId);
		return R.ok();
	}

	@IgnoreDemoMode
	@PostMapping("/api/member/cancel_follow")
	public R<?> cancelFollowMember(@RequestParam @LongId Long targetId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		if (memberId == targetId) {
			return R.ok();
		}
		this.memberFollowService.cancelFollow(memberId, targetId);
		return R.ok();
	}

	/**
	 * 获取关注用户列表
	 */
	@GetMapping("/api/member/follows")
	public R<?> getMemberFollows(@RequestParam(required = false, defaultValue = "20") Integer limit,
								 @RequestParam(required = false, defaultValue = "0") Long offset) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		List<Long> followMemberIds = memberFollowService.lambdaQuery()
				.eq(MemberFollow::getMemberId, memberId)
				.lt(IdUtils.validate(offset), MemberFollow::getLogId, offset)
				.orderByDesc(MemberFollow::getLogId)
				.page(new Page<>(1, limit, false))
				.getRecords().stream().map(MemberFollow::getFollowMemberId).toList();

		List<MemberCache> list = followMemberIds.stream().map(memberStatDataService::getMemberCache).toList();
		return this.bindDataTable(list);
	}

	/**
	 * 获取粉丝用户列表
	 */
	@GetMapping("/api/member/followers")
	public R<?> getMemberFollowers(@RequestParam(required = false, defaultValue = "20") Integer limit,
								   @RequestParam(required = false, defaultValue = "0") Long offset) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		List<Long> followerMemberIds = memberFollowService.lambdaQuery()
				.eq(MemberFollow::getFollowMemberId, memberId)
				.lt(IdUtils.validate(offset), MemberFollow::getLogId, offset)
				.orderByDesc(MemberFollow::getLogId)
				.page(new Page<>(1, limit, false))
				.getRecords().stream().map(MemberFollow::getMemberId).toList();

		List<MemberCache> list = followerMemberIds.stream().map(memberStatDataService::getMemberCache).toList();
		return this.bindDataTable(list);
	}
}