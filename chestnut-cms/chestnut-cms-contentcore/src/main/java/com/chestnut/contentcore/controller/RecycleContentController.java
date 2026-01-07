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
package com.chestnut.contentcore.controller;

import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.BCmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内容回收站管理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(
	type = AdminUserType.TYPE,
	value = { ContentCorePriv.ContentView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
	mode = SaMode.AND
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/content/recycle")
public class RecycleContentController extends CmsRestController {

	private final IContentService contentService;

	@GetMapping
	public R<?> getRecycleContentList(@RequestParam(name = "catalogId", required = false) Long catalogId,
									  @RequestParam(name = "contentType", required = false) String contentType,
									  @RequestParam(name = "title", required = false) String title,
									  @RequestParam(name = "status", required = false) String status) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.getCurrentSite();

		LambdaQueryWrapper<BCmsContent> q = new LambdaQueryWrapper<BCmsContent>().eq(BCmsContent::getSiteId, site.getSiteId())
				.eq(IdUtils.validate(catalogId), BCmsContent::getCatalogId, catalogId)
				.eq(StringUtils.isNotEmpty(contentType), BCmsContent::getContentType, contentType)
				.like(StringUtils.isNotEmpty(title), BCmsContent::getTitle, title)
				.eq(StringUtils.isNotEmpty(status), BCmsContent::getStatus, status);
		Page<BCmsContent> page = this.contentService.dao().getBackupMapper()
				.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "恢复回收站内容", businessType = BusinessType.INSERT)
	@PostMapping("/recover")
	public R<?> recoverContent(@RequestBody @NotEmpty List<Long> backupIds) {
		this.contentService.recoverContents(backupIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@Log(title = "删除回收站内容", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> deleteRecycleContents(@RequestBody @NotEmpty List<Long> backupIds) {
		this.contentService.deleteRecycleContents(backupIds);
		return R.ok();
	}
}
