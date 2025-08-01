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
package com.chestnut.customform.template.tag;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.exception.IncludeTemplateNotFoundException;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.EnableSSIProperty;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.tag.CmsIncludeTag;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.customform.CustomFormConsts;
import com.chestnut.customform.domain.CmsCustomForm;
import com.chestnut.customform.fixed.dict.CustomFormStatus;
import com.chestnut.customform.mapper.CustomFormMapper;
import com.chestnut.customform.publishpipe.PublishPipeProp_CustomFormTemplate;
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
public class CmsCustomFormTag extends AbstractTag {

	public final static String TAG_NAME = "cms_custom_form";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CODE = "{FREEMARKER.TAG." + TAG_NAME + ".code}";
	public final static String ATTR_USAGE_SSI = "{FREEMARKER.TAG." + TAG_NAME + ".ssi}";

	final static String ATTR_CODE = "code";

	final static String ATTR_SSI = "ssi";

	private final ISiteService siteService;

	private final CustomFormMapper customFormMapper;

	private final ITemplateService templateService;

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

		Long siteId = TemplateUtils.evalSiteId(env);

		CmsCustomForm form = new LambdaQueryChainWrapper<>(this.customFormMapper)
				.eq(CmsCustomForm::getSiteId, siteId)
				.eq(CmsCustomForm::getCode, code).one();
		Assert.notNull(form, () -> new TemplateException(StringUtils.messageFormat("Custom form no exists, code = {0}.", code), env));
		if (CustomFormStatus.PUBLISHED != form.getStatus()) {
			return null;
		}

		CmsSite site = this.siteService.getSite(siteId);
		String template = form.getTemplates().getOrDefault(context.getPublishPipeCode(),
				PublishPipeProp_CustomFormTemplate.getValue(context.getPublishPipeCode(), site.getPublishPipeProps()));
		File templateFile = this.templateService.findTemplateFile(site, template, context.getPublishPipeCode());
		Assert.notNull(templateFile, () -> new IncludeTemplateNotFoundException(template, env));

		boolean ssi = MapUtils.getBoolean(attrs, ATTR_SSI, EnableSSIProperty.getValue(site.getConfigProps()));
		String templateKey = SiteUtils.getTemplateKey(site, context.getPublishPipeCode(), template);
		if (context.isPreview()) {
			env.getOut().write(this.processTemplate(env, form, site, context.getPublishPipeCode(), templateKey));
		} else {
			String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
			String staticFileName = form.getCode() + "." + site.getStaticSuffix(context.getPublishPipeCode());
			String staticFilePath = CustomFormConsts.STATICIZE_DIRECTORY + staticFileName;
			if (ssi) {
				// 读取自定义表单静态化内容
				String staticContent = templateService.getTemplateStaticContentCache(templateKey);
				if (Objects.isNull(staticContent) || !new File(siteRoot + staticFilePath).exists()) {
					staticContent = this.processTemplate(env, form, site, context.getPublishPipeCode(), templateKey);
					FileUtils.writeStringToFile(new File(siteRoot + staticFilePath), staticContent, StandardCharsets.UTF_8);
					this.templateService.setTemplateStaticContentCache(templateKey, staticContent);
				}
				String prefix = SiteUtils.getPublishPipePrefix(site, context.getPublishPipeCode(), context.isPreview());
				env.getOut().write(StringUtils.messageFormat(CmsIncludeTag.SSI_INCLUDE_TAG, prefix + staticFilePath));
			} else {
				// 非ssi模式无法使用缓存
				String staticContent = this.processTemplate(env, form, site, context.getPublishPipeCode(), templateKey);
				env.getOut().write(staticContent);
			}
		}
		return null;
	}

	private String processTemplate(Environment env, CmsCustomForm form, CmsSite site, String publishPipeCode, String templateName)
			throws TemplateException, IOException {
		Writer out = env.getOut();
		try (StringWriter writer = new StringWriter()) {
			env.setOut(writer);
			Template includeTemplate = env.getTemplateForInclusion(templateName,
					StandardCharsets.UTF_8.displayName(), true);
			Map<String, Object> customFormVariables = CustomFormConsts.getCustomFormVariables(form, site, publishPipeCode);
			FreeMarkerUtils.setVariables(env, Map.of(CustomFormConsts.TemplateVariable_CustomForm,
					this.wrap(env, customFormVariables)));
			env.include(includeTemplate);
			return writer.getBuffer().toString();
		} finally {
			env.setOut(out);
		}
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
