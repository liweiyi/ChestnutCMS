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
package com.chestnut.cms.comment.template.tag;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.comment.CommentConsts;
import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.fixed.dict.CommentAuditStatus;
import com.chestnut.comment.service.ICommentService;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractListTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.service.IContentService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CmsCommentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_comment";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_UID = "{FREEMARKER.TAG." + TAG_NAME + ".uid}";
	public final static String ATTR_USAGE_TYPE = "{FREEMARKER.TAG." + TAG_NAME + ".type}";

	public final static String ATTR_UID = "uid";
	public final static String ATTR_TYPE = "type";


	private final ICommentService commentService;

	private final IContentService contentService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(ATTR_UID, true, TagAttrDataType.INTEGER, ATTR_USAGE_UID));
		tagAttrs.add(new TagAttr(ATTR_TYPE, false, TagAttrDataType.STRING, ATTR_USAGE_TYPE, CommentConsts.COMMENT_SOURCE_TYPE));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long uid = MapUtils.getLongValue(attrs, ATTR_UID);
		String sourceType = attrs.get(ATTR_TYPE);

		Page<Comment> pageResult = this.commentService.lambdaQuery()
				.eq(Comment::getSourceType, sourceType)
				.eq(Comment::getUid, uid)
				.eq(Comment::getAuditStatus, CommentAuditStatus.PASSED)
				.orderByDesc(Comment::getCommentId)
				.page(new Page<>(pageIndex, size, page));
		if (pageResult.getRecords().isEmpty()) {
			return TagPageData.of(List.of(), 0);
		}
		List<Long> contentIds = pageResult.getRecords().stream().map(c -> Long.valueOf(c.getSourceId())).toList();
		Map<String, CmsContent> contents = this.contentService.dao().listByIds(contentIds)
				.stream().collect(Collectors.toMap(c -> c.getContentId().toString(), c -> c));
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		pageResult.getRecords().forEach(comment -> {
			CmsContent content = contents.get(comment.getSourceId());
			if (content != null) {
				comment.setSourceTitle(content.getTitle());
				String contentLink = this.contentService.getContentLink(content, 1,
						context.getPublishPipeCode(), context.isPreview());
				comment.setSourceUrl(contentLink);
			}
		});
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
	}

	@Override
	public Class<Comment> getDataClass() {
		return Comment.class;
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESC;
	}
}
