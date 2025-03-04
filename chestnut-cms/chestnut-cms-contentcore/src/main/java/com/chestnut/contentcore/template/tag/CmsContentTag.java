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
package com.chestnut.contentcore.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.staticize.tag.TagAttrOption;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CmsContentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CATALOG_ID = "{FREEMARKER.TAG." + TAG_NAME + ".catalogId}";
	public final static String ATTR_USAGE_CATALOG_ALIAS = "{FREEMARKER.TAG." + TAG_NAME + ".catalogAlias}";
	public final static String ATTR_USAGE_LEVEL = "{FREEMARKER.TAG." + TAG_NAME + ".level}";
	public final static String ATTR_OPTION_LEVEL_ROOT = "{FREEMARKER.TAG." + TAG_NAME + ".level.Root}";
	public final static String ATTR_OPTION_LEVEL_CURRENT = "{FREEMARKER.TAG." + TAG_NAME + ".level.Current}";
	public final static String ATTR_OPTION_LEVEL_CHILD = "{FREEMARKER.TAG." + TAG_NAME + ".level.Child}";
	public final static String ATTR_OPTION_LEVEL_CURRENT_AND_CHILD = "{FREEMARKER.TAG." + TAG_NAME + ".level.CurrentAndChild}";
	public final static String ATTR_USAGE_SORT = "{FREEMARKER.TAG." + TAG_NAME + ".sort}";
	public final static String ATTR_OPTION_SORT_RECENT = "{FREEMARKER.TAG." + TAG_NAME + ".sort.Recent}";
	public final static String ATTR_OPTION_SORT_VIEWS = "{FREEMARKER.TAG." + TAG_NAME + ".sort.Views}";
	public final static String ATTR_OPTION_SORT_DEFAULT = "{FREEMARKER.TAG." + TAG_NAME + ".sort.Default}";
	public final static String ATTR_USAGE_HAS_ATTRIBUTE = "{FREEMARKER.TAG." + TAG_NAME + ".hasattribute}";
	public final static String ATTR_USAGE_NO_ATTRIBUTE = "{FREEMARKER.TAG." + TAG_NAME + ".noattribute}";
	public final static String ATTR_USAGE_STATUS = "{FREEMARKER.TAG." + TAG_NAME + ".status}";
	public final static String ATTR_DEFAULT_NO_STATUS = "{FREEMARKER.TAG." + TAG_NAME + ".status.defaultValue}";
	public final static String ATTR_USAGE_TOP_FLAG = "{FREEMARKER.TAG." + TAG_NAME + ".topflag}";

	public final static String ATTR_CATALOG_ID = "catalogid";
	public final static String ATTR_CATALOG_ALIAS = "catalogalias";
	public final static String ATTR_LEVEL = "level";
	public final static String ATTR_SORT = "sort";
	public final static String ATTR_HAS_ATTRIBUTE = "hasattribute";
	public final static String ATTR_NO_ATTRIBUTE = "noattribute";
	public final static String ATTR_STATUS = "status";
	public final static String ATTR_TOP_FLAG = "topflag";

	private final IContentService contentService;

	private final ICatalogService catalogService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_CATALOG_ID, false, TagAttrDataType.INTEGER, ATTR_USAGE_CATALOG_ID));
		tagAttrs.add(new TagAttr(ATTR_CATALOG_ALIAS, false, TagAttrDataType.STRING, ATTR_USAGE_CATALOG_ALIAS));
		tagAttrs.add(new TagAttr(ATTR_LEVEL, false, TagAttrDataType.STRING, ATTR_USAGE_LEVEL, LevelTagAttr.toTagAttrOptions(), LevelTagAttr.Current.name()));
		tagAttrs.add(new TagAttr(ATTR_SORT, false, TagAttrDataType.STRING, ATTR_USAGE_SORT, SortTagAttr.toTagAttrOptions(), SortTagAttr.Default.name()));
		tagAttrs.add(new TagAttr(ATTR_HAS_ATTRIBUTE, false, TagAttrDataType.STRING, ATTR_USAGE_HAS_ATTRIBUTE));
		tagAttrs.add(new TagAttr(ATTR_NO_ATTRIBUTE, false, TagAttrDataType.STRING, ATTR_USAGE_NO_ATTRIBUTE));
		tagAttrs.add(new TagAttr(ATTR_STATUS, false, TagAttrDataType.STRING, ATTR_USAGE_STATUS, ATTR_DEFAULT_NO_STATUS));
		tagAttrs.add(new TagAttr(ATTR_TOP_FLAG, false, TagAttrDataType.BOOLEAN, ATTR_USAGE_TOP_FLAG, Boolean.TRUE.toString()));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
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
		if (!LevelTagAttr.isRoot(level) && Objects.isNull(catalog)) {
			throw new CatalogNotFoundException(getTagName(), catalogId, alias, env);
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		String status = MapUtils.getString(attrs, ATTR_STATUS, ContentStatus.PUBLISHED);

		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
		q.eq(CmsContent::getSiteId, siteId).eq(!"-1".equals(status), CmsContent::getStatus, ContentStatus.PUBLISHED);
		if (Objects.nonNull(catalog)) {
			if (LevelTagAttr.isCurrent(level)) {
				q.eq(CmsContent::getCatalogId, catalog.getCatalogId());
			} else if (LevelTagAttr.isChild(level)) {
				q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
			} else if (LevelTagAttr.isCurrentAndChild(level)) {
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
		if (SortTagAttr.isRecent(sortType)) {
			q.orderByDesc(CmsContent::getPublishDate);
		} else if(SortTagAttr.isViews(sortType)) {
			q.orderByDesc(CmsContent::getViewCount);
		} else {
			q.orderByDesc(CmsContent::getSortFlag);
		}

		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsContent> pageResult = this.contentService.dao().page(new Page<>(pageIndex, size, page), q);
		List<TagContentVO> list = new ArrayList<>();
		pageResult.getRecords().forEach(c -> {
			TagContentVO dto = TagContentVO.newInstance(c, context.getPublishPipeCode(), context.isPreview());
			dto.setLink(this.contentService.getContentLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			list.add(dto);
		});
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

	public enum LevelTagAttr {
		Root(ATTR_OPTION_LEVEL_ROOT),
		Current(ATTR_OPTION_LEVEL_CURRENT),
		Child(ATTR_OPTION_LEVEL_CHILD),
		CurrentAndChild(ATTR_OPTION_LEVEL_CURRENT_AND_CHILD);

		private final String desc;

		LevelTagAttr(String desc){
			this.desc = desc;
		}

		public static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Root.name(), Root.desc),
					new TagAttrOption(Current.name(), Current.desc),
					new TagAttrOption(Child.name(), Child.desc),
					new TagAttrOption(CurrentAndChild.name(), CurrentAndChild.desc)
			);
		}

		public static boolean isCurrent(String v) {
			return Current.name().equalsIgnoreCase(v);
		}

		public static boolean isCurrentAndChild(String v) {
			return CurrentAndChild.name().equalsIgnoreCase(v);
		}

		public static boolean isChild(String v) {
			return Child.name().equalsIgnoreCase(v);
		}

		public static boolean isRoot(String v) {
			return Root.name().equalsIgnoreCase(v);
		}
	}

	public enum SortTagAttr {
		Recent(ATTR_OPTION_SORT_RECENT),
		Views(ATTR_OPTION_SORT_VIEWS),
		Default(ATTR_OPTION_SORT_DEFAULT);

		private final String desc;

		SortTagAttr(String desc){
			this.desc = desc;
		}

		public static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Recent.name(), Recent.desc),
					new TagAttrOption(Views.name(), Views.desc),
					new TagAttrOption(Default.name(), Default.desc)
			);
		}

		public static boolean isRecent(String v) {
			return Recent.name().equalsIgnoreCase(v);
		}

		public static boolean isViews(String v) {
			return Views.name().equalsIgnoreCase(v);
		}
	}
}
