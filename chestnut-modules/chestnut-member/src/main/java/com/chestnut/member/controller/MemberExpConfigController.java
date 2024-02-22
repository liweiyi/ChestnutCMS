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
package com.chestnut.member.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.member.domain.MemberExpConfig;
import com.chestnut.member.domain.vo.ExpOperationVO;
import com.chestnut.member.level.IExpOperation;
import com.chestnut.member.level.ILevelType;
import com.chestnut.member.permission.MemberPriv;
import com.chestnut.member.service.IMemberExpConfigService;
import com.chestnut.member.service.IMemberLevelConfigService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberExp)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member/expConfig")
public class MemberExpConfigController extends BaseRestController {

	private final IMemberLevelConfigService memberLevelConfigService;

	private final IMemberExpConfigService memberExpOperationService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "opType", required = false) String opType,
			@RequestParam(value = "levelType", required = false) String levelType) {
		PageRequest pr = this.getPageRequest();
		Page<MemberExpConfig> page = this.memberExpOperationService.lambdaQuery()
				.eq(StringUtils.isNotEmpty(opType), MemberExpConfig::getOpType, opType)
				.eq(StringUtils.isNotEmpty(levelType), MemberExpConfig::getLevelType, levelType)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));

		page.getRecords().forEach(conf -> {
			IExpOperation expOperation = this.memberExpOperationService.getExpOperation(conf.getOpType());
			conf.setOpTypeName(I18nUtils.get(expOperation.getName()));
			ILevelType lt = this.memberLevelConfigService.getLevelType(conf.getLevelType());
			conf.setLevelTypeName(I18nUtils.get(lt.getName()));
		});
		return this.bindDataTable(page);
	}

	@GetMapping("/{expOperationId}")
	public R<?> getExpOperationDetail(@PathVariable("expOperationId") @LongId Long expOperationId) {
		MemberExpConfig conf = this.memberExpOperationService.getById(expOperationId);
		Assert.notNull(conf, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("id", expOperationId));
		IExpOperation expOperation = this.memberExpOperationService.getExpOperation(conf.getOpType());
		conf.setOpTypeName(I18nUtils.get(expOperation.getName()));
		return R.ok(conf);
	}

	@GetMapping("/types")
	public R<?> getOperationTypes() {
		List<ExpOperationVO> list = this.memberExpOperationService.getExpOperations().values().stream()
				.map(op -> new ExpOperationVO(op.getId(), I18nUtils.get(op.getName()))).toList();
		return R.ok(list);
	}

	@Log(title = "新增会员经验配置", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addMemberExpOperation(@RequestBody @Validated MemberExpConfig expOp) {
		expOp.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		this.memberExpOperationService.addExpOperation(expOp);
		return R.ok();
	}

	@Log(title = "编辑会员经验配置", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> updateMemberExpOperation(@RequestBody @Validated MemberExpConfig expOp) {
		expOp.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		this.memberExpOperationService.updateExpOperation(expOp);
		return R.ok();
	}

	@Log(title = "删除会员经验配置", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteExpOperations(@RequestBody @NotEmpty List<Long> expOperationIds) {
		this.memberExpOperationService.deleteExpOperations(expOperationIds);
		return R.ok();
	}
}