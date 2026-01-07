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

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.member.domain.MemberLike;
import com.chestnut.member.domain.dto.LikeRequest;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberLikeService;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 会员收藏数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = MemberUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member/like")
public class MemberLikeApiController extends BaseRestController {

	private final IMemberLikeService memberLikeService;

	/**
	 * 点赞内容
	 */
	@GetMapping("/check")
	public R<?> checkLike(@RequestParam @NotBlank @Length(max = 100) String dataType, @RequestParam @LongId Long dataId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		Long count = this.memberLikeService.lambdaQuery()
				.eq(MemberLike::getMemberId, memberId)
				.eq(MemberLike::getDataType, dataType)
				.eq(MemberLike::getDataId, dataId)
				.count();
		return R.ok(count > 0);
	}

	/**
	 * 点赞内容
	 */
	@IgnoreDemoMode
	@PostMapping
	public R<?> likeContent(@RequestBody @Validated LikeRequest req) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberLikeService.like(memberId, req.getDataType(), req.getDataId());
		return R.ok();
	}

	/**
	 * 取消收藏
	 */
	@IgnoreDemoMode
	@PostMapping("/cancel")
	public R<?> cancelLike(@RequestBody @Validated LikeRequest req) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberLikeService.cancelLike(memberId, req.getDataType(), req.getDataId());
		return R.ok();
	}

	@IgnoreDemoMode
    @DeleteMapping
	@Deprecated(since = "1.5.7", forRemoval = true)
	public R<?> cancelFavorite2(@RequestBody @Validated LikeRequest req) {
		return cancelLike(req);
	}
}
