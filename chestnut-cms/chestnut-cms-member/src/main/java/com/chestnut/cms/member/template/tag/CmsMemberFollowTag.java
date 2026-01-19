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
package com.chestnut.cms.member.template.tag;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.member.domain.MemberFollow;
import com.chestnut.member.domain.vo.MemberCache;
import com.chestnut.member.service.IMemberFollowService;
import com.chestnut.member.service.IMemberStatDataService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsMemberFollowTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_member_follow";

	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_UID = "{FREEMARKER.TAG." + TAG_NAME + ".uid}";
	public final static String ATTR_USAGE_TYPE = "{FREEMARKER.TAG." + TAG_NAME + ".type}";
	public final static String ATTR_OPTION_TYPE_FOLLOW = "{FREEMARKER.TAG." + TAG_NAME + ".type.follow}";
	public final static String ATTR_OPTION_TYPE_FOLLOWER = "{FREEMARKER.TAG." + TAG_NAME + ".type.follower}";

	public final static String ATTR_UID = "uid";
	public final static String ATTR_TYPE = "type";

	private final IMemberStatDataService memberStatDataService;

	private final IMemberFollowService memberFollowService;

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long uid = MapUtils.getLongValue(attrs, ATTR_UID);
		String type = attrs.get(ATTR_TYPE);

		Page<MemberFollow> pageResult = this.memberFollowService.lambdaQuery()
				.eq(MemberFollowTagType.isFollow(type), MemberFollow::getMemberId, uid)
				.eq(MemberFollowTagType.isFollower(type), MemberFollow::getFollowMemberId, uid)
				.orderByDesc(MemberFollow::getLogId)
				.page(new Page<>(pageIndex, size, page));
		if (pageResult.getRecords().isEmpty()) {
			return TagPageData.of(List.of(), pageResult.getTotal());
		}
		List<MemberCache> list = pageResult.getRecords().stream().map(mf -> {
			if (MemberFollowTagType.isFollow(type)) {
				return this.memberStatDataService.getMemberCache(mf.getFollowMemberId());
			} else {
				return this.memberStatDataService.getMemberCache(mf.getMemberId());
			}
		}).toList();
		return TagPageData.of(list, pageResult.getTotal());
	}

	@Override
	public Class<MemberCache> getDataClass() {
		return MemberCache.class;
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESC;
	}


	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_UID, false, TagAttrDataType.INTEGER, ATTR_USAGE_UID));
		tagAttrs.add(new TagAttr(ATTR_TYPE, true, TagAttrDataType.STRING, ATTR_USAGE_TYPE, MemberFollowTagType.toTagAttrOptions()));
		return tagAttrs;
	}

	private enum MemberFollowTagType {
		follow(ATTR_OPTION_TYPE_FOLLOW),
		follower(ATTR_OPTION_TYPE_FOLLOWER);

		private final String desc;

		MemberFollowTagType(String desc) {
			this.desc = desc;
		}

		public static boolean isFollow(String v) {
			return follow.name().equalsIgnoreCase(v);
		}

		public static boolean isFollower(String v) {
			return follower.name().equalsIgnoreCase(v);
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(follow.name(), follow.desc),
					new TagAttrOption(follower.name(), follower.desc)
			);
		}
	}
}
