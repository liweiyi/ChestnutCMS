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
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.ServletUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IResourceType;
import com.chestnut.contentcore.core.impl.InternalDataType_Resource;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ResourceUploadDTO;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.config.ResourceUploadAcceptSize;
import com.chestnut.contentcore.perms.ContentCorePriv;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CmsPrivUtils;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;
import com.chestnut.system.validator.LongId;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 素材库管理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.ResourceView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
)
@RestController
@RequestMapping("/cms/resource")
@RequiredArgsConstructor
public class ResourceController extends BaseRestController {

	private final ISiteService siteService;

	private final IResourceService resourceService;

	@GetMapping("/types")
	public R<?> getResourceTypes() {
		List<Map<String, String>> list = ContentCoreUtils.getResourceTypes().stream()
				.map(rt -> Map.of(
						"id", rt.getId(),
						"name", I18nUtils.get(rt.getName()),
						"accepts", StringUtils.join(rt.getUsableSuffix(), ","))
				).toList();
		return R.ok(list);
	}

	@GetMapping
	public R<?> listData(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "resourceType", required = false) String resourceType,
			@RequestParam(value = "owner", required = false, defaultValue = "false") boolean owner,
		    @RequestParam(value = "siteId", required = false, defaultValue = "0") Long siteId,
			@RequestParam(value = "beginTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime,
			@RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
			HttpServletRequest request) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = siteService.getSiteOrCurrent(siteId, request);
		LambdaQueryWrapper<CmsResource> q = new LambdaQueryWrapper<CmsResource>()
				.eq(CmsResource::getSiteId, site.getSiteId())
				.like(StringUtils.isNotEmpty(name), CmsResource::getFileName, name)
				.eq(StringUtils.isNotEmpty(resourceType), CmsResource::getResourceType, resourceType)
				.eq(owner, CmsResource::getCreateBy, StpAdminUtil.getLoginUser().getUsername())
				.ge(beginTime != null, CmsResource::getCreateTime, beginTime)
				.le(endTime != null, CmsResource::getCreateTime, endTime).orderByDesc(CmsResource::getResourceId);
		Page<CmsResource> page = resourceService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		if (!page.getRecords().isEmpty()) {
			page.getRecords().forEach(r -> {
				IResourceType rt = ContentCoreUtils.getResourceType(r.getResourceType());
				r.setResourceTypeName(I18nUtils.get(rt.getName()));
				if (r.getPath().startsWith("http://") || r.getPath().startsWith("https://")) {
					r.setSrc(r.getPath());
				} else {
					String resourceLink = this.resourceService.getResourceLink(r, null, true);
					r.setSrc(resourceLink);
				}
				r.setInternalUrl(InternalDataType_Resource.getInternalUrl(r));
				r.setFileSizeName(FileUtils.byteCountToDisplaySize(r.getFileSize()));
			});
		}
		return bindDataTable(page);
	}

	@GetMapping("/{resourceId}")
	public R<CmsResource> getInfo(@PathVariable("resourceId") @LongId Long resourceId) {
		CmsResource resource = this.resourceService.getById(resourceId);
		if (resource == null) {
			return R.fail("资源ID错误：" + resourceId);
		}
		return R.ok(resource);
	}

	@Log(title = "新增/编辑素材", businessType = BusinessType.INSERT)
	@PostMapping
	public R<CmsResource> addResource(
			@RequestParam("file") MultipartFile resourceFile,
			Long resourceId,
			String name,
			String remark
	) {
		Assert.isFalse(resourceFile.isEmpty(), () -> CommonErrorCode.NOT_EMPTY.exception("file"));
		// 文件大小校验
		Long acceptSize = ResourceUploadAcceptSize.getValue();
		Assert.isTrue(acceptSize <= 0 || acceptSize >= resourceFile.getSize(), ContentCoreErrorCode.RESOURCE_ACCEPT_SIZE_LIMIT::exception);
		try {
			CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
			ResourceUploadDTO dto = ResourceUploadDTO.builder()
					.resourceId(resourceId)
					.site(site)
					.file(resourceFile)
					.name(name)
					.remark(remark)
					.build();
			dto.setOperator(StpAdminUtil.getLoginUser());
			CmsResource resource;
			if (IdUtils.validate(resourceId)) {
				resource = this.resourceService.editResource(dto);
			} else {
				resource = this.resourceService.addResource(dto);
			}
			return R.ok(resource);
		} catch (IOException e1) {
			throw CommonErrorCode.SYSTEM_ERROR.exception(e1.getMessage());
		}
	}

	@Log(title = "删除素材", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<String> delResources(@RequestBody @NotEmpty List<Long> resourceIds) {
		Assert.notEmpty(resourceIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("resourceIds"));
		this.resourceService.deleteResource(resourceIds);
		return R.ok();
	}

	@Log(title = "上传素材", businessType = BusinessType.INSERT)
	@PostMapping("/upload")
	public R<CmsResource> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws Exception {
		Assert.notNull(multipartFile, () -> CommonErrorCode.NOT_EMPTY.exception("file"));
		// 文件大小校验
		Long acceptSize = ResourceUploadAcceptSize.getValue();
		Assert.isTrue(acceptSize <= 0 || acceptSize >= multipartFile.getSize(), ContentCoreErrorCode.RESOURCE_ACCEPT_SIZE_LIMIT::exception);

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		ResourceUploadDTO dto = ResourceUploadDTO.builder().site(site).file(multipartFile).build();
		dto.setOperator(StpAdminUtil.getLoginUser());
		CmsResource resource = this.resourceService.addResource(dto);
		resource.setSrc(InternalUrlUtils.getActualPreviewUrl(resource.getInternalUrl()));
		return R.ok(resource);
	}

	@GetMapping("/downlad/{resourceId}")
	public void downloadResourceFile(@PathVariable @LongId Long resourceId, HttpServletResponse response) {
		CmsResource resource = this.resourceService.getById(resourceId);
		Assert.notNull(resource, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("resourceId", resourceId));
		this.resourceService.downloadResource(resource, response);
	}
}