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
package com.chestnut.advertisement.controller.front;

import com.chestnut.advertisement.stat.AdClickStatEventHandler;
import com.chestnut.advertisement.stat.AdViewStatEventHandler;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.stat.core.StatEvent;
import com.chestnut.stat.service.impl.StatEventService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 广告统计数据收集
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adv")
public class AdApiController extends BaseRestController {

	private final StatEventService statEventService;

	@GetMapping("/redirect")
	public void statAndRedirect(@RequestParam("sid") Long siteId,
								@RequestParam("aid") Long advertisementId,
								@RequestParam("url") String redirectUrl,
								HttpServletResponse response) throws IOException {
		this.adClick(siteId, advertisementId);
		response.sendRedirect(URLDecoder.decode(redirectUrl, StandardCharsets.UTF_8));
	}

	@GetMapping("/click")
	public void adClick(@RequestParam("sid") Long siteId, @RequestParam("aid") Long advertisementId) {
		StatEvent evt = new StatEvent();
		evt.setType(AdClickStatEventHandler.TYPE);
		ObjectNode objectNode = JacksonUtils.objectNode();
		objectNode.put("sid", siteId);
		objectNode.put("aid", advertisementId);
		evt.setData(objectNode);
		evt.setEvtTime(LocalDateTime.now());
		evt.fillRequestData(ServletUtils.getRequest());
		statEventService.dealStatEvent(evt);
	}

	@GetMapping("/view")
	public void adView(@RequestParam("sid") Long siteId, @RequestParam("aid") Long advertisementId) {
		StatEvent evt = new StatEvent();
		evt.setType(AdViewStatEventHandler.TYPE);
		ObjectNode objectNode = JacksonUtils.objectNode();
		objectNode.put("sid", siteId);
		objectNode.put("aid", advertisementId);
		evt.setData(objectNode);
		evt.setEvtTime(LocalDateTime.now());
		evt.fillRequestData(ServletUtils.getRequest());
		statEventService.dealStatEvent(evt);
	}
}
