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
package com.chestnut.media.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.exception.InvalidTagAttrValueException;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.media.domain.CmsVideo;
import com.chestnut.media.domain.vo.TagVideoVO;
import com.chestnut.media.service.IVideoService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class CmsVideoTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_video";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CONTENT_ID = "{FREEMARKER.TAG." + TAG_NAME + ".contentId}";

	public final static String ATTR_CONTENT_ID = "contentId";

	private final IContentService contentService;

	private final IVideoService videoService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_CONTENT_ID, true, TagAttrDataType.INTEGER, ATTR_USAGE_CONTENT_ID));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long contentId = MapUtils.getLongValue(attrs, ATTR_CONTENT_ID, 0);
		if (!IdUtils.validate(contentId)) {
			throw new InvalidTagAttrValueException(getTagName(), ATTR_CONTENT_ID, String.valueOf(contentId), env);
		}
		CmsContent c = this.contentService.dao().getById(contentId);
		if (ContentCopyType.isMapping(c.getCopyType())) {
			contentId = c.getCopyId();
		}

		LambdaQueryWrapper<CmsVideo> q = new LambdaQueryWrapper<CmsVideo>().eq(CmsVideo::getContentId, contentId);
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(CmsVideo::getSortFlag);

		Page<CmsVideo> pageResult = this.videoService.dao().page(new Page<>(pageIndex, size, page), q);
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		List<TagVideoVO> dataList = pageResult.getRecords().stream().map(video -> {
			TagVideoVO vo = TagVideoVO.newInstance(video);
			video.setSrc(InternalUrlUtils.getActualUrl(video.getPath(), context.getPublishPipeCode(), context.isPreview()));
			return vo;
		}).toList();
		return TagPageData.of(dataList, pageResult.getTotal());
	}

	@Override
	public Class<TagVideoVO> getDataClass() {
		return TagVideoVO.class;
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
