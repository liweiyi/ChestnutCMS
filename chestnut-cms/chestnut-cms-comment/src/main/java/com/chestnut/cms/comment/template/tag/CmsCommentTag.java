package com.chestnut.cms.comment.template.tag;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chestnut.cms.comment.CommentConsts;
import com.chestnut.comment.domain.Comment;
import com.chestnut.comment.domain.vo.CommentVO;
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
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final ICommentService commentService;

	private final IContentService contentService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("uid", true, TagAttrDataType.INTEGER, "用户ID"));
		tagAttrs.add(new TagAttr("type", false, TagAttrDataType.STRING, "来源类型", CommentConsts.COMMENT_SOURCE_TYPE));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long uid = MapUtils.getLongValue(attrs, "uid");
		String sourceType = attrs.get("type");

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
		Map<String, CmsContent> contents = this.contentService.listByIds(contentIds)
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
