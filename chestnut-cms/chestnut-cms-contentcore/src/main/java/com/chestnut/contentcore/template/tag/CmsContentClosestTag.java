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
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.TagContentVO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.template.exception.CatalogNotFoundException;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CmsContentClosestTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content_closest";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CONTENT_ID = "{FREEMARKER.TAG." + TAG_NAME + ".contentId}";
	public final static String ATTR_USAGE_TYPE = "{FREEMARKER.TAG." + TAG_NAME + ".type}";
	public final static String ATTR_OPTION_TYPE_PREV = "{FREEMARKER.TAG." + TAG_NAME + ".type.Prev}";
	public final static String ATTR_OPTION_TYPE_NEXT = "{FREEMARKER.TAG." + TAG_NAME + ".type.Next}";

	final static String ATTR_CONTENT_ID = "contentid";
	final static String ATTR_TYPE = "type";
	public final static String ATTR_CATALOG_ID = "catalogid";
	public final static String ATTR_CATALOG_ALIAS = "catalogalias";
	public final static String ATTR_LEVEL = "level";
	public final static String ATTR_SORT = "sort";
	public final static String ATTR_HAS_ATTRIBUTE = "hasattribute";
	public final static String ATTR_NO_ATTRIBUTE = "noattribute";
	public final static String ATTR_STATUS = "status";
	public final static String ATTR_TOP_FLAG = "topflag";

	private final ICatalogService catalogService;

	private final IContentService contentService;

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		boolean isNext = TypeTagAttr.isNext(attrs.get(ATTR_TYPE));
		Long contentId = MapUtils.getLong(attrs, ATTR_CONTENT_ID);

		CmsContent content = this.contentService.dao().getById(contentId);
		Assert.notNull(content, () -> new TemplateException(StringUtils.messageFormat("Tag attr[contentid={0}] data not found.", contentId), env));

		CmsCatalog catalog = null;
		long catalogId = MapUtils.getLongValue(attrs, ATTR_CATALOG_ID);
		if (catalogId > 0) {
			catalog = this.catalogService.getCatalog(catalogId);
		}
		long siteId = TemplateUtils.evalSiteId(env);
		String alias = MapUtils.getString(attrs, ATTR_CATALOG_ALIAS);
		if (Objects.isNull(catalog) && StringUtils.isNotEmpty(alias)) {
			catalog = this.catalogService.getCatalogByAlias(siteId, alias);
		}
		String level = MapUtils.getString(attrs, ATTR_LEVEL);
		if (!CmsContentTag.LevelTagAttr.isRoot(level) && Objects.isNull(catalog)) {
            catalog = this.catalogService.getCatalog(content.getCatalogId());
            if (Objects.isNull(catalog)) {
                throw new CatalogNotFoundException(catalogId, alias, env);
            }
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		String status = MapUtils.getString(attrs, ATTR_STATUS, ContentStatus.PUBLISHED);

		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
		q.eq(CmsContent::getSiteId, siteId).eq(!"-1".equals(status), CmsContent::getStatus, status);
		if (Objects.nonNull(catalog)) {
			if (CmsContentTag.LevelTagAttr.isCurrent(level)) {
				q.eq(CmsContent::getCatalogId, catalog.getCatalogId());
			} else if (CmsContentTag.LevelTagAttr.isChild(level)) {
				q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
			} else if (CmsContentTag.LevelTagAttr.isCurrentAndChild(level)) {
				q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors());
			}
		}
		String hasAttribute = MapUtils.getString(attrs, ATTR_HAS_ATTRIBUTE);
		if (StringUtils.isNotEmpty(hasAttribute)) {
			int attrTotal = ContentAttribute.convertInt(hasAttribute.split(","));
			q.apply(attrTotal > 0, "attributes&{0}={1}", attrTotal, attrTotal);
		}
		String noAttribute = MapUtils.getString(attrs, ATTR_NO_ATTRIBUTE);
		if (StringUtils.isNotEmpty(noAttribute)) {
			String[] contentAttrs = noAttribute.split(",");
			int attrTotal = ContentAttribute.convertInt(contentAttrs);
			for (String attr : contentAttrs) {
				int bit = ContentAttribute.bit(attr);
				q.apply(bit > 0, "attributes&{0}<>{1}", attrTotal, bit);
			}
		}
		q.apply(StringUtils.isNotEmpty(condition), condition);
		String sortType = MapUtils.getString(attrs, ATTR_SORT);
		q.orderByDesc(MapUtils.getBooleanValue(attrs, ATTR_TOP_FLAG, true), CmsContent::getTopFlag);
		if (CmsContentTag.SortTagAttr.isRecent(sortType)) {
			q.gt(isNext, CmsContent::getPublishDate, content.getPublishDate());
			q.lt(!isNext, CmsContent::getPublishDate, content.getPublishDate());
			q.orderBy(true, isNext, CmsContent::getPublishDate);
		} else if(CmsContentTag.SortTagAttr.isViews(sortType)) {
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
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_CONTENT_ID, true, TagAttrDataType.INTEGER, ATTR_USAGE_CONTENT_ID));
		tagAttrs.add(new TagAttr(ATTR_TYPE, true, TagAttrDataType.STRING, ATTR_USAGE_TYPE, TypeTagAttr.toTagAttrOptions(), TypeTagAttr.Prev.name()));
		tagAttrs.add(new TagAttr(ATTR_CATALOG_ID, false, TagAttrDataType.INTEGER, CmsContentTag.ATTR_USAGE_CATALOG_ID));
		tagAttrs.add(new TagAttr(ATTR_CATALOG_ALIAS, false, TagAttrDataType.STRING, CmsContentTag.ATTR_USAGE_CATALOG_ALIAS));
		tagAttrs.add(new TagAttr(ATTR_LEVEL, false, TagAttrDataType.STRING, CmsContentTag.ATTR_USAGE_LEVEL, CmsContentTag.LevelTagAttr.toTagAttrOptions(), CmsContentTag.LevelTagAttr.Current.name()));
		tagAttrs.add(new TagAttr(ATTR_SORT, false, TagAttrDataType.STRING, CmsContentTag.ATTR_USAGE_SORT, CmsContentTag.SortTagAttr.toTagAttrOptions(), CmsContentTag.SortTagAttr.Default.name()));
		tagAttrs.add(new TagAttr(ATTR_HAS_ATTRIBUTE, false, TagAttrDataType.STRING, CmsContentTag.ATTR_USAGE_HAS_ATTRIBUTE));
		tagAttrs.add(new TagAttr(ATTR_NO_ATTRIBUTE, false, TagAttrDataType.STRING, CmsContentTag.ATTR_USAGE_NO_ATTRIBUTE));
		tagAttrs.add(new TagAttr(ATTR_STATUS, false, TagAttrDataType.STRING, CmsContentTag.ATTR_USAGE_STATUS));
		tagAttrs.add(new TagAttr(ATTR_TOP_FLAG, false, TagAttrDataType.BOOLEAN, CmsContentTag.ATTR_USAGE_TOP_FLAG, Boolean.TRUE.toString()));
		return tagAttrs;
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
