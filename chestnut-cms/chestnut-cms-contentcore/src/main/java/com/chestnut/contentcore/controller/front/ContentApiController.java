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
package com.chestnut.contentcore.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.common.domain.R;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentApiVO;
import com.chestnut.contentcore.domain.vo.ContentDynamicDataVO;
import com.chestnut.contentcore.domain.vo.ContentVO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 内容数据API接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cms/content")
public class ContentApiController extends BaseRestController {

	private final ContentDynamicDataService contentDynamicDataService;

	/**
	 * 内容动态数据，评论数、点赞数、收藏数、浏览数
	 */
	@GetMapping("/data")
	public R<List<ContentDynamicDataVO>> getContentDynamicData(@RequestParam("ids") String contentIdsStr) {
		if (StringUtils.isEmpty(contentIdsStr)) {
			return R.ok(List.of());
		}
		List<String> contentIds = Stream.of(contentIdsStr.split(",")).toList();
		if (contentIds.isEmpty()) {
			return R.ok(List.of());
		}
		List<ContentDynamicDataVO> data = this.contentDynamicDataService.getContentDynamicDataList(contentIds);
		return R.ok(data);
	}

	private final ICatalogService catalogService;

	private final IContentService contentService;

	@GetMapping("/list")
	public R<List<ContentApiVO>> getContentList(
			@RequestParam("sid") Long siteId,
			@RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
			@RequestParam(value = "lv", required = false, defaultValue = "Root") String level,
			@RequestParam(value = "attrs", required = false) String hasAttributes,
			@RequestParam(value = "no_attrs", required = false) String noAttributes,
			@RequestParam(value = "st", required = false, defaultValue = "Recent") String sortType,
			@RequestParam(value = "ps", required = false, defaultValue = "16") Integer pageSize,
			@RequestParam(value = "pn", required = false, defaultValue = "1") Long pageNumber,
			@RequestParam(value = "pp") String publishPipeCode,
			@RequestParam(value = "preview", required = false, defaultValue = "false") Boolean preview
	) {
		if (!"Root".equalsIgnoreCase(level) && !IdUtils.validate(catalogId)) {
			return R.fail("The parameter cid is required where lv is `Root`.");
		}
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
		q.eq(CmsContent::getSiteId, siteId).eq(CmsContent::getStatus, ContentStatus.PUBLISHED);
		if (!"Root".equalsIgnoreCase(level)) {
			CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
			if (Objects.isNull(catalog)) {
				return R.fail("Catalog not found: " + catalogId);
			}
			if ("Current".equalsIgnoreCase(level)) {
				q.eq(CmsContent::getCatalogId, catalog.getCatalogId());
			} else if ("Child".equalsIgnoreCase(level)) {
				q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
			} else if ("CurrentAndChild".equalsIgnoreCase(level)) {
				q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors());
			}
		}
		if (StringUtils.isNotEmpty(hasAttributes)) {
			int attrTotal = ContentAttribute.convertInt(hasAttributes.split(","));
			q.apply(attrTotal > 0, "attributes&{0}={1}", attrTotal, attrTotal);
		}
		if (StringUtils.isNotEmpty(noAttributes)) {
			String[] contentAttrs = noAttributes.split(",");
			int attrTotal = ContentAttribute.convertInt(contentAttrs);
			for (String attr : contentAttrs) {
				int bit = ContentAttribute.bit(attr);
				q.apply(bit > 0, "attributes&{0}<>{1}", attrTotal, bit);
			}
		}
		if ("Recent".equalsIgnoreCase(sortType)) {
			q.orderByDesc(CmsContent::getPublishDate);
		} else {
			q.orderByDesc(Arrays.asList(CmsContent::getTopFlag, CmsContent::getSortFlag));
		}

		Page<CmsContent> pageResult = this.contentService.dao().page(new Page<>(pageNumber, pageSize, false), q);
		if (pageResult.getRecords().isEmpty()) {
			return R.ok(List.of());
		}
		List<ContentApiVO> list = new ArrayList<>();
		pageResult.getRecords().forEach(c -> {
			ContentApiVO dto = ContentApiVO.newInstance(c);
			CmsCatalog catalog = this.catalogService.getCatalog(c.getCatalogId());
			dto.setCatalogName(catalog.getName());
			dto.setCatalogLink(catalogService.getCatalogLink(catalog, 1, publishPipeCode, preview));
			dto.setLink(this.contentService.getContentLink(c, 1, publishPipeCode, preview));
			dto.setLogoSrc(InternalUrlUtils.getActualUrl(c.getLogo(), publishPipeCode, preview));
			list.add(dto);
		});
		return R.ok(list);
	}

	/**
	 * TODO 按浏览量排序获取数据
	 */
	public R<ContentVO> getHotContentList() {

		return R.ok();
	}
}
