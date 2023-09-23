package com.chestnut.search.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.AnalyzeRequest;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.JacksonUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.search.domain.DictWord;
import com.chestnut.search.domain.dto.DictWordDTO;
import com.chestnut.search.service.IDictWordService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/search/dict")
public class DictWordController extends BaseRestController {

	private final IDictWordService dictWordService;

	private final ElasticsearchClient esClient;

	@Priv(type = AdminUserType.TYPE)
	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<DictWord> q = new LambdaQueryWrapper<DictWord>().like(StringUtils.isNotEmpty(query),
				DictWord::getWord, query);
		Page<DictWord> page = this.dictWordService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增检索词", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE)
	@PostMapping
	public R<?> add(@RequestBody @Validated DictWordDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.dictWordService.batchAddDictWords(dto);
		return R.ok();
	}

	@Log(title = "删除检索词", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE)
	@DeleteMapping
	public R<String> delete(@RequestBody @NotEmpty List<Long> dictWordIds) {
		this.dictWordService.removeByIds(dictWordIds);
		return R.ok();
	}

	/**
	 * 检查词库是否有变更
	 *
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "/ik/{type}", method = RequestMethod.HEAD)
	public void checkDictNewest(@PathVariable("type") @NotEmpty String type, HttpServletRequest request,
								HttpServletResponse response) {
		String lastModified = this.dictWordService.getLastModified(type);
		response.setHeader("Last-Modified", StringUtils.isEmpty(lastModified) ? "0" : lastModified);
	}

	/**
	 * IK热更词库API
	 *
	 * @return 词库字符串，每行一个词
	 */
	@RequestMapping(value = "/ik/{type}", method = RequestMethod.GET, produces = { "text/html;charset=utf-8" })
	public String dictNewest(@PathVariable("type") @NotEmpty String type) {
		String words = this.dictWordService.lambdaQuery().eq(DictWord::getWordType, type).list().stream()
				.map(DictWord::getWord).collect(Collectors.joining("\n"));
		return words;
	}

	/**
	 * 分词测试
	 *
	 * @param text
	 * @return
	 */
	@PostMapping("/analyze")
	public R<?> wordAnalyze(@RequestBody String text) throws IOException {
		AnalyzeRequest analyzeRequest = new AnalyzeRequest.Builder().analyzer("ik_smart").text(text).build();
		AnalyzeResponse analyzeResponse = this.esClient.indices().analyze(analyzeRequest);
		String result = analyzeResponse.tokens().stream().map(
				token -> token.token() + "/" + token.type()
		).collect(Collectors.joining(", "));
		return R.ok(result);
	}
}
