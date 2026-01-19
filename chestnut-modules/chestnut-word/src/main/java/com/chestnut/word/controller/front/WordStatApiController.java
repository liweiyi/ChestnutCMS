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
package com.chestnut.word.controller.front;

import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.word.service.WordStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 词汇数据统计API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/word/")
public class WordStatApiController extends BaseRestController {

	private final WordStatService wordStatService;

	@GetMapping("/tagword/click")
	public void tagWordClickStat(@RequestParam("id") Long wordId) {
		if (!IdUtils.validate(wordId)) {
			return;
		}
		wordStatService.updateTagWordClick(wordId);
	}

	@GetMapping("/hotword/click")
	public void hotWordClickStat(@RequestParam("id") Long wordId) {
		if (!IdUtils.validate(wordId)) {
			return;
		}
		wordStatService.updateHotWordClick(wordId);
	}
}
