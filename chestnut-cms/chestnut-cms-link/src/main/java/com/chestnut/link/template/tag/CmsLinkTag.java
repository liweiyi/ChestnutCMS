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
package com.chestnut.link.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.link.domain.CmsLink;
import com.chestnut.link.domain.CmsLinkGroup;
import com.chestnut.link.exception.LinkGroupNotFoundTemplateException;
import com.chestnut.link.service.ILinkGroupService;
import com.chestnut.link.service.ILinkService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsLinkTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_link";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CODE = "{FREEMARKER.TAG." + TAG_NAME + ".code}";

	private static final String ATTR_CODE = "code";

	private final ILinkGroupService linkGroupService;
	
	private final ILinkService linkService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_CODE, true, TagAttrDataType.STRING, ATTR_USAGE_CODE) );
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		String code = MapUtils.getString(attrs, ATTR_CODE);
		Long siteId = TemplateUtils.evalSiteId(env);
		CmsLinkGroup group = this.linkGroupService.getOne(new LambdaQueryWrapper<CmsLinkGroup>()
				.eq(CmsLinkGroup::getSiteId, siteId)
				.eq(CmsLinkGroup::getCode, code));
		Assert.notNull(group, () -> new LinkGroupNotFoundTemplateException(code, env));
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		LambdaQueryWrapper<CmsLink> q = new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getGroupId, group.getLinkGroupId());
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(CmsLink::getSortFlag);

		Page<CmsLink> pageResult = this.linkService.page(new Page<>(pageIndex, size, page), q);
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
	}

	@Override
	public Class<CmsLink> getDataClass() {
		return CmsLink.class;
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
