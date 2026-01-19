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
package com.chestnut.cms.search.template.tag;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.search.CmsSearchConstants;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.contentcore.util.TemplateUtils;
import com.chestnut.search.domain.SearchWord;
import com.chestnut.search.service.ISearchWordService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsSearchWordTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_search_word";

	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";

	private final ISearchWordService searchWordService;
	
	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		Long siteId = TemplateUtils.evalSiteId(env);
		String source = CmsSearchConstants.generateSearchSource(siteId);
		Page<SearchWord> pageResult = this.searchWordService.lambdaQuery()
				.eq(SearchWord::getSource, source)
				.orderByDesc(SearchWord::getTopFlag, SearchWord::getSearchTotal)
				.page(new Page<>(pageIndex, size, page));
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
	}

	@Override
	public Class<SearchWord> getDataClass() {
		return SearchWord.class;
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getDescription() {
		return DESC;
	}
}
