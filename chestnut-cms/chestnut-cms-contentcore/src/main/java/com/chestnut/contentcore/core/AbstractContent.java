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
package com.chestnut.contentcore.core;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.security.domain.LoginUser;
import com.chestnut.common.utils.*;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.exception.ContentCoreErrorCode;
import com.chestnut.contentcore.fixed.dict.ContentOpType;
import com.chestnut.contentcore.fixed.dict.ContentStatus;
import com.chestnut.contentcore.listener.event.*;
import com.chestnut.contentcore.perms.CatalogPermissionType;
import com.chestnut.contentcore.properties.PublishedContentEditProperty;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.IPublishService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.util.CatalogUtils;
import com.chestnut.contentcore.util.ContentCoreUtils;
import com.chestnut.contentcore.util.ContentLogUtils;
import com.chestnut.contentcore.util.InternalUrlUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import com.chestnut.system.permission.PermissionUtils;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractContent<T> implements IContent<T> {

	private CmsContent content;

	@Setter
	private T extendEntity;

	private CmsSite site;

	private CmsCatalog catalog;

	private ISiteService siteService;

	private ICatalogService catalogService;

	private IContentService contentService;

	private IPublishService publishService;

	@Setter
	private Map<String, Object> params;

	private LoginUser operator;

	@Override
	public CmsSite getSite() {
		if (this.site == null) {
			this.site = this.getSiteService().getSite(this.getSiteId());
		}
		return this.site;
	}

	@Override
	public CmsCatalog getCatalog() {
		if (this.catalog == null) {
			this.catalog = this.getCatalogService().getCatalog(this.getCatalogId());
		}
		return this.catalog;
	}

	@Override
	public Long getSiteId() {
		return this.getContentEntity().getSiteId();
	}

	@Override
	public Long getCatalogId() {
		return this.getContentEntity().getCatalogId();
	}

	@Override
	public String getContentType() {
		return this.getContentEntity().getContentType();
	}

	@Override
	public CmsContent getContentEntity() {
		return content;
	}

	@Override
	public void setContentEntity(CmsContent content) {
		this.content = content;
	}

	@Override
	public T getExtendEntity() {
		return this.extendEntity;
	}

	@Override
	public LoginUser getOperator() {
		return this.operator;
	}

	@Override
	public void setOperator(LoginUser operator) {
		this.operator = operator;
	}

	@Override
	public boolean hasExtendEntity() {
		return !this.getContentEntity().isLinkContent()
				&& !ContentCopyType.isMapping(this.getContentEntity().getCopyType());
	}

	@Override
	public Long add() {
		CmsCatalog catalog = this.getCatalogService().getById(this.getCatalogId());
		if (catalog == null) {
			throw CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", this.getCatalog());
		}
		if (this.getContentService().checkSameTitle(catalog.getSiteId(), catalog.getCatalogId(),
				this.getContentEntity().getContentId(), this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		checkRedirectUrl();

		SpringUtils.publishEvent(new BeforeContentSaveEvent(this, this, true));
		content.setSiteId(catalog.getSiteId());
		content.setCatalogAncestors(catalog.getAncestors());
		content.setTopCatalog(CatalogUtils.getTopCatalog(catalog));
		content.setDeptId(Objects.requireNonNullElse(this.getOperator().getDeptId(), 0L));
		content.setContentType(this.getContentType());

		content.setStatus(ContentStatus.DRAFT);
		content.setSortFlag(SortUtils.getDefaultSortValue());
		content.setIsLock(YesOrNo.NO);
		if (StringUtils.isEmpty(content.getLinkFlag())) {
			content.setLinkFlag(YesOrNo.NO);
		}
		if (Objects.isNull(content.getCopyType())) {
			content.setCopyType(ContentCopyType.NONE);
		}
		content.createBy(this.getOperatorUName());
		this.add0();
		this.getContentService().dao().save(this.getContentEntity());
		// 栏目内容数+1
		this.getCatalogService().changeContentCount(catalog.getCatalogId(), 1);
		ContentLogUtils.addLog(ContentOpType.ADD, this.getContentEntity(), this.getOperator());
		SpringUtils.publishEvent(new AfterContentSaveEvent(this, this, true));
		return this.getContentEntity().getContentId();
	}

	protected abstract void add0();

	void checkLock() {
		boolean lockContent = content.isLock() && StringUtils.isNotEmpty(content.getLockUser())
				&& !content.getLockUser().equals(this.getOperatorUName());
		Assert.isFalse(lockContent, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getLockUser()));
	}

	void checkRedirectUrl() {
		if (content.isLinkContent()) {
			// 校验redirectUrl是否是内部链接且非链接数据
			InternalURL internalURL = InternalUrlUtils.parseInternalUrl(content.getRedirectUrl());
			if (Objects.nonNull(internalURL)) {
				IInternalDataType idt = ContentCoreUtils.getInternalDataType(internalURL.getType());
				Assert.isFalse(idt.isLinkData(internalURL.getId()),
						ContentCoreErrorCode.DENY_LINK_TO_LINK_INTERNAL_DATA::exception);
			}
		}
	}

	@Override
	public Long save() {
		checkLock();
		if (this.getContentService().checkSameTitle(this.getContentEntity().getSiteId(),
				this.getContentEntity().getCatalogId(), this.getContentEntity().getContentId(),
				this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		if (ContentStatus.isPublished(content.getStatus())) {
			boolean editPublishedContent = PublishedContentEditProperty.getValue(this.getSite().getConfigProps());
			if (!editPublishedContent) {
				throw ContentCoreErrorCode.CANNOT_EDIT_PUBLISHED_CONTENT.exception();
			}
		}
		checkRedirectUrl();
		SpringUtils.publishEvent(new BeforeContentSaveEvent(this, this, false));
		if (ContentStatus.isToPublishOrPublished(content.getStatus())) {
			content.setStatus(ContentStatus.EDITING);
		}
		content.updateBy(this.getOperatorUName());
		this.save0();
		contentService.dao().updateById(this.getContentEntity());
		ContentLogUtils.addLog(ContentOpType.UPDATE, this.getContentEntity(), this.getOperator());
		SpringUtils.publishEvent(new AfterContentSaveEvent(this, this, false));
		return this.getContentEntity().getContentId();
	}

	protected abstract void save0();

	public boolean isDeleteByCatalog() {
		return MapUtils.getBoolean(this.getParams(), PARAM_IS_DELETE_BY_CATALOG, false);
	}

	@Override
	public void delete() {
		this.checkLock();
		// 删除到备份表
		this.getContentService().dao().deleteByIdAndBackup(this.getContentEntity(), getOperatorUName());
		this.delete0();
		// 直接删除站内映射内容
		this.getContentService().dao().remove(new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getCopyType, ContentCopyType.Mapping)
				.eq(CmsContent::getCopyId, this.getContentEntity().getContentId()));
		if (!isDeleteByCatalog()) {
			// 栏目内容数-1, 删除栏目时，不需要更新栏目的内容数量
			this.getCatalogService().changeContentCount(getCatalogId(), -1);
		}
		ContentLogUtils.addLog(ContentOpType.DELETE, this.getContentEntity(), this.getOperator());

		SpringUtils.publishEvent(new AfterContentDeleteEvent(this, this));
	}

	protected abstract void delete0();

	@Override
	public boolean publish() {
		checkLock();
		boolean update = false;
		if (content.getPublishDate() == null) {
			content.setPublishDate(LocalDateTime.now());
			update = true;
		} else if (content.getPublishDate().isAfter(LocalDateTime.now())
				&& ContentStatus.isToPublish(content.getStatus())) {
			return false; // 待发布内容并且指定了发布时间的未到发布时间直接跳过
		}
		if (!ContentStatus.isPublished(content.getStatus())) {
			content.setStatus(ContentStatus.PUBLISHED);
			update = true;
		}
		if (update) {
			this.getContentService().dao().updateById(content);
			ContentLogUtils.addLog(ContentOpType.PUBLISH, this.getContentEntity(), this.getOperator());
		}
		// 静态化
		this.getPublishService().asyncPublishContent(this);
		return true;
	}

	@Override
	public CmsContent copyTo(CmsCatalog toCatalog, Integer copyType) {
		checkLock();
		if (this.getContentService().checkSameTitle(toCatalog.getSiteId(), toCatalog.getCatalogId(), null,
				this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		// 校验权限
		PermissionUtils.checkPermission(CatalogPermissionType.CatalogPrivItem.AddContent.getPermissionKey(toCatalog.getCatalogId()), this.getOperator());

		CmsContent newContent = new CmsContent();
		BeanUtils.copyProperties(this.getContentEntity(), newContent, "contentId", "template", "staticPath", "topFlag",
				"topDate", "isLock", "lockUser");
		newContent.setContentId(IdUtils.getSnowflakeId());
		newContent.setCatalogId(toCatalog.getCatalogId());
		newContent.setCatalogAncestors(toCatalog.getAncestors());
		newContent.setTopCatalog(CatalogUtils.getTopCatalog(toCatalog));
		newContent.setDeptCode(toCatalog.getDeptCode());
		newContent.setSortFlag(SortUtils.getDefaultSortValue());
		newContent.createBy(this.getOperatorUName());
		newContent.setCopyType(copyType);
		if (content.getCopyId() > 0 && ContentCopyType.isMapping(content.getCopyType())) {
			newContent.setCopyId(content.getCopyId()); // 映射内容溯源
		} else {
			newContent.setCopyId(content.getContentId());
		}
		if (ContentCopyType.isIndependency(copyType)) {
			newContent.setStatus(ContentStatus.DRAFT);
			newContent.setPublishDate(null);
			newContent.setOfflineDate(null);
		}
		this.getContentService().dao().save(newContent);
		copyTo0(newContent, copyType);
		// 栏目内容数+1
		this.getCatalogService().changeContentCount(toCatalog.getCatalogId(), 1);

		SpringUtils.publishEvent(new AfterContentCopyEvent(this, this.getContentEntity(), newContent));
		return newContent;
	}

	protected abstract void copyTo0(CmsContent newContent, Integer copyType);

	@Override
	public void moveTo(CmsCatalog toCatalog) {
		checkLock();
		if (this.getContentService().checkSameTitle(toCatalog.getSiteId(), toCatalog.getCatalogId(), null,
				this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		// 校验权限
		PermissionUtils.checkPermission(CatalogPermissionType.CatalogPrivItem.AddContent.getPermissionKey(toCatalog.getCatalogId()), this.getOperator());

		CmsCatalog fromCatalog = this.getCatalogService().getCatalog(content.getCatalogId());
		// 重置内容信息
		content.setSiteId(toCatalog.getSiteId());
		content.setCatalogId(toCatalog.getCatalogId());
		content.setCatalogAncestors(toCatalog.getAncestors());
		content.setTopCatalog(CatalogUtils.getTopCatalog(toCatalog));
		content.setDeptCode(toCatalog.getDeptCode());
		content.setSortFlag(SortUtils.getDefaultSortValue());
		content.setTopFlag(0L);
		content.setPublishPipe(null);
		content.setPublishPipeProps(null);
		// 重置发布状态
		content.setStatus(ContentStatus.DRAFT);
		content.updateBy(this.getOperatorUName());
		this.getContentService().dao().updateById(content);
		// 目标栏目内容数量+1
		this.getCatalogService().changeContentCount(toCatalog.getCatalogId(), 1);
		// 源栏目内容数量-1
		this.getCatalogService().changeContentCount(fromCatalog.getCatalogId(), -1);
	}

	@Override
	public void setTop(LocalDateTime topEndTime) {
		content.setTopFlag(Instant.now().toEpochMilli());
		content.setTopDate(topEndTime);
		content.updateBy(this.getOperatorUName());
		this.getContentService().dao().updateById(content);
		// 重新发布内容
		if (ContentStatus.isPublished(this.getContentEntity().getStatus())) {
			this.getPublishService().publishContent(List.of(content.getContentId()), getOperator());
		}
		ContentLogUtils.addLog(ContentOpType.TOP, this.getContentEntity(), this.getOperator());
	}

	@Override
	public void cancelTop() {
		if (content.getTopFlag() <= 0L) {
			return;
		}
		content.setTopFlag(0L);
		content.setTopDate(null);
		this.getContentService().dao().updateById(content);
		// 重新发布内容
		if (ContentStatus.isPublished(this.getContentEntity().getStatus())) {
			this.getPublishService().publishContent(List.of(content.getContentId()), getOperator());
		}
		ContentLogUtils.addLog(ContentOpType.CANCEL_TOP, this.getContentEntity(), this.getOperator());
	}

	@Override
	public void sort(Long targetContentId) {
		if (targetContentId.equals(this.getContentEntity().getContentId())) {
			return;
		}
		checkLock();
		CmsContent next = this.getContentService().dao().getById(targetContentId);
		if (next.getTopFlag() > 0 && this.getContentEntity().getTopFlag() == 0) {
			this.content.setTopFlag(next.getTopFlag() + 1); // 非置顶内容排到置顶内容前需要置顶
		} else if (this.getContentEntity().getTopFlag() > 0 && next.getTopFlag() == 0) {
			this.content.setTopFlag(0L); // 置顶内容排到非置顶内容前取消置顶
			this.content.setTopDate(null);
		}
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getCatalogId, next.getCatalogId()).gt(CmsContent::getSortFlag, next.getSortFlag())
				.orderByAsc(CmsContent::getSortFlag).last("limit 1");
		CmsContent prev = this.getContentService().dao().getOne(q);
		if (prev == null) {
			this.content.setSortFlag(SortUtils.getDefaultSortValue());
		} else {
			this.content.setSortFlag((next.getSortFlag() + prev.getSortFlag()) / 2);
		}
		this.getContentEntity().updateBy(this.getOperatorUName());
		this.getContentService().dao().updateById(content);
		ContentLogUtils.addLog(ContentOpType.SORT, this.getContentEntity(), this.getOperator());
	}

	@Override
	public void offline() {
		String status = this.getContentEntity().getStatus();
		if (!ContentStatus.isOffline(status)) {
			this.getContentEntity().setStatus(ContentStatus.OFFLINE);
			this.getContentEntity().updateBy(this.getOperatorUName());
			this.getContentService().dao().updateById(this.getContentEntity());
		}
		if (ContentStatus.isPublished(status)) {
			// 已发布内容删除静态页面
			this.getContentService().deleteStaticFiles(this.getContentEntity());
			// 重新发布内容所在栏目和父级栏目
			String[] catalogIds = this.getContentEntity().getCatalogAncestors()
					.split(CatalogUtils.ANCESTORS_SPLITER);
			for (String catalogId : catalogIds) {
				this.getPublishService().publishCatalog(this.getCatalogService().getCatalog(Long.valueOf(catalogId)),
						false, false, null, this.getOperator());
			}
		}
		ContentLogUtils.addLog(ContentOpType.OFFLINE, this.getContentEntity(), this.getOperator());
		SpringUtils.publishEvent(new AfterContentOfflineEvent(this, this));
	}

	@Override
	public void toPublish() {
		if (!ContentStatus.isToPublish(this.getContentEntity().getStatus())) {
			this.getContentEntity().setStatus(ContentStatus.TO_PUBLISHED);
			this.getContentEntity().updateBy(this.getOperatorUName());
			this.getContentService().dao().updateById(this.getContentEntity());
		}
		ContentLogUtils.addLog(ContentOpType.TO_PUBLISH, this.getContentEntity(), this.getOperator());
		SpringUtils.publishEvent(new AfterContentToPublishEvent(this, this));
	}

	@Override
	public void archive() {
		// TODO 归档
	}

	@Override
	public String getFullText() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.content.getTitle());
		if (StringUtils.isNotEmpty(this.content.getSubTitle())) {
			sb.append(StringUtils.SPACE).append(this.content.getSubTitle());
		}
		if (StringUtils.isNotEmpty(this.content.getShortTitle())) {
			sb.append(StringUtils.SPACE).append(this.content.getShortTitle());
		}
		if (StringUtils.isNotEmpty(this.content.getKeywords())) {
			sb.append(StringUtils.SPACE).append(StringUtils.join(this.content.getKeywords(), StringUtils.SPACE));
		}
		if (StringUtils.isNotEmpty(this.content.getTags())) {
			sb.append(StringUtils.SPACE).append(StringUtils.join(this.content.getTags(), StringUtils.SPACE));
		}
		return sb.toString();
	}

	@Override
	public Map<String, Object> getParams() {
		if (this.params == null) {
			this.params = new HashMap<>();
		}
		return this.params;
	}

	public ISiteService getSiteService() {
		if (this.siteService == null) {
			this.siteService = SpringUtils.getBean(ISiteService.class);
		}
		return this.siteService;
	}

	public ICatalogService getCatalogService() {
		if (this.catalogService == null) {
			this.catalogService = SpringUtils.getBean(ICatalogService.class);
		}
		return this.catalogService;
	}

	public IContentService getContentService() {
		if (this.contentService == null) {
			this.contentService = SpringUtils.getBean(IContentService.class);
		}
		return this.contentService;
	}

	private IPublishService getPublishService() {
		if (this.publishService == null) {
			this.publishService = SpringUtils.getBean(IPublishService.class);
		}
		return this.publishService;
	}
}
