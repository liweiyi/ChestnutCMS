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
package com.chestnut.cms.word.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.word.cache.TagWordMonitoredCache;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.service.ITagWordGroupService;
import com.chestnut.word.service.ITagWordService;
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
public class CmsTagWordTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_tag_word";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CODE = "{FREEMARKER.TAG." + TAG_NAME + ".code}";

	private static final String ATTR_GROUP = "group";

	private final ITagWordGroupService tagWordGroupService;

	private final ITagWordService tagWordService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_GROUP, true, TagAttrDataType.STRING, ATTR_USAGE_CODE));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		String group = MapUtils.getString(attrs, "group");
		Long siteId = TemplateUtils.evalSiteId(env);
        TagWordGroup tagWordGroup = tagWordGroupService.getTagWordGroup(siteId.toString(), group);
		if (Objects.isNull(tagWordGroup)) {
			throw new TemplateException("Tag word group not found: " + group, env);
		}
		LambdaQueryWrapper<TagWord> q = new LambdaQueryWrapper<TagWord>()
				.eq(TagWord::getGroupId, tagWordGroup.getGroupId());

		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(TagWord::getSortFlag);

        List<TagWordMonitoredCache.TagWordCache> tagWords = this.tagWordService.getTagWords(tagWordGroup.getOwner(), tagWordGroup.getCode());
		return TagPageData.of(tagWords, tagWords.size());
	}

	@Override
	public Class<TagWordMonitoredCache.TagWordCache> getDataClass() {
		return TagWordMonitoredCache.TagWordCache.class;
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
