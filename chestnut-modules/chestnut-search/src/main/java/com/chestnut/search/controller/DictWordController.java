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
package com.chestnut.search.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.SearchConsts;
import com.chestnut.search.domain.DictWord;
import com.chestnut.search.domain.dto.CreateDictWordRequest;
import com.chestnut.search.domain.dto.WordAnalyzeRequest;
import com.chestnut.search.fixed.dict.SearchDictWordType;
import com.chestnut.search.service.IDictWordService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.Dict;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@XComment("{API.DOC.SEARCH.DICT_MODULE}")
@RequiredArgsConstructor
@RestController
@RequestMapping("/search/dict")
public class DictWordController extends BaseRestController {

	private final IDictWordService dictWordService;

	private final ElasticsearchClient esClient;

	@XComment("{API.DOC.SEARCH.DICT_GET_LIST}")
	@Priv(type = AdminUserType.TYPE, value = SearchConsts.SearchPriv.DICT_VIEW)
	@GetMapping
	public R<TableData<DictWord>> getPageList(@RequestParam(required = false) @Length(max = 100) @XComment("{API.DOC.SEARCH.QUERY}") String query) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<DictWord> q = new LambdaQueryWrapper<DictWord>().like(StringUtils.isNotEmpty(query),
				DictWord::getWord, query);
		Page<DictWord> page = this.dictWordService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.SEARCH.DICT_ADD}")
	@Log(title = "新增检索词", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = SearchConsts.SearchPriv.DICT_VIEW)
	@PostMapping
	public R<Void> add(@RequestBody @Validated CreateDictWordRequest req) {
		this.dictWordService.batchAddDictWords(req);
		return R.ok();
	}

	@XComment("{API.DOC.SEARCH.DICT_DELETE}")
	@Log(title = "删除检索词", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = SearchConsts.SearchPriv.DICT_VIEW)
	@PostMapping("/delete")
	public R<Void> delete(@RequestBody @NotEmpty @XComment("{API.DOC.SEARCH.DICT_WORD_IDS}") List<Long> dictWordIds) {
		this.dictWordService.removeByIds(dictWordIds);
		return R.ok();
	}

	@XComment("{API.DOC.SEARCH.DICT_IK_CHECK}")
	@RequestMapping(value = "/ik/{type}", method = RequestMethod.HEAD)
	public void checkDictNewest(@PathVariable("type") @NotEmpty @XComment("{API.DOC.SEARCH.DICT_WORD_TYPE}") String type,
								HttpServletResponse response) {
		String lastModified = this.dictWordService.getLastModified(type);
		response.setHeader("Last-Modified", StringUtils.isEmpty(lastModified) ? "0" : lastModified);
	}

	@XComment("{API.DOC.SEARCH.DICT_IK_WORDS}")
	@RequestMapping(value = "/ik/{type}", method = RequestMethod.GET, produces = { "text/html;charset=utf-8" })
	public String dictNewest(@PathVariable("type") @NotBlank @Dict(SearchDictWordType.TYPE) @XComment("{API.DOC.SEARCH.DICT_WORD_TYPE}") String type) {
		String words = this.dictWordService.lambdaQuery().eq(DictWord::getWordType, type).list().stream()
				.map(DictWord::getWord).collect(Collectors.joining("\n"));
		return words;
	}

	@Priv(type = AdminUserType.TYPE, value = SearchConsts.SearchPriv.DICT_VIEW)
	@XComment("{API.DOC.SEARCH.DICT_ANALYZE}")
	@PostMapping("/analyze")
	public R<?> wordAnalyze(@RequestBody WordAnalyzeRequest req) throws IOException {
		AnalyzeRequest analyzeRequest = new AnalyzeRequest.Builder().analyzer(req.getType()).text(req.getText()).build();
		AnalyzeResponse analyzeResponse = this.esClient.indices().analyze(analyzeRequest);
		String result = analyzeResponse.tokens().stream().map(
				token -> token.token() + "/" + token.type()
		).collect(Collectors.joining(", "));
		return R.ok(result);
	}
}
