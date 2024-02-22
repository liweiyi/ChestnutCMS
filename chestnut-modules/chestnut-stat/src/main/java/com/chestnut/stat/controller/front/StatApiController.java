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
package com.chestnut.stat.controller.front;

import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
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
public class StatApiController extends BaseRestController {

	private final StatEventService statEventService;

	/**
	 * 统计API
	 */
	@GetMapping("/api/stat/evt")
	public void statEvent(@RequestParam("t") String type, @RequestParam(required = false) String data) {
		StatEvent evt = new StatEvent();
		evt.setType(type);
		if (StringUtils.isNotEmpty(data)) {
			ObjectNode objectNode = JacksonUtils.objectNode();
			StringUtils.splitToMap(data, "&", "=").forEach(objectNode::put);
			evt.setData(objectNode);
		}
		evt.fillRequestData(ServletUtils.getRequest());
		evt.setEvtTime(LocalDateTime.now());

		statEventService.dealStatEvent(evt);
	}
}
