/*
 * Copyright 2022-2025 兮玥(190785909@qq.com)
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
package com.chestnut.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.search.domain.DictWord;
import com.chestnut.search.domain.dto.CreateDictWordRequest;

import java.util.List;

public interface IDictWordService extends IService<DictWord> {

	/**
	 * 词库变更标识
	 */
	String getLastModified(String wordType);

	/**
	 * 批量导入词库新词
	 * 
	 * @param req 新词
	 */
	void batchAddDictWords(CreateDictWordRequest req);

	/**
	 * 删除词库新词
	 * 
	 * @param dictWordIds 词库ID列表
	 */
	void deleteDictWord(List<Long> dictWordIds);
}
