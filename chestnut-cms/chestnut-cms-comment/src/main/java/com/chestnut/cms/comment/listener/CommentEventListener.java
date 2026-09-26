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
package com.chestnut.cms.comment.listener;

import com.chestnut.cms.comment.CmsCommentErrorCode;
import com.chestnut.cms.comment.properties.EnableComment;
import com.chestnut.cms.comment.properties.EnableCommentAudit;
import com.chestnut.comment.fixed.dict.CommentAuditStatus;
import com.chestnut.comment.listener.event.AfterCommentSubmitEvent;
import com.chestnut.comment.listener.event.BeforeCommentSubmitEvent;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.service.ICatalogService;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentEventListener {

	public static final String COMMENT_SOURCE_TYPE_CONTENT = "cms_content";

	private final ContentDynamicDataService contentDynamicDataService;

	private final IContentService contentService;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	@EventListener
	public void beforeCommentSubmit(BeforeCommentSubmitEvent event) {
		if (!COMMENT_SOURCE_TYPE_CONTENT.equals(event.getComment().getSourceType())) {
			return;
		}
		Optional<CmsContent> opt = this.contentService.dao().lambdaQuery()
				.select(List.of(CmsContent::getSiteId, CmsContent::getCatalogId, CmsContent::getConfigProps))
				.eq(CmsContent::getContentId, event.getComment().getSourceId())
				.oneOpt();
		if (opt.isEmpty()) {
			throw new RuntimeException("Comment content not found: " + event.getComment().getSourceId());
		}

		CmsContent content = opt.get();
		CmsSite site = this.siteService.getSite(content.getSiteId());
		// 先检查站点是否开启评论，如果站点未开启评论直接返回错误
		boolean enableComment = EnableComment.getValue(site.getConfigProps());
		if (!enableComment) {
			throw CmsCommentErrorCode.COMMENT_DISABLED.exception();
		}
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		// 再检查栏目是否开启评论
		enableComment = EnableComment.getValue(catalog.getConfigProps());
		if (!enableComment) {
			throw CmsCommentErrorCode.COMMENT_DISABLED.exception();
		}
		// 最后检查内容是否开启评论
		enableComment = EnableComment.getValue(content.getConfigProps());
		if (!enableComment) {
			throw CmsCommentErrorCode.COMMENT_DISABLED.exception();
		}
		// 是否需要审核判断
		if (!EnableCommentAudit.getValue(site.getConfigProps())) {
			event.getComment().setAuditStatus(CommentAuditStatus.PASSED);
		}
	}

	@EventListener
	public void afterCommentSubmit(AfterCommentSubmitEvent event) {
		if (!COMMENT_SOURCE_TYPE_CONTENT.equals(event.getComment().getSourceType())) {
			return;
		}
		Long contentId = Long.valueOf(event.getComment().getSourceId());
		this.contentDynamicDataService.increaseCommentCount(contentId);
	}
}
