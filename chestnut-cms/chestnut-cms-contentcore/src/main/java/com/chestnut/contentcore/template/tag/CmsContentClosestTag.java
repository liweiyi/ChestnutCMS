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
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.TagContentVO;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
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
public class CmsContentClosestTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content_closest";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CONTENT_ID = "{FREEMARKER.TAG." + TAG_NAME + ".contentId}";
	public final static String ATTR_USAGE_TYPE = "{FREEMARKER.TAG." + TAG_NAME + ".type}";
	public final static String ATTR_USAGE_SORT = "{FREEMARKER.TAG." + TAG_NAME + ".sort}";
	public final static String ATTR_OPTION_TYPE_PREV = "{FREEMARKER.TAG." + TAG_NAME + ".type.Prev}";
	public final static String ATTR_OPTION_TYPE_NEXT = "{FREEMARKER.TAG." + TAG_NAME + ".type.Next}";

	final static String ATTR_CONTENT_ID = "contentid";

	final static String ATTR_SORT = "sort";

	final static String ATTR_TYPE = "type";

	private final IContentService contentService;

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		boolean isNext = TypeTagAttr.isNext(attrs.get(ATTR_TYPE));
		String sort = attrs.get(ATTR_SORT);
		Long contentId = MapUtils.getLong(attrs, ATTR_CONTENT_ID);

		CmsContent content = this.contentService.dao().getById(contentId);
		Assert.notNull(content, () -> new TemplateException(StringUtils.messageFormat("Tag attr[contentid={0}] data not found.", contentId), env));

		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getCatalogId, content.getCatalogId())
				.eq(CmsContent::getStatus, ContentStatus.PUBLISHED);
		if (CmsContentTag.SortTagAttr.isRecent(sort)) {
			q.gt(isNext, CmsContent::getPublishDate, content.getPublishDate());
			q.lt(!isNext, CmsContent::getPublishDate, content.getPublishDate());
			q.orderBy(true, isNext, CmsContent::getPublishDate);
		} else if(CmsContentTag.SortTagAttr.isViews(sort)) {
			q.gt(isNext, CmsContent::getViewCount, content.getViewCount());
			q.lt(!isNext, CmsContent::getViewCount, content.getViewCount());
			q.orderBy(true, isNext, CmsContent::getViewCount);
		} else {
			q.gt(isNext, CmsContent::getSortFlag, content.getSortFlag());
			q.lt(!isNext, CmsContent::getSortFlag, content.getSortFlag());
			q.orderBy(true, isNext, CmsContent::getSortFlag);
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsContent> pageResult = this.contentService.dao().page(new Page<>(1, 1, false), q);
		if (pageResult.getRecords().isEmpty()) {
			return TagPageData.of(List.of(), 0);
		}
		List<TagContentVO> dataList = pageResult.getRecords().stream().map(c -> {
			TagContentVO dto = TagContentVO.newInstance(c, context.getPublishPipeCode(), context.isPreview());
			dto.setLink(this.contentService.getContentLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			return dto;
		}).toList();
		return TagPageData.of(dataList, dataList.size());
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

	@Override
	public List<TagAttr> getTagAttrs() {
		return List.of(
				new TagAttr(ATTR_CONTENT_ID, true, TagAttrDataType.INTEGER, ATTR_USAGE_CONTENT_ID),
				new TagAttr(ATTR_TYPE, true, TagAttrDataType.STRING, ATTR_USAGE_TYPE, TypeTagAttr.toTagAttrOptions(), TypeTagAttr.Prev.name()),
				new TagAttr(ATTR_SORT, false, TagAttrDataType.STRING, ATTR_USAGE_SORT, CmsContentTag.SortTagAttr.toTagAttrOptions(), CmsContentTag.SortTagAttr.Default.name())
		);
	}

	enum TypeTagAttr {
		Prev(ATTR_OPTION_TYPE_PREV), Next(ATTR_OPTION_TYPE_NEXT);

		private final String desc;

		TypeTagAttr(String desc){
			this.desc = desc;
		}

		public static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Prev.name(), Prev.desc),
					new TagAttrOption(Next.name(), Next.desc)
			);
		}

		public static boolean isNext(String v) {
			return Next.name().equalsIgnoreCase(v);
		}
	}
}
