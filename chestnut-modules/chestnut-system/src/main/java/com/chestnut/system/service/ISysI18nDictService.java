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
package com.chestnut.system.service;

import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chestnut.system.config.I18nMessageSource;
import com.chestnut.system.domain.SysI18nDict;

public interface ISysI18nDictService extends IService<SysI18nDict> {

	/**
	 * 获取国际化键值
	 * 
	 * @param languageTag
	 * @param langKey
	 * @return
	 */
	public String getLangValue(String languageTag, String langKey);

	/**
	 * 获取指定国际化键名的数据列表
	 * 
	 * @param langKey
	 * @return
	 */
	List<SysI18nDict> listByLangKey(String langKey);

	/**
	 * 新增国际化字典数据
	 * 
	 * @param dict
	 */
	void insertI18nDict(SysI18nDict dict);

	/**
	 * 更新国际化字典数据
	 * 
	 * @param dict
	 */
	void updateI18nDict(SysI18nDict dict);
	
	/**
	 * 批量更新国际化字典数据，不存在则插入
	 * 
	 * @param dicts
	 */
	void batchSaveI18nDicts(List<SysI18nDict> dicts);

	/**
	 * 删除国际化字典数据
	 * 
	 * @param i18nDictIds
	 */
	public void deleteI18nDictByIds(List<Long> i18nDictIds);

	/**
	 * 加载国际化资源
	 * @param i18nMessageSource 
	 * @throws IOException 
	 */
	public void loadMessages(I18nMessageSource i18nMessageSource) throws IOException;
}
