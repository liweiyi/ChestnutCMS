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

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.exception.MissionTagAttributeException;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.properties.EnableSSIProperty;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import freemarker.core.Environment;
import freemarker.template.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
@Component
public class CmsIncludeTag extends AbstractTag {

	public static final String TAG_NAME = "cms_include";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_FILE = "{FREEMARKER.TAG." + TAG_NAME + ".file}";
	public final static String ATTR_USAGE_SSI = "{FREEMARKER.TAG." + TAG_NAME + ".ssi}";
	public final static String ATTR_USAGE_VIRTUAL = "{FREEMARKER.TAG." + TAG_NAME + ".virtual}";
	public final static String ATTR_USAGE_CACHE = "{FREEMARKER.TAG." + TAG_NAME + ".cache}";

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

	private static final String ATTR_FILE = "file";

	private static final String ATTR_SSI = "ssi";

	private static final String ATTR_VIRTUAL = "virtual";

	private static final String ATTR_CACHE = "cache";

	/**
	 * <@cms_include file="footer.template.html"></@cms_include>
	 */
	public static final String SSI_INCLUDE_TAG = "<!--#include file=\"{0}\" -->\n";

	/**
	 * 使用virtual来访问动态区块内容，实际访问后台/ssi/virtual接口返回html内容后包含在前端页面中。
	 * 与ajax异步获取内容不同，此方式可将动态内容与html同步返回有利于Spider获取页面更新， 包含模板中可用${Request.xxx}获取参数值
	 *
	 * <@cms_include virtual="footer.template.html?t=123&c=ddd"></@cms_include>
	 */
	public static final String SSI_INCLUDE_VIRTUAL_TAG = "<!--#include virtual=\"{0}\" -->\n";

	private final ISiteService siteService;

	private final ITemplateService templateService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(ATTR_FILE, true, TagAttrDataType.STRING, ATTR_USAGE_FILE));
		tagAttrs.add(new TagAttr(ATTR_SSI, false, TagAttrDataType.BOOLEAN, ATTR_USAGE_SSI, Boolean.TRUE.toString()));
		tagAttrs.add(new TagAttr(ATTR_VIRTUAL, false, TagAttrDataType.BOOLEAN, ATTR_USAGE_VIRTUAL, Boolean.FALSE.toString()));
		tagAttrs.add(new TagAttr(ATTR_CACHE, false, TagAttrDataType.BOOLEAN, ATTR_USAGE_CACHE, Boolean.TRUE.toString()));

		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);

		String file = attrs.get(ATTR_FILE);
		Assert.notEmpty(file, () -> new MissionTagAttributeException(TAG_NAME, ATTR_FILE, env));

		Long siteId = TemplateUtils.evalSiteId(env);
		CmsSite site = this.siteService.getSite(siteId);

		boolean ssi = MapUtils.getBoolean(attrs, ATTR_SSI, EnableSSIProperty.getValue(site.getConfigProps()));
		boolean virtual = Boolean.parseBoolean(attrs.get(ATTR_VIRTUAL));
		boolean cache = Boolean.parseBoolean(attrs.get(ATTR_CACHE));

		String templateFile = StringUtils.substringBefore(file, "?");
		String params = StringUtils.substringAfter(file, "?");

		String includeTemplateKey = SiteUtils.getTemplateKey(site, context.getPublishPipeCode(), templateFile);
		if (context.isPreview()) {
			Template includeTemplate = env.getTemplateForInclusion(includeTemplateKey,
					StandardCharsets.UTF_8.displayName(), true);
			Map<String, String> paramsMap = StringUtils.splitToMap(params, "&", "=");
			Map<String, String> mergeParams = mergeRequestVariable(env, paramsMap);
			env.setVariable(TemplateUtils.TemplateVariable_Request, wrap(env, mergeParams));
			// TODO 兼容历史版本，1.6.0移除IncludeRequest模板变量
			env.setVariable(TemplateUtils.TemplateVariable_IncludeRequest, wrap(env, mergeParams));
			env.include(includeTemplate);
		} else if (virtual) {
			// 动态模板
			String virtualPath = "/cms/ssi/virtual?sid=" + siteId + "&pp=" + context.getPublishPipeCode()
					+ "&t=" + templateFile + "&" + params;
			env.getOut().write(StringUtils.messageFormat(SSI_INCLUDE_VIRTUAL_TAG, virtualPath));
		} else {
			String cacheKey = includeTemplateKey + (StringUtils.isEmpty(params) ? "" : ("?" + params));
			String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
			String staticFilePath = TemplateUtils.getIncludeRelativeStaticPath(site,
					context.getPublishPipeCode(), includeTemplateKey);
			String staticContent = cache ? templateService.getTemplateStaticContentCache(cacheKey) : null;
			if (Objects.isNull(staticContent) || !new File(siteRoot + staticFilePath).exists()) {
				staticContent = processTemplate(env, StringUtils.getPathParameterMap(file), includeTemplateKey);
				if (ssi) {
					FileUtils.writeStringToFile(new File(siteRoot + staticFilePath), staticContent, StandardCharsets.UTF_8);
				}
				if (cache) {
					this.templateService.setTemplateStaticContentCache(cacheKey, staticContent);
				}
			}
			if (ssi) {
				env.getOut().write(StringUtils.messageFormat(SSI_INCLUDE_TAG, "/" + staticFilePath));
			} else {
				env.getOut().write(staticContent);
			}
		}
		return null;
	}

	private Map<String, String> mergeRequestVariable(Environment env, Map<String, String> params) throws TemplateModelException {
		TemplateModel variable = env.getVariable(TemplateUtils.TemplateVariable_Request);
		if (Objects.nonNull(variable)) {
			if (variable instanceof TemplateHashModelEx2 req) {
				for (TemplateHashModelEx2.KeyValuePairIterator iterator = req.keyValuePairIterator();iterator.hasNext();) {
					TemplateHashModelEx2.KeyValuePair next = iterator.next();
					String key = ((SimpleScalar) next.getKey()).getAsString();
					if (params.containsKey(key)) {
						log.warn("<@cms_include> file parameter `{}` conflicts with the Request parameter.", key);
					}
					params.put(key, ((SimpleScalar) next.getValue()).getAsString());
				}
			}
		}
		return params;
	}

	/**
	 * 生成包含模板静态化内容
	 */
	private String processTemplate(Environment env, Map<String, String> params, String includeTemplateName)
			throws TemplateException, IOException {
		Writer out = env.getOut();
		try (StringWriter writer = new StringWriter()) {
			env.setOut(writer);
			Template includeTemplate = env.getTemplateForInclusion(includeTemplateName,
					StandardCharsets.UTF_8.displayName(), true);
			Map<String, String> mergeParams = mergeRequestVariable(env, params);
			env.setVariable(TemplateUtils.TemplateVariable_Request, wrap(env, mergeParams));
			// TODO 兼容历史版本，1.6.0版本移除IncludeRequest模板变量
			env.setVariable(TemplateUtils.TemplateVariable_IncludeRequest, wrap(env, mergeParams));
			env.include(includeTemplate);
			return writer.getBuffer().toString();
		} finally {
			env.setOut(out);
		}
	}
}
