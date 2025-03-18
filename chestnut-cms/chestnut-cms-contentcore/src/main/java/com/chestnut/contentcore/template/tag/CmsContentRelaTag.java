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
package com.chestnut.contentcore.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsContentRela;
import com.chestnut.contentcore.domain.vo.TagContentVO;
import com.chestnut.contentcore.mapper.CmsContentRelaMapper;
import com.chestnut.contentcore.service.IContentService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsContentRelaTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content_rela";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CONTENT_ID = "{FREEMARKER.TAG." + TAG_NAME + ".contentId}";

	public final static String ATTR_CONTENT_ID = "contentid";

	private final CmsContentRelaMapper contentRelaMapper;

	private final IContentService contentService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_CONTENT_ID, true, TagAttrDataType.INTEGER, ATTR_USAGE_CONTENT_ID));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long contentId = MapUtils.getLongValue(attrs, ATTR_CONTENT_ID);
		if (!IdUtils.validate(contentId)) {
			throw new TemplateException("Invalid content id: " + contentId, env);
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsContentRela> pageResult = contentRelaMapper.selectPage(new Page<>(pageIndex, size, page),
				new LambdaQueryWrapper<CmsContentRela>().eq(CmsContentRela::getContentId, contentId));
		if (!pageResult.getRecords().isEmpty()) {
			List<Long> contentIds = pageResult.getRecords().stream().map(CmsContentRela::getRelaContentId).toList();
			List<CmsContent> contents = this.contentService.dao().lambdaQuery().in(CmsContent::getContentId, contentIds).list();
			List<TagContentVO> result = contents.stream().map(c -> {
				TagContentVO dto = TagContentVO.newInstance(c, context.getPublishPipeCode(), context.isPreview());
				dto.setLink(this.contentService.getContentLink(c, 1,
						context.getPublishPipeCode(), context.isPreview()));
				return dto;
			}).toList();
			return TagPageData.of(result, page ? pageResult.getTotal() : result.size());
		} else {
			return TagPageData.of(List.of(), 0);
		}
	}

	@Override
	public Class<TagContentVO> getDataClass() {
		return TagContentVO.class;
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
