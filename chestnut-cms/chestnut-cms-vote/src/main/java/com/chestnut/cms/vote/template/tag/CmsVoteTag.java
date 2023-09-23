package com.chestnut.cms.vote.template.tag;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.utils.StringUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.vote.service.ICmsVoteService;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.service.IVoteService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsVoteTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_vote";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IVoteService voteService;

	private final ICmsVoteService cmsVoteService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("siteid", false, TagAttrDataType.STRING, "站点ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long siteId = MapUtils.getLongValue(attrs, "siteid", FreeMarkerUtils.evalLongVariable(env, "Site.siteId"));
		String voteSource = this.cmsVoteService.getVoteSource(siteId);
		LambdaQueryWrapper<Vote> q = new LambdaQueryWrapper<Vote>().eq(Vote::getSource, voteSource);

		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);

		Page<Vote> pageResult = this.voteService.page(new Page<>(pageIndex, size, page), q);
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
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
}
