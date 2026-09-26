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
package com.chestnut.cms.member.controller.front;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.article.ArticleContentType;
import com.chestnut.article.IArticleBodyFormat;
import com.chestnut.article.format.ArticleBodyFormat_RichText;
import com.chestnut.article.service.IArticleService;
import com.chestnut.cms.member.domain.dto.ArticleContributeDTO;
import com.chestnut.cms.member.domain.vo.MemberContentVO;
import com.chestnut.cms.member.properties.EnableContributeProperty;
import com.chestnut.cms.member.service.MemberContributeService;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.domain.R;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.anno.Priv;
import com.chestnut.common.security.domain.Operator;
import com.chestnut.common.security.web.BaseRestController;
import com.chestnut.common.security.web.TableData;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.HtmlUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.core.IContentType;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsResource;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.domain.dto.ResourceUploadDTO;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.AfterContentDeleteEvent;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.member.security.MemberUserType;
import com.chestnut.member.security.StpMemberUtil;
import com.chestnut.system.annotation.IgnoreDemoMode;
import com.chestnut.system.validator.LongId;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 会员个人中心
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@XComment("{API.DOC.CMS.CMS_MEMBER.MEMBER_CONTRIBUTE_API}")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account/contribute")
public class MemberContributeApiController extends BaseRestController implements ApplicationContextAware {

	private final TransactionTemplate transactionTemplate;

	private final IContentService contentService;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IResourceService resourceService;

	private final IArticleService articleService;

	private final ArticleContentType articleContentType;

	private final MemberContributeService memberContributeService;

	private ApplicationContext applicationContext;


	@XComment("{API.DOC.CMS.CMS_MEMBER.DELETE_CONTRIBUTE}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/delete")
	public R<Void> deleteContribute(@RequestParam("cid") @LongId @XComment("{API.DOC.CMS.CMS_MEMBER.CONTENT_ID}") Long contentId) {
		CmsContent xContent = this.contentService.dao().getById(contentId);
		if (xContent == null) {
			return R.fail("内容不存在");
		}
		if (!ContentStatus.isDraft(xContent.getStatus())) {
			return R.fail("只能删除待审核的初稿");
		}
		Operator operator = Operator.of(StpMemberUtil.getLoginUser());
		if (!Objects.equals(xContent.getContributorId(), operator.getUserId())) {
			return R.fail("内容ID错误");
		}

		IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
		IContent<?> content = contentType.loadContent(xContent);
		content.setOperator(operator);
		transactionTemplate.executeWithoutResult(transactionStatus -> content.delete());
		applicationContext.publishEvent(new AfterContentDeleteEvent(this, content));
		return R.ok();
	}
	@XComment("{API.DOC.CMS.CMS_MEMBER.DELETE_CONTRIBUTE}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
    @DeleteMapping
	@Deprecated(since = "1.5.7", forRemoval = true)
	public R<Void> deleteContribute2(@RequestParam("cid") @LongId @XComment("{API.DOC.CMS.CMS_MEMBER.CONTENT_ID}") Long contentId) {
		return deleteContribute(contentId);
	}

	/**
	 * 投稿
	 */
	@XComment("{API.DOC.CMS.CMS_MEMBER.ARTICLE_CONTRIBUTE}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping
	public R<Void> articleContribute(@RequestBody @Validated ArticleContributeDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));
		if (!EnableContributeProperty.getValue(catalog.getConfigProps())) {
			return R.fail("参数`catalogId`异常：" + dto.getCatalogId());
		}
		String contentHtml = HtmlUtils.cleanRichText(dto.getContentHtml());
		Assert.isTrue(StringUtils.isNotBlank(contentHtml), () -> CommonErrorCode.NOT_EMPTY.exception("contentHtml"));
		dto.setContentHtml(contentHtml);

		Operator operator = Operator.of(StpMemberUtil.getLoginUser());
		IArticleBodyFormat articleBodyFormat = articleService.getArticleBodyFormat(dto.getFormat());
		if (Objects.isNull(articleBodyFormat)) {
			dto.setFormat(ArticleBodyFormat_RichText.ID);
		}

		memberContributeService.contribute(dto, operator);
		return R.ok();
	}

	@XComment("{API.DOC.CMS.CMS_MEMBER.UPLOAD_IMAGE}")
	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/upload_image")
	public R<?> uploadFile(@RequestParam("file") @XComment("{API.DOC.CMS.CMS_MEMBER.FILE}") MultipartFile multipartFile,
						   @RequestParam("sid") @LongId @XComment("{API.DOC.CMS.CMS_MEMBER.SITE_ID}") Long siteId) throws Exception {
		Assert.notNull(multipartFile, () -> CommonErrorCode.NOT_EMPTY.exception("file"));

		CmsSite site = this.siteService.getSite(siteId);
		if (site == null) {
			return R.fail("Invalid parameter sid: "+ siteId);
		}
		ResourceUploadDTO dto = ResourceUploadDTO.builder().site(site).file(multipartFile).build();
		dto.setOperator(StpMemberUtil.getLoginUser());
		CmsResource resource = this.resourceService.addResource(dto);
		return R.ok(Map.of("url", resource.getPath(), "iurl", resource.getInternalUrl()));
	}

	/**
	 * 会员发表的内容数据
	 */
	@XComment("{API.DOC.CMS.CMS_MEMBER.GET_MEMBER_CONTENT_LIST}")
	@GetMapping("/{memberId}")
	public R<TableData<MemberContentVO>> getMemberContentList(
			@PathVariable @LongId @XComment("{API.DOC.CMS.CMS_MEMBER.MEMBER_ID}") Long memberId,
			@RequestParam(required = false, defaultValue = "16") @Min(1) @XComment("{API.DOC.CMS.CMS_MEMBER.LIMIT}") Integer limit,
			@RequestParam(required = false, defaultValue = "1") @Min(0) @XComment("{API.DOC.CMS.CMS_MEMBER.OFFSET}") Long offset) {
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getStatus, ContentStatus.PUBLISHED)
				.eq(CmsContent::getContributorId, memberId)
				.lt(offset > 0, CmsContent::getPublishDate, LocalDateTime.ofInstant(Instant.ofEpochMilli(offset), ZoneId.systemDefault()))
				.orderByDesc(CmsContent::getPublishDate);
		Page<CmsContent> page = contentService.dao().page(new Page<>(1, limit, false), q);
		List<MemberContentVO> list = page.getRecords().stream().map(MemberContentVO::newInstance).toList();
		return this.bindDataTable(list, page.getTotal());
	}

	@Override
	public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
