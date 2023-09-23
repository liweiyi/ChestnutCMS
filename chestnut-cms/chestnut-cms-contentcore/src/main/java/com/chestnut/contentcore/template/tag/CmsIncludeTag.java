package com.chestnut.contentcore.template.tag;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.properties.EnableSSIProperty;
import com.chestnut.contentcore.util.TemplateUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.SiteUtils;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CmsIncludeTag extends AbstractTag {

	public static final String TAG_NAME = "cms_include";
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

	private static final String TagAttr_FILE = "file";

	private static final String TagAttr_SSI = "ssi";

	private static final String TagAttr_VIRTUAL = "virtual";

	private static final String TagAttr_CACHE = "cache";

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
		tagAttrs.add(new TagAttr(TagAttr_FILE, true, TagAttrDataType.STRING, "引用模板文件路径（相对模板目录template/）"));
		tagAttrs.add(new TagAttr(TagAttr_SSI, false, TagAttrDataType.BOOLEAN, "是否启用SSI", "true"));
		tagAttrs.add(new TagAttr(TagAttr_VIRTUAL, false, TagAttrDataType.BOOLEAN, "是否启用virtual，此模式下区块无法继承当前页面上限文变量，需要通过参数传入需要的变量", "false"));
		tagAttrs.add(new TagAttr(TagAttr_CACHE, false, TagAttrDataType.BOOLEAN, "是否启用缓存", "true"));

		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);

		String file = attrs.get(TagAttr_FILE);
		Assert.notEmpty(file, () -> new TemplateException("参数[file]不能为空", env));

		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		CmsSite site = this.siteService.getSite(siteId);

		boolean ssi = MapUtils.getBoolean(attrs, TagAttr_SSI, EnableSSIProperty.getValue(site.getConfigProps()));
		boolean virtual = Boolean.parseBoolean(attrs.get(TagAttr_VIRTUAL));
		boolean cache = Boolean.parseBoolean(attrs.get(TagAttr_CACHE));

		String templateFile = StringUtils.substringBefore(file, "?");
		String params = StringUtils.substringAfter(file, "?");

		String includeTemplateKey = SiteUtils.getTemplateKey(site, context.getPublishPipeCode(), templateFile);
		if (context.isPreview()) {
			Template includeTemplate = env.getTemplateForInclusion(includeTemplateKey,
					StandardCharsets.UTF_8.displayName(), true);
			env.setVariable("IncludeRequest", wrap(env, StringUtils.splitToMap(params, "&", "=")));
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
			env.setVariable("IncludeRequest", wrap(env, params));
			env.include(includeTemplate);
			return writer.getBuffer().toString();
		} finally {
			env.setOut(out);
		}
	}
}
