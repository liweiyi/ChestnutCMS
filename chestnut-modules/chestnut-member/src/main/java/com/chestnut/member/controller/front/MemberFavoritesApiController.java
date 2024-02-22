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
package com.chestnut.member.controller.front;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.member.domain.MemberFavorites;
import com.chestnut.member.domain.dto.FavoriteDTO;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.member.service.IMemberFavoritesService;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/member/favorites")
public class MemberFavoritesApiController extends BaseRestController {

	private final IMemberFavoritesService memberFavoritesService;

	/**
	 * 是否收藏过指定内容
	 */
	@IgnoreDemoMode
	@GetMapping("/check")
	public R<?> isFavorited(@RequestParam @NotEmpty String dataType, @RequestParam @LongId Long dataId) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		Long count = this.memberFavoritesService.lambdaQuery()
				.eq(MemberFavorites::getMemberId, memberId)
				.eq(MemberFavorites::getDataType, dataType)
				.eq(MemberFavorites::getDataId, dataId)
				.count();
		return R.ok(count > 0);
	}

	/**
	 * 收藏内容
	 */
	@IgnoreDemoMode
	@PostMapping
	public R<?> favorite(@RequestBody @Validated FavoriteDTO dto) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFavoritesService.favorite(memberId, dto.getDataType(), dto.getDataId());
		return R.ok();
	}

	/**
	 * 取消收藏
	 */
	@IgnoreDemoMode
	@DeleteMapping
	public R<?> cancelFavorite(@RequestBody @Validated FavoriteDTO dto) {
		long memberId = StpMemberUtil.getLoginIdAsLong();
		this.memberFavoritesService.cancelFavorite(memberId, dto.getDataType(), dto.getDataId());
		return R.ok();
	}
}
