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
package com.chestnut.media.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsAudio;
import com.chestnut.media.service.IAudioService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class CmsAudioTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_audio";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IContentService contentService;

	private final IAudioService audioService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("contentid", true, TagAttrDataType.INTEGER, "音频集内容ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long contentId = MapUtils.getLongValue(attrs, "contentid", 0);
		if (contentId <= 0) {
			throw new TemplateException("音频集内容ID错误：" + contentId, env);
		}
		CmsContent c = this.contentService.dao().getById(contentId);
		if (ContentCopyType.isMapping(c.getCopyType())) {
			contentId = c.getCopyId();
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		LambdaQueryWrapper<CmsAudio> q = new LambdaQueryWrapper<CmsAudio>().eq(CmsAudio::getContentId, contentId);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(CmsAudio::getSortFlag);
		Page<CmsAudio> pageResult = this.audioService.dao().page(new Page<>(pageIndex, size, page), q);
		if (pageIndex > 1 & pageResult.getRecords().isEmpty()) {
			throw new TemplateException("内容列表页码超出上限：" + pageIndex, env);
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		pageResult.getRecords().forEach(audio -> {
			audio.setSrc(
					InternalUrlUtils.getActualUrl(audio.getPath(), context.getPublishPipeCode(), context.isPreview()));
		});
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
