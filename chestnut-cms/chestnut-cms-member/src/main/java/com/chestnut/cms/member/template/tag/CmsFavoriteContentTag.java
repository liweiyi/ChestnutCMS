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
import com.chestnut.cms.member.CmsMemberConstants;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.TagContentVO;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.member.domain.MemberFavorites;
import com.chestnut.member.service.IMemberFavoritesService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsFavoriteContentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_favorite_content";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_UID = "{FREEMARKER.TAG." + TAG_NAME + ".uid}";

	final static String ATTR_UID = "uid";

	private final IContentService contentService;

	private final IMemberFavoritesService memberFavoritesService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_UID, false, TagAttrDataType.INTEGER, ATTR_USAGE_UID));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long uid = MapUtils.getLongValue(attrs, ATTR_UID);

		Page<MemberFavorites> pageResult = this.memberFavoritesService.lambdaQuery().eq(MemberFavorites::getMemberId, uid)
				.eq(MemberFavorites::getDataType, CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE)
				.orderByDesc(MemberFavorites::getLogId)
				.page(new Page<>(pageIndex, size, page));
		if (pageResult.getRecords().isEmpty()) {
			return TagPageData.of(List.of(), pageResult.getTotal());
		}

		List<Long> contentIds = pageResult.getRecords().stream().map(MemberFavorites::getDataId).toList();
		List<CmsContent> contents = this.contentService.dao().listByIds(contentIds);

		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		List<TagContentVO> list = contents.stream().map(c -> {
			TagContentVO dto = TagContentVO.newInstance(c, context.getPublishPipeCode(), context.isPreview());
			dto.setLink(this.contentService.getContentLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			return dto;
		}).toList();
		return TagPageData.of(list, pageResult.getTotal());
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
