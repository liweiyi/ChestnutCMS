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
package com.chestnut.contentcore.controller;

import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.core.IInternalDataType;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.template.ITemplateType;
import com.chestnut.contentcore.template.impl.SiteTemplateType;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.system.security.AdminUserType;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

/**
 * 内容核心管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class CoreController extends BaseRestController {

	private final ISiteService siteService;

	private final ITemplateService templateService;

	private final StaticizeService staticizeService;

	/**
	 * 预览内容核心数据页面
	 *
	 * @param dataType    内容核心数据类型ID
	 * @param dataId      内容核心数据ID
	 * @param publishPipe 发布通道编码
	 * @param pageIndex   页码
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/cms/preview/{dataType}/{dataId}")
	public void preview(@PathVariable("dataType") String dataType, @PathVariable("dataId") Long dataId,
						@RequestParam(value = "pp") String publishPipe,
						@RequestParam(value = "pi", required = false, defaultValue = "1") Integer pageIndex,
						@RequestParam(value = "list", required = false, defaultValue = "N") String listFlag)
			throws IOException, TemplateException {
		HttpServletResponse response = ServletUtils.getResponse();
		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());
		IInternalDataType internalDataType = ContentCoreUtils.getInternalDataType(dataType);
		Assert.notNull(internalDataType, () -> ContentCoreErrorCode.UNSUPPORTED_INTERNAL_DATA_TYPE.exception(dataType));

		IInternalDataType.RequestData data = new IInternalDataType.RequestData(dataId, pageIndex, publishPipe,
				true, ServletUtils.getParamMap(ServletUtils.getRequest()));
		String pageData = internalDataType.getPageData(data);
		response.getWriter().write(pageData);
	}

	/**
	 * 浏览内部数据页面
	 *
	 * @param dataType    内容核心数据类型ID
	 * @param dataId      内容核心数据ID
	 * @param publishPipe 发布通道编码
	 * @param pageIndex   页码
	 */
	@GetMapping("/cms/view/{dataType}/{dataId}")
	public void browse(@PathVariable("dataType") String dataType, @PathVariable("dataId") Long dataId,
					   @RequestParam(value = "pp") String publishPipe,
					   @RequestParam(value = "pi", required = false, defaultValue = "1") Integer pageIndex)
			throws IOException, TemplateException {
		HttpServletResponse response = ServletUtils.getResponse();

		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());
		IInternalDataType internalDataType = ContentCoreUtils.getInternalDataType(dataType);
		Assert.notNull(internalDataType, () -> ContentCoreErrorCode.UNSUPPORTED_INTERNAL_DATA_TYPE.exception(dataType));

		IInternalDataType.RequestData data = new IInternalDataType.RequestData(dataId, pageIndex, publishPipe,
				false, ServletUtils.getParamMap(ServletUtils.getRequest()));
		String pageData = internalDataType.getPageData(data);
		response.getWriter().write(pageData);
	}

	@GetMapping("/cms/ssi/virtual/")
	public void getSSIVirtualContent(@RequestParam("sid") Long siteId, @RequestParam("pp") String publishPipeCode,
									 @RequestParam("t") String template,
									 @RequestParam(value = "pi", required = false, defaultValue = "1") Integer pageIndex,
									 @RequestParam Map<String, Object> params) {
		try {
			// TODO 缓存
			long s = System.currentTimeMillis();
			CmsSite site = this.siteService.getSite(siteId);
			// 模板ID = 通道:站点目录:模板文件名
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
			// 缓存
			String templateStaticContentCache = this.templateService.getTemplateStaticContentCache(templateKey);
			if (Objects.nonNull(templateStaticContentCache)) {
				ServletUtils.getResponse().getWriter().write(templateStaticContentCache);
				return;
			}
			TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
			templateContext.setPageIndex(pageIndex);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(SiteTemplateType.TypeId);
			templateType.initTemplateData(siteId, templateContext);
			templateContext.getVariables().put("Request", params);
			templateContext.getVariables().put("ClientType", ServletUtils.getDeviceType());
			// staticize
			this.staticizeService.process(templateContext, ServletUtils.getResponse().getWriter());
			log.debug("[{}]动态区块模板解析：{}，耗时：{}", publishPipeCode, template, System.currentTimeMillis() - s);
		} catch (TemplateException | IOException e) {
			log.error("[{}]Process ssi virtual failed: {}", publishPipeCode, template);
		}
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/cms/dynamicPageTypes")
	public R<?> getDynamicPageTypes() {
		return R.ok(ContentCoreUtils.getDynamicPageTypes());
	}
}
