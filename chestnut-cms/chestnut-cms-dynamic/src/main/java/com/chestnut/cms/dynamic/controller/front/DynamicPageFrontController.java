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
package com.chestnut.cms.dynamic.controller.front;

import com.chestnut.cms.dynamic.service.IDynamicPageService;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.contentcore.service.ISiteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * 自定义动态模板页面管理控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/dynamic/page")
public class DynamicPageFrontController extends BaseRestController {

	private final ISiteService siteService;

	private final IDynamicPageService dynamicPageService;

	@GetMapping
	public void handleDynamicPageRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, String> parameters = ServletUtils.getParameters();
		String requestURI = request.getRequestURI();

		Long siteId = MapUtils.getLong(parameters, "sid", 0L);
		String publishPipeCode = parameters.get("pp");
		boolean preview = MapUtils.getBoolean(parameters, "preview", false);
		this.dynamicPageService.generateDynamicPage(requestURI,
				siteId, publishPipeCode, preview, parameters, response);
	}
}
