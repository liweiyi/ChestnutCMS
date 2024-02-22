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

import com.chestnut.cms.member.domain.vo.ContentDynamicDataWithContributorVO;
import com.chestnut.cms.member.domain.vo.ContributorVO;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.vo.ContentDynamicDataVO;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.fixed.config.MemberResourcePrefix;
import com.chestnut.member.service.IMemberStatDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/api/cms/contentex")
public class ContentExApiController extends BaseRestController {

	private final IMemberStatDataService memberStatDataService;

	private final ContentDynamicDataService contentDynamicDataService;

	/**
	 * 内容动态数据扩展，评论数、点赞数、收藏数、浏览数 + 作者信息
	 *
	 * @param contentIdsStr
	 * @return
	 */
	@GetMapping("/data")
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
				if (StringUtils.isNotEmpty(contributor.getAvatar())) {
					contributor.setAvatarSrc(MemberResourcePrefix.getValue() + memberCache.getAvatar());
				}
				contributor.setSlogan(memberCache.getSlogan());
				contributor.setStat(memberCache.getStat());
				vo.setContributor(contributor);
			}
			return vo;
		}).toList();
		return R.ok(values);
	}
}
