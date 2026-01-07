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
import com.chestnut.system.validator.LongId;
import com.chestnut.word.domain.TagWord;
import com.chestnut.word.domain.dto.BatchAddTagRequest;
import com.chestnut.word.domain.dto.CreateTagWordRequest;
import com.chestnut.word.domain.dto.UpdateTagWordRequest;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.ITagWordService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * TAG词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/tagword")
public class TagWordController extends BaseRestController {

	private final ITagWordService tagWordService;

	@GetMapping("/list")
	public R<?> getPageList(@RequestParam("groupId") @LongId Long groupId,
			@RequestParam(required = false) @Length(max = 255) String query) {
		PageRequest pr = this.getPageRequest();
		Page<TagWord> page = this.tagWordService.lambdaQuery().eq(TagWord::getGroupId, groupId)
				.like(StringUtils.isNotEmpty(query), TagWord::getWord, query)
				.orderByAsc(TagWord::getSortFlag)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@PostMapping("/add")
	public R<?> add(@RequestBody @Validated CreateTagWordRequest req) {
		this.tagWordService.addTagWord(req);
		return R.ok();
	}

	@PostMapping("/batchAdd")
	public R<?> batchAdd(@RequestBody @Validated BatchAddTagRequest req) {
		this.tagWordService.batchAddTagWord(req);
		return R.ok();
	}

	@PostMapping("/update")
	public R<?> edit(@RequestBody @Validated UpdateTagWordRequest req) {
		this.tagWordService.editTagWord(req);
		return R.ok();
	}

	@PostMapping("/delete")
	public R<?> remove(@RequestBody @NotEmpty List<Long> tagWordIds) {
		this.tagWordService.deleteTagWords(tagWordIds);
		return R.ok();
	}
}
