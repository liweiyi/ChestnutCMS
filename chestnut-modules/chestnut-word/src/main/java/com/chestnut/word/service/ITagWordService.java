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
package com.chestnut.word.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.word.cache.TagWordMonitoredCache;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.dto.BatchAddTagRequest;
import com.chestnut.word.domain.dto.CreateTagWordRequest;
import com.chestnut.word.domain.dto.UpdateTagWordRequest;

import java.util.List;

public interface ITagWordService extends IService<TagWord> {


    /**
     * 获取TAG词列表，优先缓存
     *
     * @param groupCode
     */
    List<TagWordMonitoredCache.TagWordCache> getTagWords(String owner, String groupCode);

    /**
	 * 添加TAG词
	 * 
	 * @param req
	 */
	void addTagWord(CreateTagWordRequest req);

	/**
	 * 批量添加TAG词
	 *
	 * @param dto
	 */
    void batchAddTagWord(BatchAddTagRequest dto);

    /**
	 * 编辑TAG词
	 * 
	 * @param req
	 */
	void editTagWord(UpdateTagWordRequest req);

	/**
	 * 删除TAG词
	 * 
	 * @param tagWordIds
	 */
	void deleteTagWords(List<Long> tagWordIds);
}
