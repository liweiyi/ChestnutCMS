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
package com.chestnut.cms.stat.controller.front;

import com.chestnut.cms.stat.handler.PageViewStatEventHandler;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.stat.core.StatEvent;
import com.chestnut.stat.service.impl.StatEventService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 统计数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
public class CmsStatApiController extends BaseRestController {

	private final StatEventService statEventService;

	/**
	 * 网站访问统计
	 */
	@GetMapping("/api/stat/visit")
	public void visitSite(
			@RequestParam("sid") Long siteId,
			@RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
			@RequestParam(value = "id", required = false, defaultValue = "0") Long contentId,
			@RequestParam(value = "h", required = false) String host,
			@RequestParam(value = "p", required = false) String path) {
		StatEvent evt = new StatEvent();
		evt.setType(PageViewStatEventHandler.TYPE);
		ObjectNode objectNode = JacksonUtils.objectNode();
		objectNode.put("sid", siteId);
		objectNode.put("cid", catalogId);
		objectNode.put("id", contentId);
		evt.setData(objectNode);
		evt.setEvtTime(LocalDateTime.now());
		evt.fillRequestData(ServletUtils.getRequest());
		evt.getRequestData().setHost(host);
		evt.getRequestData().setUri(path);
		statEventService.dealStatEvent(evt);
	}
}
