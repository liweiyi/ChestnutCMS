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
package com.chestnut.article.template.tag;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.mapper.CmsArticleDetailMapper;
import com.chestnut.common.staticize.FreeMarkerUtils;
import com.chestnut.common.staticize.StaticizeConstants;
import com.chestnut.common.staticize.core.TemplateContext;
import com.chestnut.common.staticize.enums.TagAttrDataType;
import com.chestnut.common.staticize.tag.AbstractTag;
import com.chestnut.common.staticize.tag.TagAttr;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.mapper.CmsContentMapper;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CmsArticleTag extends AbstractTag {

	public static final String TAG_NAME = "cms_article";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	public static final String TagAttr_ContentId = "contentId";

	public static final String TagAttr_Page = "page";

	public static final String TemplateVariable_ArticleContent = "ArticleContent";

	// CKEditor5: <div class="page-break" style="page-break-after:always;"><span style="display:none;">&nbsp;</span></div>
//	private static final String PAGE_BREAK_SPLITER = "<div[^>]+class=['\"]page-break['\"].*?</div>";
	private static final String PAGE_BREAK_SPLITER = "__XY_UEDITOR_PAGE_BREAK__";


	private final CmsContentMapper contentMapper;

	private final CmsArticleDetailMapper articleMapper;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_ContentId, true, TagAttrDataType.INTEGER, "文章内容ID"));
		tagAttrs.add(new TagAttr(TagAttr_Page, false, TagAttrDataType.BOOLEAN, "是否分页，默认false"));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		String contentHtml = null;
		long contentId = MapUtils.getLongValue(attrs, TagAttr_ContentId, 0);
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
		boolean page = MapUtils.getBooleanValue(attrs, TagAttr_Page, false);
		if (page) {
			if (context.isPaged()) {
				throw new TemplateException("分页标识已被其他标签激活", env);
			}
			context.setPaged(true);

			String[] pageContents = contentHtml.split(PAGE_BREAK_SPLITER);
			if (context.getPageIndex() > pageContents.length) {
				throw new TemplateException(StringUtils.messageFormat("文章内容分页越界：{0}, 最大页码：{1}。", context.getPageIndex(),
						pageContents.length), env);
			}
			context.setPageTotal(pageContents.length);
			env.setGlobalVariable(StaticizeConstants.TemplateVariable_PageTotal,
					this.wrap(env, context.getPageTotal()));
			contentHtml = pageContents[context.getPageIndex() - 1];
		}
		return Map.of(TemplateVariable_ArticleContent, this.wrap(env, contentHtml));
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
