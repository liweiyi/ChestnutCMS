package com.chestnut.cms.vote.template.tag;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.domain.vo.VoteSubjectVO;
import com.chestnut.vote.domain.vo.VoteVO;
import com.chestnut.vote.service.IVoteService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsVoteSubjectTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_vote_subject";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IVoteService voteService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("code", true, TagAttrDataType.STRING, "问卷调查编码"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		String code = MapUtils.getString(attrs, "code");
		if (StringUtils.isEmpty(code)) {
			throw new TemplateException("属性code不能为空", env);
		}
		Vote vote = this.voteService.lambdaQuery().eq(Vote::getCode, code).one();
		if (vote == null) {
			throw new TemplateException("获取问卷调查数据失败：" + code, env);
		}
		
		VoteVO vo = this.voteService.getVote(vote.getVoteId());
		List<VoteSubjectVO> subjects = vo.getSubjects();
		return TagPageData.of(subjects);
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
