package com.chestnut.cms.word.listener;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.chestnut.article.ArticleContentType;
import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.cms.word.properties.HotWordGroupsProperty;
import com.chestnut.contentcore.core.IContent;
import com.chestnut.contentcore.domain.CmsSite;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.listener.event.BeforeContentSaveEvent;
import com.chestnut.contentcore.service.ISiteService;
import com.chestnut.word.service.IErrorProneWordService;
import com.chestnut.word.service.IHotWordService;
import com.chestnut.word.service.ISensitiveWordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WordEventListener {
	
	private final ISensitiveWordService sensitiveWordService;
	
	private final IHotWordService hotWordService;
	
	private final IErrorProneWordService errorProneWordService;
	
	private final ISiteService siteService;

	@EventListener
	public void afterContentSave(BeforeContentSaveEvent event) {
		IContent<?> content = event.getContent();
		if (ArticleContentType.ID.equals(content.getContentType()) && !content.getContentEntity().isLinkContent()
				&& !ContentCopyType.isMapping(content.getContentEntity().getCopyType())) {
			CmsArticleDetail articleDetail = (CmsArticleDetail) content.getExtendEntity();
			String contentHtml = articleDetail.getContentHtml();
			// 敏感词处理
			contentHtml = sensitiveWordService.replaceSensitiveWords(contentHtml, null);
			// 易错词处理
			contentHtml = errorProneWordService.replaceErrorProneWords(contentHtml);
			// 热词处理
			CmsSite site = siteService.getSite(content.getCatalog().getSiteId());
			String[] groupCodes = HotWordGroupsProperty.getHotWordGroupCodes(content.getCatalog().getConfigProps(), site.getConfigProps());
			if (Objects.nonNull(groupCodes) && groupCodes.length > 0) {
				contentHtml = hotWordService.replaceHotWords(contentHtml, groupCodes, null, null);
			}
			articleDetail.setContentHtml(contentHtml);
		}
	}
}

