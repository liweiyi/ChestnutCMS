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
package com.chestnut.contentcore.util;

import cn.dev33.satoken.config.SaTokenConfig;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.ReflectASMUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.ContentCoreConsts;
import com.chestnut.contentcore.core.IProperty.UseType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.fixed.config.TemplateSuffix;
import com.chestnut.contentcore.properties.SiteApiUrlProperty;
import com.chestnut.system.security.StpAdminUtil;
import freemarker.core.Environment;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TemplateUtils {

	/**
	 * 模板变量：请求参数
	 */
	public final static String TemplateVariable_Request = "Request";

	/**
	 * 模板变量：<@cms_include>标签file属性请求参数
	 */
	public final static String TemplateVariable_IncludeRequest = "IncludeRequest";

	/**
	 * 模板变量：预览模式登录用户token键名
	 */
	public final static String TemplateVariable_TokenName = "TokenName";

	/**
	 * 模板变量：预览模式登录用户token
	 */
	public final static String TemplateVariable_Token = "Token";

	/**
	 * 模板变量：是否预览模式
	 */
	public final static String TemplateVariable_IsPreview = "IsPreview";

	/**
	 * 模板变量：发布通道编码
	 */
	public final static String TemplateVariable_PublishPipeCode = "PublishPipeCode";

	/**
	 * 模板变量：发布通道静态化文件访问前缀
	 */
	public final static String TemplateVariable_Prefix = "Prefix";

	/**
	 * 模板变量：资源文件访问前缀
	 */
	public final static String TemplateVariable_ResourcePrefix = "ResourcePrefix";

	/**
	 * 模板变量：站点API访问前缀
	 */
	public final static String TemplateVariable_ApiPrefix = "ApiPrefix";

	/**
	 * 模板变量：站点信息
	 */
	public final static String TemplateVariable_Site = "Site";

	/**
	 * 模板变量：栏目信息
	 */
	public final static String TemplateVariable_Catalog = "Catalog";

	/**
	 * 模板变量：内容信息
	 */
	public final static String TemplateVariable_Content = "Content";

	/**
	 * 模板变量：页面组件信息
	 */
	public final static String TemplateVariable_PageWidget = "PageWidget";

	/**
	 * 模板变量：logo链接
	 */
	public final static String TemplateVariable_OBJ_LogoSrc = "logoSrc";

	/**
	 * 模板变量：访问地址
	 */
	public final static String TemplateVariable_OBJ_Link = "link";

	/**
	 * 添加站点数据到模板上线文变量中
	 *
	 * @param site 站点
	 * @param context 模板上下文
	 */
	public static void addSiteVariables(CmsSite site, TemplateContext context) {
		// 站点属性
		Map<String, Object> mapSite = ReflectASMUtils.beanToMap(site);
		// 扩展属性
		Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(site.getConfigProps(), UseType.Site);
		configProps.forEach((key, value) -> mapSite.put(ContentCoreConsts.ConfigPropFieldPrefix + key, value));
		// 站点logo
		String siteLogo = InternalUrlUtils.getActualUrl(site.getLogo(), context.getPublishPipeCode(), context.isPreview());
		if (StringUtils.isNotEmpty(siteLogo)) {
			mapSite.put(TemplateVariable_OBJ_LogoSrc, siteLogo);
		}
		// 站点链接
		String siteLink = SiteUtils.getSiteLink(site, context.getPublishPipeCode(), context.isPreview());
		mapSite.put(TemplateVariable_OBJ_Link, siteLink);
		context.getVariables().put(TemplateVariable_Site, mapSite);
	}

	/**
	 * 添加栏目数据到模板上下文变量中
	 *
	 * @param site 站点
	 * @param catalog 栏目
	 * @param context 模板上下文
	 */
	public static void addCatalogVariables(CmsSite site, CmsCatalog catalog, TemplateContext context) {
		Map<String, Object> mapCatalog = ReflectASMUtils.beanToMap(catalog);
		// 扩展属性
		Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(catalog.getConfigProps(), UseType.Catalog);
		configProps.forEach((key, value) -> mapCatalog.put(ContentCoreConsts.ConfigPropFieldPrefix + key, value));
		// 栏目logo
		String catalogLogo = InternalUrlUtils.getActualUrl(catalog.getLogo(), context.getPublishPipeCode(), context.isPreview());
		if (StringUtils.isNotEmpty(catalogLogo)) {
			mapCatalog.put(TemplateVariable_OBJ_LogoSrc, catalogLogo);
		}
		// 栏目链接
		String catalogLink = CatalogUtils.getCatalogLink(site, catalog, 1, context.getPublishPipeCode(), context.isPreview());
		mapCatalog.put(TemplateVariable_OBJ_Link, catalogLink);
		String catalogListLink = CatalogUtils.getCatalogListLink(site, catalog, 1, context.getPublishPipeCode(), context.isPreview());
		mapCatalog.put("listLink", catalogListLink);
		context.getVariables().put(TemplateVariable_Catalog, mapCatalog);
	}

	/**
	 * 创建模板初始变量，包括全局变量和站点信息
	 *
	 * @param site 站点
	 * @param context 模板上下文
	 */
	public static void initGlobalVariables(CmsSite site, TemplateContext context) {
		context.getVariables().put(TemplateVariable_IsPreview, context.isPreview());
		context.getVariables().put(TemplateVariable_PublishPipeCode, context.getPublishPipeCode());
		// 发布通道静态化文件访问前缀
		context.getVariables().put(TemplateVariable_Prefix, SiteUtils.getPublishPipePrefix(site, context.getPublishPipeCode(), context.isPreview()));
		// 资源文件访问前缀
		context.getVariables().put(TemplateVariable_ResourcePrefix, SiteUtils.getResourcePrefix(site, context.isPreview()));
		// 站点API访问前缀
		context.getVariables().put(TemplateVariable_ApiPrefix, SiteApiUrlProperty.getValue(site, context.getPublishPipeCode()));
		// 添加站点数据
		addSiteVariables(site, context);
		if (context.isPreview()) {
			SaTokenConfig config = StpAdminUtil.getStpLogic().getConfigOrGlobal();
			context.getVariables().put(TemplateVariable_TokenName, config.getTokenName());
			String token = StpAdminUtil.getTokenValue();
			if (StringUtils.isNotEmpty(config.getTokenPrefix())) {
				token = config.getTokenPrefix() + " " + token;
			}
			context.getVariables().put(TemplateVariable_Token, token);
		}
	}

	public static String appendTokenParameter(String url, Environment env) throws TemplateModelException {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		String tokenName = FreeMarkerUtils.getStringVariable(Environment.getCurrentEnvironment(), TemplateUtils.TemplateVariable_TokenName);
		String token = FreeMarkerUtils.getStringVariable(Environment.getCurrentEnvironment(), TemplateUtils.TemplateVariable_Token);
		if (url.contains("?")) {
			return url + "&" + tokenName + "=" + token;
		}
		return url + "?" + token + "=" + token;
	}

	public static String appendTokenParameter(String url) {
		if (StringUtils.isEmpty(url)) {
			return url;
		}
		SaTokenConfig config = StpAdminUtil.getStpLogic().getConfigOrGlobal();
		String token = StpAdminUtil.getTokenValue();
		if (StringUtils.isNotEmpty(config.getTokenPrefix())) {
			token = config.getTokenPrefix() + " " + token;
		}
		if (url.contains("?")) {
			return url + "&" + config.getTokenName() + "=" + token;
		}
		return url + "?" + config.getTokenName() + "=" + token;
	}


	/**
	 * 页面区块静态文件相对路径
	 *
	 * @param site 站点
	 * @param publishPipeCode 发布通道编码
	 * @param includeTemplateName 相对resourceRoot的模板路径
	 */
	public static String getIncludeRelativeStaticPath(CmsSite site, String publishPipeCode, String includeTemplateName) {
		String siteTemplatePath = SiteUtils.getSiteTemplatePath(site.getPath(), publishPipeCode);
		return "include/" + includeTemplateName.substring(siteTemplatePath.length(),
				includeTemplateName.length() - TemplateSuffix.getValue().length())
				+ "." + site.getStaticSuffix(publishPipeCode);
	}
}
