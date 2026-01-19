/*
 * Copyright 2022-2026 兮玥(190785909@qq.com)
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
package com.chestnut.contentcore.template.func;

import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.func.AbstractFunc;
import com.chestnut.common.utils.ObjectUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.InternalURL;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.contentcore.util.SiteUtils;
import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Freemarker模板自定义函数：生成图片缩略图
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageSizeFunction extends AbstractFunc {

	private static final String FUNC_NAME = "imageSize";

	private static final String DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".DESC}";

	private static final String ARG1_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Name}";

	private static final String ARG1_DESC = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg1.Desc}";

	private static final String ARG2_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg2.Name}";

	private static final String ARG3_NAME = "{FREEMARKER.FUNC." + FUNC_NAME + ".Arg3.Name}";

	private final ISiteService siteService;

	private final IResourceService resourceService;

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length < 3 || ObjectUtils.isAnyNull(args)) {
			return StringUtils.EMPTY;
		}
		String iurl = ((SimpleScalar) args[0]).getAsString();
		int width = ((SimpleNumber) args[1]).getAsNumber().intValue();
		int height = ((SimpleNumber) args[2]).getAsNumber().intValue();
		if (width <= 0 || width >= 6144 || height <= 0 || height >= 6144) {
			throw new TemplateModelException("Function[imageSize]: make sure the width/height is between 0 - 6144.");
		}
		if (!InternalUrlUtils.isInternalUrl(iurl)) {
			return iurl; // 非内部链接直接返回
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		InternalURL internalUrl = InternalUrlUtils.parseInternalUrl(iurl);
		if (Objects.isNull(internalUrl) || !InternalDataType_Resource.ID.equals(internalUrl.getType())) {
			return iurl;
		}
		String originalUrl = InternalUrlUtils.getActualUrl(internalUrl, context.getPublishPipeCode(), context.isPreview());
		String siteResourceRoot = SiteUtils.getSiteResourceRoot(siteService.getSite(Long.valueOf(internalUrl.getParams().get("sid"))));
		String destPath = StringUtils.substringBeforeLast(internalUrl.getPath(), ".") + "_" + width + "x" + height
				+ "." + StringUtils.substringAfterLast(internalUrl.getPath(), ".");
		String thumbnailUrl = StringUtils.substringBeforeLast(originalUrl, ".") + "_" + width + "x" + height + "."
				+ StringUtils.substringAfterLast(originalUrl, ".");
		if (Files.exists(Path.of(siteResourceRoot + destPath))) {
			return thumbnailUrl;
		}
		try {
			if (resourceService.createThumbnailIfNotExists(internalUrl, width, height)) {
                return thumbnailUrl;
            }
			return originalUrl;
		} catch (Exception e) {
			log.warn("Generate thumbnail failed: " + originalUrl, e);
		}
		return originalUrl;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg(ARG1_NAME, FuncArgType.String, true, ARG1_DESC),
				new FuncArg(ARG2_NAME, FuncArgType.Int, true),
				new FuncArg(ARG3_NAME, FuncArgType.Int, true)
		);
	}
}
