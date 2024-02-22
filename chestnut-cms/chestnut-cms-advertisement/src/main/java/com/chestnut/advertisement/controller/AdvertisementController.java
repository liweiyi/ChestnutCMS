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
package com.chestnut.advertisement.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.advertisement.IAdvertisementType;
import com.chestnut.advertisement.domain.CmsAdvertisement;
import com.chestnut.advertisement.permission.CmsAdvertisementPriv;
import com.chestnut.advertisement.pojo.dto.AdvertisementDTO;
import com.chestnut.advertisement.pojo.vo.AdvertisementVO;
import com.chestnut.advertisement.service.IAdvertisementService;
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
@Priv(type = AdminUserType.TYPE, value = CmsAdvertisementPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/advertisement")
public class AdvertisementController extends BaseRestController {

	private final IAdvertisementService advertisementService;

	@GetMapping("/types")
	public R<?> listAdvertisements() {
		List<Map<String, String>> list = advertisementService.getAdvertisementTypeList().stream()
				.map(t -> Map.of("id", t.getId(), "name", I18nUtils.get(t.getName()))).toList();
		return this.bindDataTable(list);
	}

	@GetMapping
	public R<?> listAdvertisements(@RequestParam(name = "adSpaceId") @Min(1) Long adSpaceId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "state", required = false) Integer state) {
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

	@GetMapping("/{advertisementId}")
	public R<AdvertisementVO> getAdvertisementInfo(@PathVariable("advertisementId") @Min(1) Long advertisementId) {
		CmsAdvertisement ad = this.advertisementService.getById(advertisementId);
		Assert.notNull(ad, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("advertisementId", advertisementId));
		return R.ok(new AdvertisementVO(ad).dealPreviewResourcePath());
	}

	@Log(title = "新增广告", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addAdvertisement(@RequestBody AdvertisementDTO dto) throws IOException {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.advertisementService.addAdvertisement(dto);
		return R.ok();
	}

	@Log(title = "编辑广告", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editAdvertisement(@RequestBody AdvertisementDTO dto) throws IOException {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.advertisementService.saveAdvertisement(dto);
		return R.ok();
	}

	@Log(title = "删除广告", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteAdvertisements(@RequestBody List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.deleteAdvertisement(advertisementIds);
		return R.ok();
	}

	@Log(title = "启用广告", businessType = BusinessType.UPDATE)
	@PutMapping("/enable")
	public R<?> enableAdvertisements(@RequestBody List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.enableAdvertisement(advertisementIds,
				StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	@Log(title = "禁用广告", businessType = BusinessType.UPDATE)
	@PutMapping("/disable")
	public R<?> disableAdvertisements(@RequestBody List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.disableAdvertisement(advertisementIds,
				StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}
}
