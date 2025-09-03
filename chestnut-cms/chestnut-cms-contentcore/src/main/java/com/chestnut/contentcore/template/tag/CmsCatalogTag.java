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
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.vo.TagCatalogVO;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.template.exception.CatalogNotFoundException;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
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
public class CmsCatalogTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_catalog";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_ID = "{FREEMARKER.TAG." + TAG_NAME + ".id}";
	public final static String ATTR_USAGE_ALIAS = "{FREEMARKER.TAG." + TAG_NAME + ".alias}";
	public final static String ATTR_USAGE_LEVEL = "{FREEMARKER.TAG." + TAG_NAME + ".level}";
	public final static String ATTR_OPTION_LEVEL_ROOT = "{FREEMARKER.TAG." + TAG_NAME + ".level.Root}";
	public final static String ATTR_OPTION_LEVEL_CURRENT = "{FREEMARKER.TAG." + TAG_NAME + ".level.Current}";
	public final static String ATTR_OPTION_LEVEL_CHILD = "{FREEMARKER.TAG." + TAG_NAME + ".level.Child}";
	public final static String ATTR_OPTION_LEVEL_CURRENT_AND_CHILD = "{FREEMARKER.TAG." + TAG_NAME + ".level.CurrentAndChild}";
	public final static String ATTR_OPTION_LEVEL_SELF = "{FREEMARKER.TAG." + TAG_NAME + ".level.Self}";

	private static final String ATTR_ID = "id";
	private static final String ATTR_ALIAS = "alias";
	private static final String ATTR_LEVEL = "level";

	private final ICatalogService catalogService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_ID, false, TagAttrDataType.INTEGER, ATTR_USAGE_ID));
		tagAttrs.add(new TagAttr(ATTR_ALIAS, false, TagAttrDataType.STRING, ATTR_USAGE_ALIAS));
		tagAttrs.add(new TagAttr(ATTR_LEVEL, false, TagAttrDataType.STRING, ATTR_USAGE_LEVEL,
				CatalogTagLevel.toTagAttrOptions(), CatalogTagLevel.Current.name()));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long catalogId = MapUtils.getLongValue(attrs, ATTR_ID);
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);

		Long siteId = TemplateUtils.evalSiteId(env);
		String alias = MapUtils.getString(attrs, ATTR_ALIAS);
		if (Objects.isNull(catalog) && StringUtils.isNotEmpty(alias)) {
			catalog = this.catalogService.getCatalogByAlias(siteId, alias);
		}
		String level = MapUtils.getString(attrs, ATTR_LEVEL);
		if (!CatalogTagLevel.isRoot(level) && Objects.isNull(catalog)) {
			throw new CatalogNotFoundException(catalogId, alias, env);
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);

		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<>();
		q.eq(CmsCatalog::getSiteId, siteId).eq(CmsCatalog::getVisibleFlag, YesOrNo.YES).ne(CmsCatalog::getTagIgnore, YesOrNo.YES);
		if (CatalogTagLevel.isCurrent(level)) {
			q.eq(CmsCatalog::getParentId, catalog.getParentId());
		} else if (CatalogTagLevel.isChild(level)) {
			q.eq(CmsCatalog::getParentId, catalog.getCatalogId());
		} else if (CatalogTagLevel.isCurrentAndChild(level)) {
			q.likeRight(CmsCatalog::getAncestors, catalog.getAncestors());
		} else if (CatalogTagLevel.isSelf(level)) {
			q.eq(CmsCatalog::getCatalogId, catalog.getCatalogId());
		}
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(CmsCatalog::getSortFlag);

		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsCatalog> pageResult = this.catalogService.page(new Page<>(pageIndex, size, page), q);
		List<TagCatalogVO> dataList = pageResult.getRecords().stream().map(c -> {
			TagCatalogVO vo = TagCatalogVO.newInstance(c, context.getPublishPipeCode(), context.isPreview());
			vo.setLink(catalogService.getCatalogLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			if (StringUtils.isNotEmpty(c.getIndexTemplate(context.getPublishPipeCode()))
					&& StringUtils.isNotEmpty(c.getListTemplate(context.getPublishPipeCode()))) {
				vo.setListLink(catalogService.getCatalogListLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			}
			return vo;
		}).toList();
		return TagPageData.of(dataList, pageResult.getTotal());
	}

	@Override
	public Class<TagCatalogVO> getDataClass() {
		return TagCatalogVO.class;
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

	public enum CatalogTagLevel {
		Root(ATTR_OPTION_LEVEL_ROOT),
		Current(ATTR_OPTION_LEVEL_CURRENT),
		Child(ATTR_OPTION_LEVEL_CHILD),
		CurrentAndChild(ATTR_OPTION_LEVEL_CURRENT_AND_CHILD),
		Self(ATTR_OPTION_LEVEL_SELF);

		private final String desc;

		CatalogTagLevel(String desc) {
			this.desc = desc;
		}

		public static boolean isRoot(String level) {
			return Root.name().equalsIgnoreCase(level);
		}

		public static boolean isCurrent(String level) {
			return Current.name().equalsIgnoreCase(level);
		}

		public static boolean isChild(String level) {
			return Child.name().equalsIgnoreCase(level);
		}

		public static boolean isCurrentAndChild(String level) {
			return CurrentAndChild.name().equalsIgnoreCase(level);
		}

		public static boolean isSelf(String level) {
			return Self.name().equalsIgnoreCase(level);
		}

		public static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Root.name(), Root.desc),
					new TagAttrOption(Current.name(), Current.desc),
					new TagAttrOption(Child.name(), Child.desc),
					new TagAttrOption(CurrentAndChild.name(), CurrentAndChild.desc),
					new TagAttrOption(Self.name(), Self.desc)
			);
		}
	}
}
