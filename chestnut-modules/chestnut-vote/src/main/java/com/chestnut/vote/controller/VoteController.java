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
package com.chestnut.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.validator.LongId;
import com.chestnut.vote.core.IVoteItemType;
import com.chestnut.vote.core.IVoteUserType;
import com.chestnut.vote.domain.Vote;
import com.chestnut.vote.domain.dto.CreateVoteRequest;
import com.chestnut.vote.domain.dto.UpdateVoteRequest;
import com.chestnut.vote.permission.VotePriv;
import com.chestnut.vote.service.IVoteService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/vote")
public class VoteController extends BaseRestController {

	private final IVoteService voteService;

	private final List<IVoteUserType> userTypes;

	private final List<IVoteItemType> itemTypes;

	@Priv(type = AdminUserType.TYPE, value = VotePriv.View)
	@GetMapping
	public R<?> getPageList(@RequestParam(required = false) String title, @RequestParam(required = false) String status) {
		PageRequest pr = this.getPageRequest();
		Page<Vote> page = this.voteService.lambdaQuery().like(StringUtils.isNotEmpty(title), Vote::getTitle, title)
				.eq(StringUtils.isNotEmpty(status), Vote::getStatus, status)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@Priv(type = AdminUserType.TYPE, value = VotePriv.View)
	@GetMapping("/{voteId}")
	public R<?> getVoteDetail(@PathVariable @LongId Long voteId) {
		Vote vote = this.voteService.getById(voteId);
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteId));
		return R.ok(vote);
	}

	@Priv(type = AdminUserType.TYPE, value = VotePriv.View)
	@GetMapping("/userTypes")
	public R<?> getVoteUserTypes() {
		return bindSelectOptions(this.userTypes, IVoteUserType::getId, IVoteUserType::getName);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/item/types")
	public R<?> getVoteItemTypes() {
		return bindSelectOptions(this.itemTypes, IVoteItemType::getId, IVoteItemType::getName);
	}

	@Log(title = "新增问卷调查", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = VotePriv.Add)
	@PostMapping
	public R<?> add(@RequestBody @Validated CreateVoteRequest req) {
		this.voteService.addVote(req);
		return R.ok();
	}

	@Log(title = "编辑问卷调查", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { VotePriv.Add, VotePriv.Edit })
	@PutMapping
	public R<?> update(@RequestBody @Validated UpdateVoteRequest req) {
		this.voteService.updateVote(req);
		return R.ok();
	}

	@Log(title = "删除问卷调查", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = VotePriv.Delete)
	@PostMapping("/delete")
	public R<String> delete(@RequestBody @NotEmpty List<Long> dictWordIds) {
		this.voteService.deleteVotes(dictWordIds);
		return R.ok();
	}
}
