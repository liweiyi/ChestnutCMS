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
package com.chestnut.article.template.tag;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.mapper.CmsArticleDetailMapper;
import com.chestnut.common.annotation.XComment;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.StaticizeConstants;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.exception.DuplicatePageFlagException;
import com.chestnut.common.staticize.exception.PageIndexOutOfBoundsException;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CmsArticleTag extends AbstractTag {

	public static final String TAG_NAME = "cms_article";
	public final static String NAME = "{FREEMARKER.TAG." + TAG_NAME + ".NAME}";
	public final static String DESC = "{FREEMARKER.TAG." + TAG_NAME + ".DESC}";
	public final static String ATTR_USAGE_CONTENT_ID = "{FREEMARKER.TAG." + TAG_NAME + ".contentId}";
	public final static String ATTR_USAGE_PAGE = "{FREEMARKER.TAG." + TAG_NAME + ".page}";

	public static final String ATTR_CONTENT_ID = "contentId";
	public static final String ATTR_PAGE = "page";

	public static final String TemplateVariable_ArticleContent = "ArticleContent";

//	private static final String PAGE_BREAK_SPLITER = "<div[^>]+class=['\"]page-break['\"].*?</div>";
	private static final String PAGE_BREAK_SPLITER = "__XY_UEDITOR_PAGE_BREAK__";

	private final CmsContentMapper contentMapper;

	private final CmsArticleDetailMapper articleMapper;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(ATTR_CONTENT_ID, true, TagAttrDataType.INTEGER, ATTR_USAGE_CONTENT_ID));
		tagAttrs.add(new TagAttr(ATTR_PAGE, false, TagAttrDataType.BOOLEAN, ATTR_USAGE_PAGE, Boolean.FALSE.toString()));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException {
		String contentHtml;
		long contentId = MapUtils.getLongValue(attrs, ATTR_CONTENT_ID, 0);
		if (contentId <= 0) {
			throw new TemplateException("Invalid contentId: " + contentId, env);
		}
		CmsContent content = this.contentMapper.selectById(contentId);
		if (content.isLinkContent()) {
			return Map.of(TemplateVariable_ArticleContent, this.wrap(env, StringUtils.EMPTY));
		}
		if (ContentCopyType.isMapping(content.getCopyType())) {
			contentId = content.getCopyId();
		}
		CmsArticleDetail articleDetail = this.articleMapper.selectById(contentId);
		if (Objects.isNull(articleDetail)) {
			throw new TemplateException("Article details not found: " + contentId, env);
		}
		contentHtml = articleDetail.getContentHtml();
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		boolean page = MapUtils.getBooleanValue(attrs, ATTR_PAGE, false);
		if (page) {
			if (context.isPaged()) {
				throw new DuplicatePageFlagException(env);
			}
			context.setPaged(true);

			String[] pageContents = contentHtml.split(PAGE_BREAK_SPLITER);
			if (context.getPageIndex() > pageContents.length) {
				throw new PageIndexOutOfBoundsException(context.getPageIndex(), pageContents.length, env);
			}
			context.setPageTotal(pageContents.length);
			env.setGlobalVariable(StaticizeConstants.TemplateVariable_PageTotal,
					this.wrap(env, context.getPageTotal()));
			contentHtml = pageContents[context.getPageIndex() - 1];
		}
		ArticleTagData data = new ArticleTagData(articleDetail.getFormat(), contentHtml);
		return Map.of(
				TemplateVariable_ArticleContent, this.wrap(env, contentHtml), // 兼容历史版本保留ArticleContent
				StaticizeConstants.TemplateVariable_Data, this.wrap(env, data)
		);
	}

	@Override
	public Class<ArticleTagData> getDataClass() {
		return ArticleTagData.class;
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

	@Getter
	@Setter
	@NoArgsConstructor
	public static class ArticleTagData {

		@XComment("文章正文格式")
		private String Format;

		@XComment("文章正文")
		private String ArticleContent;

		public ArticleTagData(String format, String articleContent) {
			this.Format = format;
			this.ArticleContent = articleContent;
		}
	}
}
