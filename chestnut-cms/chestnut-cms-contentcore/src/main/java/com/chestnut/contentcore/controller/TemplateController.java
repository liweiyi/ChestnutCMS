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
import cn.dev33.satoken.exception.NotPermissionException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.extend.annotation.XssIgnore;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.staticize.StaticizeService;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.DateUtils;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.common.utils.file.FileExUtils;
import com.chestnut.common.validation.RegexConsts;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.CmsTemplate;
import com.chestnut.contentcore.domain.dto.TemplateAddDTO;
import com.chestnut.contentcore.domain.dto.TemplateRenameDTO;
import com.chestnut.contentcore.domain.dto.TemplateUpdateDTO;
import com.chestnut.contentcore.domain.vo.TemplateListVO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.config.TemplateSuffix;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.perms.SitePermissionType;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.ITemplateService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.CmsRestController;
import com.chestnut.contentcore.util.SiteUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 模板管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/template")
@RequiredArgsConstructor
public class TemplateController extends CmsRestController {

	private final ITemplateService templateService;

	private final ISiteService siteService;

	private final StaticizeService staticizeService;

	/**
	 * 模板数据集合
	 */
	@Priv(
			type = AdminUserType.TYPE,
			value = { CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@GetMapping("/list")
	public R<?> getTemplateList(@RequestParam(value = "publishPipeCode", required = false) String publishPipeCode,
								@RequestParam(value = "siteId", required = false, defaultValue = "0") Long siteId,
								@RequestParam(value = "filename", required = false) String filename,
								HttpServletRequest request) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = siteService.getSiteOrCurrent(siteId, request, StpAdminUtil.getLoginUser());
		this.templateService.scanTemplates(site);
		Page<CmsTemplate> page = this.templateService.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId())
				.eq(StringUtils.isNotEmpty(publishPipeCode), CmsTemplate::getPublishPipeCode, publishPipeCode)
				.like(StringUtils.isNotEmpty(filename), CmsTemplate::getPath, filename)
				.orderByAsc(CmsTemplate::getPath)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		List<TemplateListVO> list = page.getRecords().stream().map(t -> TemplateListVO.builder()
				.templateId(t.getTemplateId())
				.path(t.getPath())
				.publishPipeCode(t.getPublishPipeCode())
				.siteId(t.getSiteId())
				.filesize(t.getFilesize())
				.filesizeName(FileUtils.byteCountToDisplaySize(t.getFilesize()))
				.modifyTime(DateUtils.epochMilliToLocalDateTime(t.getModifyTime()))
				.remark(t.getRemark())
				.build()).toList();
		return this.bindDataTable(list, (int) page.getTotal());
	}

	/**
	 * 获取模板详情
	 */
	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@GetMapping("/detail/{templateId}")
	public R<?> getTemplateDetail(@PathVariable("templateId") @LongId Long templateId) {
		CmsSite site = this.getCurrentSite();
		this.templateService.scanTemplates(site);

		CmsTemplate template = this.templateService.getById(templateId);
		Assert.notNull(template, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", templateId));

		Assert.isTrue(Objects.equals(template.getSiteId(), site.getSiteId()),
				() -> new NotPermissionException(SitePermissionType.SitePrivItem.View.getPermissionKey(site.getSiteId())));
		return R.ok(template);
	}

	/**
	 * 新增模板文件
	 */
	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@Log(title = "新增模板", businessType = BusinessType.INSERT)
	@XssIgnore
	@PostMapping("/add")
	public R<?> add(@RequestBody @Validated TemplateAddDTO dto) throws IOException {
		validTemplateName(dto.getPath());
		CmsSite site = this.getCurrentSite();
		dto.setSiteId(site.getSiteId());
		this.templateService.addTemplate(dto);
		return R.ok();
	}

	/**
	 * 重命名模板文件
	 */
	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@Log(title = "重命名模板", businessType = BusinessType.UPDATE)
	@PostMapping("/rename")
	public R<?> rename(@RequestBody @Validated TemplateRenameDTO dto) throws IOException {
        validTemplateName(dto.getPath());
		CmsTemplate template = this.templateService.getById(dto.getTemplateId());
		Assert.notNull(template,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", dto.getTemplateId()));

		CmsSite site = this.getCurrentSite();
		Assert.isTrue(Objects.equals(template.getSiteId(), site.getSiteId()),
				() -> new NotPermissionException(SitePermissionType.SitePrivItem.View.getPermissionKey(site.getSiteId())));

		this.templateService.renameTemplate(template, dto.getPath(), dto.getRemark(), StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	private static void validTemplateName(String fileName) {
		String suffix = TemplateSuffix.getValue();
		if (StringUtils.isEmpty(fileName) || !fileName.endsWith(suffix)) {
			throw ContentCoreErrorCode.INVALID_TEMPLATE_NAME.exception(suffix);
		}
		fileName = FileExUtils.normalizePath(fileName);
		String[] split = fileName.substring(0, fileName.indexOf(suffix)).split("/");
		for (String item : split) {
			if (StringUtils.isEmpty(item) || !Pattern.matches(RegexConsts.REGEX_CODE, item)) {
                throw ContentCoreErrorCode.INVALID_TEMPLATE_NAME.exception(suffix);
			}
		}
	}

	/**
	 * 修改模板文件内容
	 */
	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@Log(title = "编辑模板", businessType = BusinessType.UPDATE)
	@XssIgnore
	@PostMapping("/update")
	public R<?> save(@RequestBody @Validated TemplateUpdateDTO dto) throws IOException {
		CmsTemplate template = this.templateService.getById(dto.getTemplateId());
		Assert.notNull(template,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", dto.getTemplateId()));

		CmsSite site = this.getCurrentSite();
		Assert.isTrue(Objects.equals(template.getSiteId(), site.getSiteId()),
				() -> new NotPermissionException(SitePermissionType.SitePrivItem.View.getPermissionKey(site.getSiteId())));

		this.templateService.saveTemplate(template, dto);
		return R.ok();
	}

	/**
	 * 删除模板文件
	 */
	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@Log(title = "删除模板", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<?> delete(@RequestBody @NotEmpty List<Long> templateIds) throws IOException {
		Assert.isTrue(IdUtils.validate(templateIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("templateIds"));
		CmsSite site = this.getCurrentSite();
		this.templateService.deleteTemplates(site, templateIds);
		return R.ok();
	}

	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@Log(title = "清理模板缓存", businessType = BusinessType.OTHER)
	@PostMapping("/clearTemplateCache")
	public R<?> clearTemplateCache() {
		this.staticizeService.clearTemplateCache();
		return R.ok();
	}

	@Priv(
			type = AdminUserType.TYPE,
			value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
			mode = SaMode.AND
	)
	@Log(title = "清理区块缓存", businessType = BusinessType.OTHER)
	@PostMapping("/clearIncludeCache")
	public R<?> clearIncludeCache(@RequestBody @NotEmpty List<Long> templateIds) {
		this.templateService.listByIds(templateIds).forEach(template -> {
			CmsSite site = this.siteService.getSite(template.getSiteId());
			String templateKey = SiteUtils.getTemplateKey(site, template.getPublishPipeCode(), template.getPath());
			this.templateService.clearTemplateStaticContentCache(templateKey);
		});
		return R.ok();
	}
}
