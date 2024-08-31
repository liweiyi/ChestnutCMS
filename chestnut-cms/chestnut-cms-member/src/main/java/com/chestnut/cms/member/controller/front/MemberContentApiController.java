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
package com.chestnut.cms.member.controller.front;

import com.chestnut.cms.member.CmsMemberConstants;
import com.chestnut.cms.member.domain.vo.ContentDynamicDataWithContributorVO;
import com.chestnut.cms.member.domain.vo.ContributorVO;
import com.chestnut.cms.member.domain.vo.FavoriteContentVO;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentDynamicDataVO;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import com.chestnut.member.domain.MemberFavorites;
import com.chestnut.member.domain.MemberLike;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.service.IMemberFavoritesService;
import com.chestnut.member.service.IMemberLikeService;
import com.chestnut.member.service.IMemberStatDataService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

/**
 * 会员收藏内容API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cms/member")
public class MemberContentApiController extends BaseRestController {

	private final IMemberFavoritesService memberFavoritesService;

	private final IMemberLikeService memberLikeService;

	private final IContentService contentService;

	private final IMemberStatDataService memberStatDataService;

	private final ContentDynamicDataService contentDynamicDataService;

	/**
	 * 内容动态数据，评论数、点赞数、收藏数、浏览数
	 */
	@GetMapping("/content/data")
	public R<List<ContentDynamicDataWithContributorVO>> getContentDynamicData(@RequestParam("ids") String contentIdsStr) {
		if (StringUtils.isEmpty(contentIdsStr)) {
			return R.ok(List.of());
		}
		List<String> contentIds = Stream.of(contentIdsStr.split(",")).toList();
		if (contentIds.isEmpty()) {
			return R.ok(List.of());
		}
		List<ContentDynamicDataVO> list = this.contentDynamicDataService.getContentDynamicDataList(contentIds);
		List<ContentDynamicDataWithContributorVO> values = list.stream().map(data -> {
			ContentDynamicDataWithContributorVO vo = new ContentDynamicDataWithContributorVO(data);
			if (IdUtils.validate(vo.getContributorId())) {
				MemberCache memberCache = this.memberStatDataService.getMemberCache(vo.getContributorId());
				ContributorVO contributor = new ContributorVO();
				contributor.setUid(memberCache.getMemberId());
				contributor.setDisplayName(memberCache.getDisplayName());
				contributor.setAvatar(memberCache.getAvatar());
				contributor.setSlogan(memberCache.getSlogan());
				contributor.setStat(memberCache.getStat());
				vo.setContributor(contributor);
			}
			return vo;
		}).toList();
		return R.ok(values);
	}

	/**
	 * 内容作者信息
	 */
	@GetMapping("/{uid}/data")
	public R<?> getMemberData(
			@PathVariable("uid") Long memberId) {
		MemberCache memberCache = this.memberStatDataService.getMemberCache(memberId);


		return R.ok(memberCache);
	}

	/**
	 * 收藏内容列表
	 */
	@GetMapping("/{uid}/favorites")
	public R<?> getMemberFavorites(
			@PathVariable("uid") Long memberId,
			@RequestParam(required = false, defaultValue = "16") @Min(1) Integer limit,
			@RequestParam(required = false, defaultValue = "0") @Min(0) Long offset) {
		List<MemberFavorites> memberFavorites = this.memberFavoritesService
				.getMemberFavorites(memberId, CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE, limit, offset);
		List<Long> contentIds = memberFavorites.stream().map(MemberFavorites::getDataId).toList();
		List<FavoriteContentVO> contents = this.contentService.dao().lambdaQuery().in(CmsContent::getContentId, contentIds)
				.list().stream().map(FavoriteContentVO::newInstance).toList();
		return this.bindDataTable(contents);
	}

	/**
	 * 点赞内容列表
	 */
	@GetMapping("/{uid}/likes")
	public R<?> getMemberLikes(
			@PathVariable("uid") Long memberId,
			@RequestParam(required = false, defaultValue = "16") @Min(1) Integer limit,
			@RequestParam(required = false, defaultValue = "0") @Min(0) Long offset) {
		List<MemberLike> memberLikes = this.memberLikeService
				.getMemberLikes(memberId, CmsMemberConstants.MEMBER_LIKE_DATA_TYPE, limit, offset);
		List<Long> contentIds = memberLikes.stream().map(MemberLike::getDataId).toList();
		List<FavoriteContentVO> contents = this.contentService.dao().lambdaQuery().in(CmsContent::getContentId, contentIds)
				.list().stream().map(FavoriteContentVO::newInstance).toList();
		return this.bindDataTable(contents);
	}
}
