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
package com.chestnut.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.extend.annotation.XssIgnore;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.system.domain.SysNotice;
import com.chestnut.system.domain.dto.CreateNoticeRequest;
import com.chestnut.system.domain.dto.QueryNoticeRequest;
import com.chestnut.system.domain.dto.UpdateNoticeRequest;
import com.chestnut.system.permission.SysMenuPriv;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.service.ISysNoticeService;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告 信息操作处理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseRestController {

	private final ISysNoticeService noticeService;

	/**
	 * 获取通知公告列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysNoticeList)
	@GetMapping("/list")
	public R<?> list(@Validated QueryNoticeRequest req) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysNotice> q = new LambdaQueryWrapper<SysNotice>()
				.like(StringUtils.isNotEmpty(req.getNoticeTitle()), SysNotice::getNoticeTitle, req.getNoticeTitle())
				.like(StringUtils.isNotEmpty(req.getNoticeType()), SysNotice::getNoticeType, req.getNoticeType())
				.eq(StringUtils.isNotEmpty(req.getCreateBy()), SysNotice::getCreateBy, req.getCreateBy())
				.orderByDesc(SysNotice::getNoticeId);
		Page<SysNotice> page = noticeService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	/**
	 * 根据通知公告编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysNoticeList)
	@GetMapping(value = "/{noticeId}")
	public R<?> getInfo(@PathVariable @LongId Long noticeId) {
		Assert.isTrue(IdUtils.validate(noticeId), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("noticeId: " + noticeId));
		return R.ok(noticeService.getById(noticeId));
	}

	/**
	 * 新增通知公告
	 */
	@XssIgnore
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysNoticeAdd)
	@Log(title = "通知公告", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody CreateNoticeRequest req) {
		noticeService.insertNotice(req);
		return R.ok();
	}

	/**
	 * 修改通知公告
	 */
	@XssIgnore
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysNoticeEdit)
	@Log(title = "通知公告", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody UpdateNoticeRequest req) {
		noticeService.updateNotice(req);
		return R.ok();
	}

	/**
	 * 删除通知公告
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysNoticeRemove)
	@Log(title = "通知公告", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> remove(@RequestBody @NotEmpty List<Long> noticeIds) {
		noticeService.deleteNoticeByIds(noticeIds);
		return R.ok();
	}
}
