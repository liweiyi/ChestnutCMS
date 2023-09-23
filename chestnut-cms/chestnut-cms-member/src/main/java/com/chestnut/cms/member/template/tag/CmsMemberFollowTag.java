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

	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IMemberStatDataService memberStatDataService;

	private final IMemberFollowService memberFollowService;

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long uid = MapUtils.getLongValue(attrs, "uid");
		String type = attrs.get("type");

		Page<MemberFollow> pageResult = this.memberFollowService.lambdaQuery()
				.eq(MemberFollowTagType.isFollow(type), MemberFollow::getMemberId, uid)
				.eq(MemberFollowTagType.isFollower(type), MemberFollow::getFollowMemberId, uid)
				.orderByDesc(MemberFollow::getLogId)
				.page(new Page<>(pageIndex, size, page));
		if (pageResult.getRecords().isEmpty()) {
			return TagPageData.of(List.of(), pageResult.getTotal());
		}
		List<MemberCache> list = pageResult.getRecords().stream().map(mf -> {
			if ("follow".equalsIgnoreCase(type)) {
				return this.memberStatDataService.getMemberCache(mf.getFollowMemberId());
			} else {
				return this.memberStatDataService.getMemberCache(mf.getMemberId());
			}
		}).toList();
		return TagPageData.of(list, pageResult.getTotal());
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
		tagAttrs.add(new TagAttr("uid", false, TagAttrDataType.INTEGER, "用户ID"));
		tagAttrs.add(new TagAttr("type", true, TagAttrDataType.STRING, "类型", MemberFollowTagType.toTagAttrOptions(), null));
		return tagAttrs;
	}

	private enum MemberFollowTagType {
		// 所有站点
		follow("关注"),
		// 当前站点
		follower("粉丝");

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
