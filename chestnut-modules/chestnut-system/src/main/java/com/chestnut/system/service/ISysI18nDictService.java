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
package com.chestnut.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.config.I18nMessageSource;
import com.chestnut.system.domain.SysI18nDict;
import com.chestnut.system.domain.dto.BatchSaveI18nDictRequest;
import com.chestnut.system.domain.dto.CreateI18nDictRequest;
import com.chestnut.system.domain.dto.UpdateI18nDictRequest;

import java.io.IOException;
import java.util.List;

public interface ISysI18nDictService extends IService<SysI18nDict> {

	/**
	 * 获取国际化键值
	 * 
	 * @param languageTag 语言标签
	 * @param langKey 国际化键名
	 * @return 结果
	 */
	String getLangValue(String languageTag, String langKey);

	/**
	 * 获取指定国际化键名的数据列表
	 *
	 * @param langKey 国际化键名
	 * @return 结果
	 */
	List<SysI18nDict> listByLangKey(String langKey);

	/**
	 * 新增国际化字典数据
	 * 
	 * @param req 国际化字典数据
	 */
	void insertI18nDict(CreateI18nDictRequest req);

	/**
	 * 更新国际化字典数据
	 * 
	 * @param req 国际化字典数据
	 */
	void updateI18nDict(UpdateI18nDictRequest req);
	
	/**
	 * 批量更新国际化字典数据，不存在则插入
	 * 
	 * @param dictList 国际化字典数据列表
	 */
	void batchSaveI18nDicts(List<BatchSaveI18nDictRequest> dictList);

	default void saveOrUpdate(String langTag, String langKey, String langValue) {
		BatchSaveI18nDictRequest i18nDict = new BatchSaveI18nDictRequest();
		i18nDict.setLangTag(langTag);
		i18nDict.setLangKey(langKey);
		i18nDict.setLangValue(langValue);
		batchSaveI18nDicts(List.of(i18nDict));
	}

	/**
	 * 修改国际化键名
	 *
	 * @param oldLangKey 原键名
	 * @param newLangKey 新键名
	 * @param includePrefix 是更新否键名以“oldLangKey+'.'”开头的数据
	 */
	void changeLangKey(String oldLangKey, String newLangKey, boolean includePrefix);

	/**
	 * 删除国际化字典数据
	 * 
	 * @param i18nDictIds 国际化字典ID列表
	 */
	void deleteI18nDictByIds(List<Long> i18nDictIds);

	/**
	 * 根据国际化键名删除数据
	 *
	 * @param langKey 国际化键名
	 * @param prefix 是否前缀匹配模式
	 */
	void deleteByLangKey(String langKey, boolean prefix);

	/**
	 * 加载国际化资源
	 *
	 * @param i18nMessageSource 
	 * @throws IOException 
	 */
	void loadMessages(I18nMessageSource i18nMessageSource) throws IOException;
}
