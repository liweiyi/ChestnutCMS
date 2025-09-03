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
package com.chestnut.word.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.word.domain.SensitiveWord;
import com.chestnut.word.domain.dto.CreateSensitiveWordRequest;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.ISensitiveWordService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * 敏感词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/sensitiveword")
public class SensitiveWordController extends BaseRestController {

	private final ISensitiveWordService sensitiveWordService;

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@GetMapping("/list")
	public R<?> getPageList(@RequestParam(required = false) @Length(max = 255) String query) {
		PageRequest pr = this.getPageRequest();
		Page<SensitiveWord> page = this.sensitiveWordService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), SensitiveWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/add")
	public R<?> add(@RequestBody @Validated CreateSensitiveWordRequest req) {
		this.sensitiveWordService.addWord(req);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
	@PostMapping("/delete")
	public R<?> remove(@RequestBody @NotEmpty List<Long> sensitiveWordIds) {
		this.sensitiveWordService.deleteWord(sensitiveWordIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@PostMapping("/check")
	public R<?> check(@RequestBody @NotBlank String text) {
		Set<String> words = this.sensitiveWordService.check(text);
		return R.ok(words);
	}
}
