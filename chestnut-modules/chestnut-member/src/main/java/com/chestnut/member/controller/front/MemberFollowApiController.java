package com.chestnut.member.controller.front;

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
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Priv(type = MemberUserType.TYPE)
@RequiredArgsConstructor
@RestController
public class MemberFollowApiController extends BaseRestController {

	private final IMemberFollowService memberFollowService;

	private final IMemberStatDataService memberStatDataService;

	@GetMapping("/api/member/check_follow")
	public R<?> checkFollowMember(@RequestParam @NotEmpty String targetIds) {
		String[] memberIds = StringUtils.split(targetIds, ",");
		long memberId = StpMemberUtil.getLoginIdAsLong();
		List<MemberFollow> list = this.memberFollowService.lambdaQuery().eq(MemberFollow::getMemberId, memberId)
				.in(MemberFollow::getFollowMemberId, List.of(memberIds)).list();
		Map<String, Boolean> map = new HashMap<>(list.size());
		list.forEach(mf -> {
			map.put(mf.getFollowMemberId().toString(), true);
		});
		return R.ok(map);
	}

	@IgnoreDemoMode
	@PostMapping("/api/member/follow")
	public R<?> followMember(@RequestParam Long targetId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFollowService.follow(memberId, targetId);
		return R.ok();
	}

	@IgnoreDemoMode
	@PostMapping("/api/member/cancel_follow")
	public R<?> cancelFollowMember(@RequestParam Long targetId) {
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
				.last("limit " + limit)
				.list().stream().map(MemberFollow::getFollowMemberId).toList();

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
				.last("limit " + limit)
				.list().stream().map(MemberFollow::getMemberId).toList();

		List<MemberCache> list = followerMemberIds.stream().map(memberStatDataService::getMemberCache).toList();
		return this.bindDataTable(list);
	}
}