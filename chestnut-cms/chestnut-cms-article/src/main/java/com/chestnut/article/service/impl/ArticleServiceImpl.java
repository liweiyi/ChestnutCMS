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
package com.chestnut.article.service.impl;

import com.chestnut.article.IArticleBodyFormat;
import com.chestnut.article.dao.CmsArticleDetailDAO;
import com.chestnut.article.service.IArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements IArticleService {

	private final CmsArticleDetailDAO dao;


	private final Map<String, IArticleBodyFormat> articleBodyFormatMap;

	@Override
	public CmsArticleDetailDAO dao() {
		return this.dao;
	}

	@Override
	public IArticleBodyFormat getArticleBodyFormat(String format) {
		return articleBodyFormatMap.get(IArticleBodyFormat.BEAN_PREFIX + format);
	}

}
