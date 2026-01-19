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
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.vo.TagSiteVO;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.template.exception.SiteNotFoundException;
import com.chestnut.contentcore.util.SiteUtils;
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
public class CmsSiteTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_site";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_ID = "{FREEMARKER.TAG." + TAG_NAME + ".id}";
	public final static String ATTR_USAGE_LEVEL = "{FREEMARKER.TAG." + TAG_NAME + ".level}";
	public final static String ATTR_OPTION_LEVEL_ROOT = "{FREEMARKER.TAG." + TAG_NAME + ".level.Root}";
	public final static String ATTR_OPTION_LEVEL_CURRENT = "{FREEMARKER.TAG." + TAG_NAME + ".level.Current}";
	public final static String ATTR_OPTION_LEVEL_CHILD = "{FREEMARKER.TAG." + TAG_NAME + ".level.Child}";

	public final static String ATTR_ID = "id";

	public final static String ATTR_LEVEL = "level";

	private final ISiteService siteService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_ID, false, TagAttrDataType.INTEGER, ATTR_USAGE_ID));
		tagAttrs.add(new TagAttr(ATTR_LEVEL, false, TagAttrDataType.STRING, ATTR_USAGE_LEVEL,
				SiteTagLevel.toTagAttrOptions(), SiteTagLevel.Current.name()));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		Long siteId = MapUtils.getLong(attrs, ATTR_ID);
		String level = MapUtils.getString(attrs, ATTR_LEVEL);

		CmsSite site = this.siteService.getSite(siteId);
		if (!SiteTagLevel.isRoot(level) && Objects.isNull(site)) {
			throw new SiteNotFoundException(getTagName(), siteId, env);
		}

		LambdaQueryWrapper<CmsSite> q = new LambdaQueryWrapper<>();
		if (SiteTagLevel.isCurrent(level)) {
			q.eq(CmsSite::getParentId, site.getParentId());
		} else if (SiteTagLevel.isChild(level)) {
			q.eq(CmsSite::getParentId, site.getSiteId());
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(CmsSite::getSortFlag);

		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsSite> pageResult = this.siteService.page(new Page<>(pageIndex, size, page), q);
		List<TagSiteVO> dataList = pageResult.getRecords().stream().map(s -> {
			TagSiteVO vo = TagSiteVO.newInstance(s, context.getPublishPipeCode(), context.isPreview());
			vo.setLink(SiteUtils.getSiteLink(s, context.getPublishPipeCode(), context.isPreview()));
			return vo;
		}).toList();
		return TagPageData.of(dataList, pageResult.getTotal());
	}

	@Override
	public Class<TagSiteVO> getDataClass() {
		return TagSiteVO.class;
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

	private enum SiteTagLevel {
		// 所有站点
		Root(ATTR_OPTION_LEVEL_ROOT),
		// 同级站点
		Current(ATTR_OPTION_LEVEL_CURRENT),
		// 子站点
		Child(ATTR_OPTION_LEVEL_CHILD);

		private final String desc;

		SiteTagLevel(String desc) {
			this.desc = desc;
		}

		static boolean isRoot(String level) {
			return Root.name().equalsIgnoreCase(level);
		}

		static boolean isCurrent(String level) {
			return Current.name().equalsIgnoreCase(level);
		}

		static boolean isChild(String level) {
			return Child.name().equalsIgnoreCase(level);
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Root.name(), Root.desc),
					new TagAttrOption(Current.name(), Current.desc),
					new TagAttrOption(Child.name(), Child.desc)
			);
		}
	}
}
