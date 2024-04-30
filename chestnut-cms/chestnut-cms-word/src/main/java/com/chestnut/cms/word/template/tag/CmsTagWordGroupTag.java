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
package com.chestnut.cms.word.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.word.domain.TagWordGroup;
import com.chestnut.word.service.ITagWordGroupService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CmsTagWordGroupTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_tag_word_group";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private static final String TAG_ATTR_CODE = "code";

	private static final String TAG_ATTR_LEVEL = "level";

	private final ITagWordGroupService tagWordGroupService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(TAG_ATTR_CODE, true, TagAttrDataType.STRING, "TAG词分组编码") );
		tagAttrs.add(new TagAttr(TAG_ATTR_LEVEL, false, TagAttrDataType.STRING, "数据获取范围，值为`Root`时忽略属性code",
				TagWordGroupTagLevel.toTagAttrOptions(), TagWordGroupTagLevel.Current.name()));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		String group = MapUtils.getString(attrs, TAG_ATTR_CODE);
		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		Optional<TagWordGroup> opt = tagWordGroupService.lambdaQuery()
				.eq(TagWordGroup::getOwner, siteId)
				.eq(TagWordGroup::getCode, group).oneOpt();
		if (opt.isEmpty()) {
			throw new TemplateException("Tag word group not found: " + group, env);
		}
		String level = MapUtils.getString(attrs, TAG_ATTR_LEVEL);

		TagWordGroup parent = opt.get();
		LambdaQueryWrapper<TagWordGroup> q = new LambdaQueryWrapper<>();
		if (TagWordGroupTagLevel.isCurrent(level)) {
			q.eq(TagWordGroup::getParentId, parent.getParentId());
		} else if (TagWordGroupTagLevel.isChild(level)) {
			q.eq(TagWordGroup::getParentId, parent.getGroupId());
		}

		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		q.apply(StringUtils.isNotEmpty(condition), condition);
		q.orderByAsc(TagWordGroup::getSortFlag);

		 Page<TagWordGroup> pageResult = this.tagWordGroupService.page(new Page<>(pageIndex, size, page), q);
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

	private enum TagWordGroupTagLevel {
		Root("所有分组"), Current("同级分组"), Child("子分组");

		private final String desc;

		TagWordGroupTagLevel(String desc) {
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
