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
package com.chestnut.vote.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.Assert;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import com.chestnut.vote.domain.VoteSubject;
import com.chestnut.vote.domain.VoteSubjectItem;
import com.chestnut.vote.domain.dto.SaveSubjectItemsDTO;
import com.chestnut.vote.permission.VotePriv;
import com.chestnut.vote.service.IVoteSubjectItemService;
import com.chestnut.vote.service.IVoteSubjectService;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/vote/subject")
public class VoteSubjectController extends BaseRestController {

	private final IVoteSubjectService voteSubjectService;

	private final IVoteSubjectItemService voteSubjectItemService;

	@Priv(type = AdminUserType.TYPE, value = VotePriv.View)
	@GetMapping
	public R<?> getVoteSubjects(@RequestParam @LongId Long voteId) {
		List<VoteSubject> subjects = this.voteSubjectService.getVoteSubjectList(voteId);
		return this.bindDataTable(subjects);
	}

	@Priv(type = AdminUserType.TYPE, value = VotePriv.View)
	@GetMapping("/{subjectId}")
	public R<?> getVoteSubjectDetail(@PathVariable @LongId Long subjectId) {
		VoteSubject subject = this.voteSubjectService.getById(subjectId);
		Assert.notNull(subject, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", subjectId));
		return R.ok(subject);
	}

	@Log(title = "新增调查主题", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = { VotePriv.Add, VotePriv.Edit })
	@PostMapping
	public R<?> add(@RequestBody @Validated VoteSubject voteSubject) {
		voteSubject.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteSubjectService.addVoteSubject(voteSubject);
		return R.ok();
	}

	@Log(title = "编辑调查主题", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { VotePriv.Add, VotePriv.Edit })
	@PutMapping
	public R<?> update(@RequestBody @Validated VoteSubject voteSubject) {
		voteSubject.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.voteSubjectService.updateVoteSubject(voteSubject);
		return R.ok();
	}

	@Log(title = "删除调查主题", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { VotePriv.Add, VotePriv.Edit })
	@DeleteMapping
	public R<String> delete(@RequestBody @NotEmpty List<Long> subjectIds) {
		this.voteSubjectService.deleteVoteSubjects(subjectIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = VotePriv.View)
	@GetMapping("/items/{subjectId}")
	public R<?> getSubjectItems(@PathVariable @Min(1) Long subjectId) {
		List<VoteSubjectItem> list = voteSubjectItemService.lambdaQuery().eq(VoteSubjectItem::getSubjectId, subjectId)
				.orderByAsc(VoteSubjectItem::getSortFlag).list();
		return this.bindDataTable(list);
	}

	@Log(title = "保存调查主题选项", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { VotePriv.Add, VotePriv.Edit })
	@PostMapping("/items")
	public R<?> saveSubjectItems(@RequestBody SaveSubjectItemsDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.voteSubjectService.saveSubjectItems(dto);
		return R.ok();
	}
}
