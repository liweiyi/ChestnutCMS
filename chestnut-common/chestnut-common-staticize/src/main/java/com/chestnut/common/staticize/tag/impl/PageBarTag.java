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
package com.chestnut.common.staticize.tag.impl;

import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.StaticizeConstants;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.staticize.tag.TagAttrOption;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.XCollectionUtils;
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
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_TYPE = "{FREEMARKER.TAG." + TAG_NAME + ".type}";
	public final static String ATTR_USAGE_TARGET = "{FREEMARKER.TAG." + TAG_NAME + ".target}";
	public final static String ATTR_USAGE_FIRST = "{FREEMARKER.TAG." + TAG_NAME + ".first}";
	public final static String ATTR_USAGE_LAST = "{FREEMARKER.TAG." + TAG_NAME + ".last}";
	public final static String ATTR_DEFAULT_FIRST = "{FREEMARKER.TAG." + TAG_NAME + ".first.defaultValue}";
	public final static String ATTR_DEFAULT_LAST = "{FREEMARKER.TAG." + TAG_NAME + ".last.defaultValue}";
	public final static String ATTR_OPTIONS_TYPE_MINI = "{FREEMARKER.TAG." + TAG_NAME + ".type.mini}";
	public final static String ATTR_OPTIONS_TYPE_SIMPLE = "{FREEMARKER.TAG." + TAG_NAME + ".type.simple}";
	public final static String ATTR_OPTIONS_TARGET_BLANK = "{FREEMARKER.TAG." + TAG_NAME + ".target._blank}";
	public final static String ATTR_OPTIONS_TARGET_SELF = "{FREEMARKER.TAG." + TAG_NAME + ".target._self}";
	public final static String ATTR_USAGE_PARAMS = "{FREEMARKER.TAG." + TAG_NAME + ".params}";

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

	final static String ATTR_TYPE = "type";
	final static String ATTR_TARGET = "target";
	final static String ATTR_FIRST = "first";
	final static String ATTR_LAST = "last";
	final static String ATTR_PARAMS = "params";

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(ATTR_TYPE, false, TagAttrDataType.STRING, ATTR_USAGE_TYPE,
				PageBarType.toTagAttrOptions(), PageBarType.Simple.name()));
		tagAttrs.add(new TagAttr(ATTR_TARGET, false, TagAttrDataType.STRING, ATTR_USAGE_TARGET,
				LinkTarget.toTagAttrOptions(), LinkTarget._self.name()));
		tagAttrs.add(new TagAttr(ATTR_FIRST, false, TagAttrDataType.STRING, ATTR_USAGE_FIRST));
		tagAttrs.add(new TagAttr(ATTR_LAST, false, TagAttrDataType.STRING, ATTR_USAGE_LAST));
		tagAttrs.add(new TagAttr(ATTR_PARAMS, false, TagAttrDataType.STRING, ATTR_USAGE_PARAMS));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		String type = MapUtils.getString(attrs, ATTR_TYPE, PageBarType.Simple.name());
		String target = MapUtils.getString(attrs, ATTR_TARGET, LinkTarget._self.name());
		String firstPage = MapUtils.getString(attrs, ATTR_FIRST, I18nUtils.get(ATTR_DEFAULT_FIRST));
		String lastPage = MapUtils.getString(attrs, ATTR_LAST, I18nUtils.get(ATTR_DEFAULT_LAST));
		String params = MapUtils.getString(attrs, ATTR_PARAMS);
		if (StringUtils.isEmpty(params)) {
			Map<?, ?> mapVariable = FreeMarkerUtils.getImmutableMapVariable(env, StaticizeConstants.TemplateVariable_Request);
			if (!mapVariable.isEmpty()) {
				params = XCollectionUtils.join(
						mapVariable.entrySet(),
						"&",
						entry -> entry.getKey().toString() + "=" + entry.getValue().toString(),
						entry -> !StaticizeConstants.TemplateParam_PageIndex.equals(entry.getKey())
				);
			}
		}

		env.getOut().write(switch (PageBarType.valueOf(type)) {
			case Mini -> generateMinPageBar(target, firstPage, lastPage, params, env);
			case Simple -> generateSimplePageBar(target, firstPage, lastPage, params, env);
		});
		return null;
	}

	private String generateMinPageBar(String target, String firstPage, String lastPage, String params, Environment env)
			throws TemplateException {
		return generatePageBar(target, firstPage, lastPage, params, false, env);
	}

	private String generateSimplePageBar(String target, String firstPage, String lastPage, String params, Environment env)
			throws TemplateException {
		return generatePageBar(target, firstPage, lastPage, params, true, env);
	}

	/**
	 * <a href="#" class="page_link page_active" target="_blank">1</a>
	 * [首页]...[2][3][4] 5 [6][7][8]...[末页]
	 * 最多显示7个页码，当前页前后各三个
	 */
	private String generatePageBar(String target, String firstPage, String lastPage, String params, boolean withFirstAndLast, Environment env)
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
		if (StringUtils.isNotEmpty(params)) {
			firstPageLink += (firstPageLink.contains("?") ? "&" : "?") + params;
			otherPageLink += (otherPageLink.contains("?") ? "&" : "?") + params;
		}
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

		Mini(ATTR_OPTIONS_TYPE_MINI),
		Simple(ATTR_OPTIONS_TYPE_SIMPLE);

		private final String desc;

		PageBarType(String desc) {
			this.desc = desc;
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Mini.name(), I18nUtils.get(Mini.desc)),
					new TagAttrOption(Simple.name(), I18nUtils.get(Simple.desc))
			);
		}
	}

	private enum LinkTarget {

		_blank(ATTR_OPTIONS_TARGET_BLANK),
		_self(ATTR_OPTIONS_TARGET_SELF);

		private final String desc;

		LinkTarget(String desc) {
			this.desc = desc;
		}

		static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(_blank.name(), I18nUtils.get(_blank.desc)),
					new TagAttrOption(_self.name(), I18nUtils.get(_self.desc))
			);
		}
	}
}
