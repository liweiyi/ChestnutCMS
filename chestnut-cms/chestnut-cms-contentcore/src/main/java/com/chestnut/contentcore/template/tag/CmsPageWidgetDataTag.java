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
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.StaticizeConstants;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IPageWidgetType;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsPageWidgetDataTag extends AbstractTag {

	public final static String TAG_NAME = "cms_pagewidget_data";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";
	
	final static String TagAttr_Code = "code";
	
	private final IPageWidgetService pageWidgetService;

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
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_Code, true, TagAttrDataType.STRING, "页面部件编码"));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException {
		String code = attrs.get(TagAttr_Code);
		if (StringUtils.isEmpty(code)) {
			throw new TemplateException("参数[code]不能为空", env);
		}
		Long siteId = TemplateUtils.evalSiteId(env);
		
		LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, siteId)
				.eq(CmsPageWidget::getCode, code);
		CmsPageWidget pageWidget = this.pageWidgetService.getOne(q);
		Assert.notNull(pageWidget, () -> new TemplateException(StringUtils.messageFormat("Tag attr[code={0}] data not found.", code), env));

		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(pageWidget.getType());
		Assert.notNull(pwt, () -> new TemplateException(StringUtils.messageFormat("Unknow page widget type：{0}", pageWidget.getType()), env));
		
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Object contentObj = pwt.parseContent(pageWidget, context.getPublishPipeCode(), context.isPreview());
		pageWidget.setContentObj(contentObj);
		return Map.of(StaticizeConstants.TemplateVariable_Data, this.wrap(env, pageWidget));
	}
}
