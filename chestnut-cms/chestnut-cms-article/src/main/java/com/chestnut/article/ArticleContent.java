package com.chestnut.article;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.properties.AutoArticleLogo;
import com.chestnut.article.service.IArticleService;
import com.chestnut.article.service.impl.ArticleServiceImpl;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.HtmlUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.AbstractContent;
import com.chestnut.contentcore.domain.CmsCatalog;
import com.chestnut.system.fixed.dict.YesOrNo;
import org.springframework.beans.BeanUtils;

import java.util.regex.Matcher;

public class ArticleContent extends AbstractContent<CmsArticleDetail> {

	private IArticleService articleService;

	@Override
	public Long add() {
		super.add();
		if (!this.hasExtendEntity()) {
			this.getContentService().save(this.getContentEntity());
			return this.getContentEntity().getContentId();
		}
		CmsArticleDetail articleDetail = this.getExtendEntity();
		articleDetail.setContentId(this.getContentEntity().getContentId());
		articleDetail.setSiteId(this.getContentEntity().getSiteId());
		// 处理内部链接
		String contentHtml = this.getArticleService().saveInternalUrl(articleDetail.getContentHtml());
		// 处理文章正文远程图片
		if (YesOrNo.isYes(articleDetail.getDownloadRemoteImage())) {
			AsyncTaskManager.setTaskPercent(90);
			contentHtml = this.getArticleService().downloadRemoteImages(contentHtml, this.getSite(),
					this.getOperator().getUsername());
		}
		articleDetail.setContentHtml(contentHtml);
		// 正文首图作为logo
		if (StringUtils.isEmpty(this.getContentEntity().getLogo())
				&& AutoArticleLogo.getValue(this.getSite().getConfigProps())) {
			this.getContentEntity().setLogo(this.getFirstImage(articleDetail.getContentHtml()));
		}
		this.getContentService().save(this.getContentEntity());
		this.getArticleService().save(articleDetail);
		return this.getContentEntity().getContentId();
	}

	@Override
	public Long save() {
		super.save();
		// 非映射内容或标题内容修改文章详情
		if (!this.hasExtendEntity()) {
			this.getContentService().updateById(this.getContentEntity());
			this.getArticleService().removeById(this.getContentEntity().getContentId());
			return this.getContentEntity().getContentId();
		}
		CmsArticleDetail articleDetail = this.getExtendEntity();
		// 处理内部链接
		String contentHtml = this.getArticleService().saveInternalUrl(articleDetail.getContentHtml());
		// 处理文章正文远程图片
		if (YesOrNo.isYes(articleDetail.getDownloadRemoteImage())) {
			AsyncTaskManager.setTaskPercent(90);
			contentHtml = this.getArticleService().downloadRemoteImages(contentHtml, this.getSite(),
					this.getOperator().getUsername());
		}
		articleDetail.setContentHtml(contentHtml);
		// 正文首图作为logo
		if (StringUtils.isEmpty(this.getContentEntity().getLogo())
				&& AutoArticleLogo.getValue(this.getSite().getConfigProps())) {
			this.getContentEntity().setLogo(this.getFirstImage(articleDetail.getContentHtml()));
		}
		this.getContentService().updateById(this.getContentEntity());
		this.getArticleService().saveOrUpdate(articleDetail);
		return this.getContentEntity().getContentId();
	}

	/**
	 * 获取文章正文第一张图片地址
	 */
	private String getFirstImage(String contentHtml) {
		if (StringUtils.isEmpty(contentHtml)) {
			return contentHtml;
		}
		Matcher matcher = ArticleServiceImpl.ImgHtmlTagPattern.matcher(contentHtml);
		if (matcher.find()) {
			String imgSrc = matcher.group(1);
			if (StringUtils.isNotEmpty(imgSrc)) {
				return imgSrc;
			}
		}
		return null;
	}

	@Override
	public void delete() {
		super.delete();
		if (this.hasExtendEntity()) {
			this.getArticleService().removeById(this.getContentEntity().getContentId());
		}
	}

	@Override
	public void copyTo(CmsCatalog toCatalog, Integer copyType) {
		super.copyTo(toCatalog, copyType);
		if (this.hasExtendEntity()) {
			Long newContentId = (Long) this.getParams().get("NewContentId");
			CmsArticleDetail newArticleDetail = new CmsArticleDetail();
			BeanUtils.copyProperties(this.getExtendEntity(), newArticleDetail, "contentId");
			newArticleDetail.setContentId(newContentId);
			this.getArticleService().save(newArticleDetail);
		}
	}

	@Override
	public String getFullText() {
		return super.getFullText() + StringUtils.SPACE + HtmlUtils.clean(this.getExtendEntity().getContentHtml());
	}

	public IArticleService getArticleService() {
		if (this.articleService == null) {
			this.articleService = SpringUtils.getBean(IArticleService.class);
		}
		return this.articleService;
	}
}
