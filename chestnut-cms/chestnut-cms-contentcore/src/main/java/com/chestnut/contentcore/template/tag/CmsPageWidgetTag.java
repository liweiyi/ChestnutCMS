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

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.exception.IncludeTemplateNotFoundException;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsPageWidget;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.dict.PageWidgetStatus;
import com.chestnut.contentcore.properties.EnableSSIProperty;
import com.chestnut.contentcore.service.IPageWidgetService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.PageWidgetUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CmsPageWidgetTag extends AbstractTag {

	public final static String TAG_NAME = "cms_pagewidget";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CODE = "{FREEMARKER.TAG." + TAG_NAME + ".code}";
	public final static String ATTR_USAGE_SSI = "{FREEMARKER.TAG." + TAG_NAME + ".ssi}";

	final static String ATTR_CODE = "code";

	final static String ATTR_SSI = "ssi";
	
	private final ISiteService siteService;

	private final IPageWidgetService pageWidgetService;

	private final ITemplateService templateService;

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
		tagAttrs.add(new TagAttr(ATTR_CODE, true, TagAttrDataType.STRING, ATTR_USAGE_CODE));
		tagAttrs.add(new TagAttr(ATTR_SSI, false, TagAttrDataType.BOOLEAN, ATTR_USAGE_SSI, Boolean.TRUE.toString()));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);

		String code = attrs.get(ATTR_CODE);

		long siteId = TemplateUtils.evalSiteId(env);
		CmsPageWidget pw = this.pageWidgetService.lambdaQuery()
				.eq(CmsPageWidget::getSiteId, siteId)
				.eq(CmsPageWidget::getCode, code)
				.one();
		Assert.notNull(pw, () -> new TemplateException(StringUtils.messageFormat("Page widget [code={0}] not found", code), env));
		if (!PageWidgetStatus.PUBLISHED.equals(pw.getState())) {
			return null;
		}
		CmsSite site = this.siteService.getSite(siteId);
		File templateFile = this.templateService.findTemplateFile(site, pw.getTemplate(), context.getPublishPipeCode());
		Assert.notNull(templateFile, () -> new IncludeTemplateNotFoundException(pw.getTemplate(), env));

		boolean ssi = MapUtils.getBoolean(attrs, ATTR_SSI, EnableSSIProperty.getValue(site.getConfigProps()));
		String templateKey = SiteUtils.getTemplateKey(site, pw.getPublishPipeCode(), pw.getTemplate());
		if (context.isPreview()) {
			env.getOut().write(this.processTemplate(env, pw, templateKey));
		} else {
			String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
			String staticFileName = PageWidgetUtils.getStaticFileName(pw, site.getStaticSuffix(pw.getPublishPipeCode()));
			String staticFilePath = pw.getPath() + staticFileName;
			if (ssi) {
				// 读取页面部件静态化内容
				String staticContent = templateService.getTemplateStaticContentCache(templateKey);
				if (Objects.isNull(staticContent) || !new File(siteRoot + staticFilePath).exists()) {
					staticContent = this.processTemplate(env, pw, templateKey);
					FileUtils.writeStringToFile(new File(siteRoot + staticFilePath), staticContent, StandardCharsets.UTF_8);
					this.templateService.setTemplateStaticContentCache(templateKey, staticContent);
				}
				env.getOut().write(StringUtils.messageFormat(CmsIncludeTag.SSI_INCLUDE_TAG, "/" + staticFilePath));
			} else {
				// 非ssi模式无法使用缓存
				String staticContent = this.processTemplate(env, pw, templateKey);
				env.getOut().write(staticContent);
			}
		}
		return null;
	}
	
	private String processTemplate(Environment env, CmsPageWidget pageWidget, String templateName)
			throws TemplateException, IOException {
		Writer out = env.getOut();
		try (StringWriter writer = new StringWriter()) {
			env.setOut(writer);
			Template includeTemplate = env.getTemplateForInclusion(templateName,
					StandardCharsets.UTF_8.displayName(), true);
			env.setVariable(TemplateUtils.TemplateVariable_PageWidget, wrap(env, pageWidget));
			env.include(includeTemplate);
			return writer.getBuffer().toString();
		} finally {
			env.setOut(out);
		}
	}
}
