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
package com.chestnut.advertisement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.advertisement.IAdvertisementType;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.permission.CmsAdvertisementPriv;
import com.chestnut.advertisement.pojo.dto.AdvertisementDTO;
import com.chestnut.advertisement.pojo.vo.AdvertisementVO;
import com.chestnut.advertisement.service.IAdvertisementService;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.i18n.I18nUtils;
import com.chestnut.common.log.annotation.Log;
import com.chestnut.common.log.enums.BusinessType;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.PageRequest;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.system.security.AdminUserType;
import com.chestnut.system.security.StpAdminUtil;


import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 广告前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CMS.ADVERTISEMENT.MODULE}")
@Priv(type = AdminUserType.TYPE, value = CmsAdvertisementPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/advertisement")
public class AdvertisementController extends BaseRestController {

	private final IAdvertisementService advertisementService;

	private final ISiteService siteService;

	private final IResourceService resourceService;

	@XComment("{API.DOC.CMS.ADVERTISEMENT.GET_TYPES}")
	@GetMapping("/types")
	public R<?> listAdvertisements() {
		List<Map<String, String>> list = advertisementService.getAdvertisementTypeList().stream()
				.map(t -> Map.of("id", t.getId(), "name", I18nUtils.get(t.getName()))).toList();
		return this.bindDataTable(list);
	}

	@XComment("{API.DOC.CMS.ADVERTISEMENT.GET_LIST}")
	@GetMapping("/list")
	public R<TableData<CmsAdvertisement>> listAdvertisements(
			@RequestParam(name = "adSpaceId") @Min(1) @XComment("{API.DOC.CMS.ADVERTISEMENT.AD_SPACE_ID}") Long adSpaceId,
			@RequestParam(name = "name", required = false) @XComment("{API.DOC.CMS.ADVERTISEMENT.NAME}") String name,
			@RequestParam(name = "state", required = false) @XComment("{API.DOC.CMS.ADVERTISEMENT.STATE}") Integer state) {
		PageRequest pr = getPageRequest();
		Page<CmsAdvertisement> page = this.advertisementService.lambdaQuery()
				.eq(CmsAdvertisement::getAdSpaceId, adSpaceId)
				.like(StringUtils.isNotEmpty(name), CmsAdvertisement::getName, name)
				.eq(state != null && state > -1, CmsAdvertisement::getState, state)
				.orderByDesc(CmsAdvertisement::getCreateTime).page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		page.getRecords().forEach(adv -> {
			IAdvertisementType advertisementType = this.advertisementService.getAdvertisementType(adv.getType());
			if (Objects.nonNull(advertisementType)) {
				adv.setTypeName(I18nUtils.get(advertisementType.getName()));
			}
		});
		return this.bindDataTable(page.getRecords(), (int) page.getTotal());
	}

	@XComment("{API.DOC.CMS.ADVERTISEMENT.GET_INFO}")
	@GetMapping("/detail/{advertisementId}")
	public R<AdvertisementVO> getAdvertisementInfo(
			@PathVariable("advertisementId") @Min(1) @XComment("{CMS.AD.ID}") Long advertisementId) {
		CmsAdvertisement ad = this.advertisementService.getById(advertisementId);
		Assert.notNull(ad, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("advertisementId", advertisementId));

		CmsSite site = siteService.getSite(ad.getSiteId());
		AdvertisementVO vo = new AdvertisementVO(ad).dealPreviewResourcePath();
		resourceService.dealDefaultThumbnail(site, vo.getResourcePath(), vo::setResourceSrc);
		return R.ok(vo);
	}

	@XComment("{API.DOC.CMS.ADVERTISEMENT.CREATE}")
	@Log(title = "新增广告", businessType = BusinessType.INSERT)
	@PostMapping("/add")
	public R<Void> addAdvertisement(@RequestBody AdvertisementDTO dto) throws IOException {
		this.advertisementService.addAdvertisement(dto);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.ADVERTISEMENT.UPDATE}")
	@Log(title = "编辑广告", businessType = BusinessType.UPDATE)
	@PostMapping("/update")
	public R<Void> editAdvertisement(@RequestBody AdvertisementDTO dto) throws IOException {
		this.advertisementService.saveAdvertisement(dto);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.ADVERTISEMENT.DELETE}")
	@Log(title = "删除广告", businessType = BusinessType.DELETE)
	@PostMapping("/delete")
	public R<Void> deleteAdvertisements(
			@RequestBody @XComment("{API.DOC.CMS.ADVERTISEMENT.IDS}") List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.deleteAdvertisement(advertisementIds);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.ADVERTISEMENT.ENABLE}")
	@Log(title = "启用广告", businessType = BusinessType.UPDATE)
	@PostMapping("/enable")
	public R<Void> enableAdvertisements(
			@RequestBody @XComment("{API.DOC.CMS.ADVERTISEMENT.IDS}") List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.enableAdvertisement(advertisementIds,
				StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	@XComment("{API.DOC.CMS.ADVERTISEMENT.DISABLE}")
	@Log(title = "禁用广告", businessType = BusinessType.UPDATE)
	@PostMapping("/disable")
	public R<Void> disableAdvertisements(
			@RequestBody @XComment("{API.DOC.CMS.ADVERTISEMENT.IDS}") List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.disableAdvertisement(advertisementIds,
				StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}
}
