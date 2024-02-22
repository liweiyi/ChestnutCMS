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
package com.chestnut.common.staticize.tag.impl;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.StaticizeConstants;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PageBarTag extends AbstractTag {

	public final static String TAG_NAME = "page_bar";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

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

	final static String TagAttr_Type = "type";
	final static String TagAttr_Target = "target";
	final static String TagAttr_First = "first";
	final static String TagAttr_Last = "last";

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_Type, false, TagAttrDataType.STRING, "类型",
				PageBarType.toTagAttrOptions(), PageBarType.Simple.name()));
		tagAttrs.add(new TagAttr(TagAttr_Target, false, TagAttrDataType.STRING, "链接打开方式",
				LinkTarget.toTagAttrOptions(), LinkTarget._self.name()));
		tagAttrs.add(new TagAttr(TagAttr_First, false, TagAttrDataType.STRING, "首页链接名称", "首页"));
		tagAttrs.add(new TagAttr(TagAttr_Last, false, TagAttrDataType.STRING, "末页链接名称", "末页"));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		String type = MapUtils.getString(attrs, TagAttr_Type, PageBarType.Simple.name());
		String target = MapUtils.getString(attrs, TagAttr_Target, LinkTarget._self.name());
		String firstPage = MapUtils.getString(attrs, TagAttr_First, "首页");
		String lastPage = MapUtils.getString(attrs, TagAttr_Last, "末页");

		env.getOut().write(switch (PageBarType.valueOf(type)) {
			case Mini -> generateMinPageBar(target, firstPage, lastPage, env);
			case Simple -> generateSimplePageBar(target, firstPage, lastPage, env);
		});
		return null;
	}

	private String generateMinPageBar(String target, String firstPage, String lastPage, Environment env)
			throws TemplateException {
		return generatePageBar(target, firstPage, lastPage, false, env);
	}

	private String generateSimplePageBar(String target, String firstPage, String lastPage, Environment env)
			throws TemplateException {
		return generatePageBar(target, firstPage, lastPage, true, env);
	}

	/**
	 * <a href="#" class="page_link page_active" target="_blank">1</a>
	 * [首页]...[2][3][4] 5 [6][7][8]...[末页]
	 * 最多显示7个页码，当前页前后各三个
	 */
	private String generatePageBar(String target, String firstPage, String lastPage, boolean withFirstAndLast, Environment env)
			throws TemplateException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		int pageSize = context.getPageSize() == 0 ? 20 : context.getPageSize();
		int pageCount = Long.valueOf((context.getPageTotal() + pageSize - 1 ) / pageSize).intValue();
		int startPage = 1;
		int endPage = 7;
		if (context.getPageIndex() > 4) {
			int maxMove = Math.max(pageCount - endPage, 0);
			startPage += Math.min(context.getPageIndex() - 4, maxMove);
		}
		endPage = Math.min(startPage + 6, pageCount);

		String firstPageLink = FreeMarkerUtils.evalStringVariable(env, StaticizeConstants.TemplateVariable_FirstPage);
		String otherPageLink = FreeMarkerUtils.evalStringVariable(env, StaticizeConstants.TemplateVariable_OtherPage);
		String temp = "<a href=\"{0}\" class=\"page_link{1}\" target=\"{2}\">{3}</a>";
		StringBuilder sb = new StringBuilder();
		sb.append("<div class=\"pagination\">");
		if (withFirstAndLast && startPage > 1) {
			sb.append(StringUtils.messageFormat(temp, firstPageLink, " page_first", target, firstPage));
			sb.append("<a href=\"javascript:;\" class=\"page_white\">...</a>");
		}
		for (int i = startPage; i <= endPage; i++) {
			String pageLink = "javascript:;";
			String active = " page_active";
			if (i != context.getPageIndex()) {
				pageLink = i > 1 ? StringUtils.messageFormat(otherPageLink, i) : firstPageLink;
				active = "";
			}
			sb.append(StringUtils.messageFormat(temp, pageLink, active, target, i));
		}
		if (withFirstAndLast && endPage < pageCount) {
			sb.append("<a href=\"javascript:;\" class=\"page_white\">...</a>");
			sb.append(StringUtils.messageFormat(temp, StringUtils.messageFormat(otherPageLink, pageCount), " page_last", target, lastPage));
		}
		sb.append("</div>");
		return sb.toString();
	}

	private enum PageBarType {

		Mini("极简（页码）"),
		Simple("简单（页码+首末页）");

		private final String desc;

		PageBarType(String desc) {
			this.desc = desc;
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Mini.name(), Mini.desc),
					new TagAttrOption(Simple.name(), Simple.desc)
			);
		}
	}

	private enum LinkTarget {

		_blank("新标签页打开"),
		_self("当前页打开");

		private final String desc;

		LinkTarget(String desc) {
			this.desc = desc;
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(_blank.name(), _blank.desc),
					new TagAttrOption(_self.name(), _self.desc)
			);
		}
	}
}
