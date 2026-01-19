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
package com.chestnut.cms.vote.template.tag;

import com.chestnut.cms.vote.exception.VoteNotFoundTemplateException;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.domain.vo.VoteSubjectVO;
import com.chestnut.vote.domain.vo.VoteVO;
import com.chestnut.vote.service.IVoteService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsVoteSubjectTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_vote_subject";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CONTENT_ID = "{FREEMARKER.TAG." + TAG_NAME + ".code}";

	public final static String ATTR_CODE = "code";

	private final IVoteService voteService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_CODE, true, TagAttrDataType.STRING, ATTR_USAGE_CONTENT_ID));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		String code = MapUtils.getString(attrs, ATTR_CODE);
		Vote vote = this.voteService.lambdaQuery().eq(Vote::getCode, code).one();
		Assert.notNull(vote, () -> new VoteNotFoundTemplateException(code, env));

		VoteVO vo = this.voteService.getVote(vote.getVoteId());
		List<VoteSubjectVO> subjects = vo.getSubjects();
		return TagPageData.of(subjects);
	}

	@Override
	public Class<VoteSubjectVO> getDataClass() {
		return VoteSubjectVO.class;
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
