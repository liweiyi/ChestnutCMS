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
package com.chestnut.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.domain.HotWord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface IHotWordService extends IService<HotWord> {

	@Transactional(rollbackFor = Exception.class)
	void addHotWord(HotWord hotWord);

	void editHotWord(HotWord hotWord);

	@Transactional(rollbackFor = Exception.class)
    void deleteHotWords(List<Long> hotWordIds);

    /**
	 * 缓存热词对象
	 */
	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	class HotWordCache {
		private String word;
		private String url;
		private String target;
	}

	/**
	 * 获取指定分组热词集合
	 * 
	 * @param groupCode
	 * @return
	 */
	Map<String, HotWordCache> getHotWords(String groupCode);

	/**
	 * 按指定热词分组处理内容中的热词
	 * 
	 * @param text
	 * @param groupCodes
	 * @param target
	 * @param replacementTemplate
	 * @return
	 */
	String replaceHotWords(String text, String[] groupCodes, String target, String replacementTemplate);
}
