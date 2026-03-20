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
package com.chestnut.word.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.word.domain.ErrorProneWord;
import com.chestnut.word.domain.dto.CreateErrorProneWordRequest;
import com.chestnut.word.domain.dto.UpdateErrorProneWordRequest;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.IErrorProneWordService;


import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 易错词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.WORD.ERROR_PRONE_WORD.MODULE}")
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/errorproneword")
public class ErrorProneWordController extends BaseRestController {

	private final IErrorProneWordService errorProneWordService;

	@XComment("{API.DOC.WORD.ERROR_PRONE_WORD.GET_LIST}")
	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/list")
	public R<TableData<ErrorProneWord>> getPageList(@RequestParam(required = false) @Length(max = 255) @XComment("{API.DOC.WORD.ERROR_PRONE_WORD.QUERY}") String query) {
		PageRequest pr = this.getPageRequest();
		Page<ErrorProneWord> page = this.errorProneWordService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), ErrorProneWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@XComment("{API.DOC.WORD.ERROR_PRONE_WORD.CREATE}")
	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/add")
	public R<Void> add(@RequestBody @Validated CreateErrorProneWordRequest req) {
		this.errorProneWordService.addErrorProneWord(req);
		return R.ok();
	}

	@XComment("{API.DOC.WORD.ERROR_PRONE_WORD.UPDATE}")
	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/update")
	public R<Void> edit(@RequestBody @Validated UpdateErrorProneWordRequest req) {
		this.errorProneWordService.updateErrorProneWord(req);
		return R.ok();
	}

	@XComment("{API.DOC.WORD.ERROR_PRONE_WORD.DELETE}")
	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/delete")
	public R<Void> remove(@RequestBody @NotEmpty @XComment("{API.DOC.WORD.ERROR_PRONE_WORD.WORD_IDS}") List<Long> errorProneWordIds) {
		this.errorProneWordService.removeByIds(errorProneWordIds);
		return R.ok();
	}

	@XComment("{API.DOC.WORD.ERROR_PRONE_WORD.CHECK}")
	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/check")
	public R<?> check(@RequestBody @XComment("{API.DOC.WORD.ERROR_PRONE_WORD.TEXT}") String text) {
		Map<String, String> map = this.errorProneWordService.check(text);
		return R.ok(map.entrySet().stream().map(e -> Map.of("w", e.getKey(), "r", e.getValue())).toList());
	}
}
