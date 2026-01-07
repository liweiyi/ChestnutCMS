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
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import com.chestnut.word.domain.HotWord;
import com.chestnut.word.domain.dto.CreateHotWordRequest;
import com.chestnut.word.domain.dto.UpdateHotWordRequest;
import com.chestnut.word.permission.WordPriv;
import com.chestnut.word.service.IHotWordService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  热词词前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/hotword")
public class HotWordController extends BaseRestController {
    
	private final IHotWordService hotWordService;
    
    @GetMapping("/list")
    public R<?> getPageList(@RequestParam("groupId") @LongId Long groupId,
    		@RequestParam(required = false) @Length(max = 255) String query) {
    	PageRequest pr = this.getPageRequest();
    	Page<HotWord> page = this.hotWordService.lambdaQuery().eq(HotWord::getGroupId, groupId)
				.like(StringUtils.isNotEmpty(query), HotWord::getWord, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
    	return this.bindDataTable(page);
    }

	@PostMapping("/add")
	public R<?> add(@RequestBody @Validated CreateHotWordRequest req) {
    	this.hotWordService.addHotWord(req);
    	return R.ok();
	}

	@PostMapping("/update")
	public R<String> edit(@RequestBody @Validated UpdateHotWordRequest req) {
		this.hotWordService.editHotWord(req);
    	return R.ok();
	}

	@PostMapping("/delete")
	public R<String> remove(@RequestBody @NotEmpty List<Long> hotWordIds) {
		this.hotWordService.deleteHotWords(hotWordIds);
		return R.ok();
	}
}

