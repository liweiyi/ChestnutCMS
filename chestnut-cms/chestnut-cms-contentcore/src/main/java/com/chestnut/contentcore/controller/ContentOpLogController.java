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
package com.chestnut.contentcore.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContentOpLog;
import com.chestnut.contentcore.service.IContentOpLogService;
import com.chestnut.system.security.AdminUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容操作日志管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/content/log")
public class ContentOpLogController extends BaseRestController {

	private final IContentOpLogService contentOpLogService;

	@GetMapping
	public R<?> getContentOpLogPageList(@RequestParam Long contentId,
										@RequestParam(required = false) String type,
										@RequestParam(required = false) String operator) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<CmsContentOpLog> q = new LambdaQueryWrapper<CmsContentOpLog>()
				.eq(CmsContentOpLog::getContentId, contentId)
				.eq(StringUtils.isNotEmpty(type), CmsContentOpLog::getType, type)
				.eq(StringUtils.isNotEmpty(operator), CmsContentOpLog::getOperator, operator)
				.orderByDesc(CmsContentOpLog::getLogId);
		Page<CmsContentOpLog> pageResult = contentOpLogService.page(
				new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q
		);
		return this.bindDataTable(pageResult);
	}
}
