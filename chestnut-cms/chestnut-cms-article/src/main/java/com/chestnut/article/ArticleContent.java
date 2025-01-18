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
package com.chestnut.article;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.article.properties.AutoArticleLogo;
import com.chestnut.article.service.IArticleService;
import com.chestnut.common.async.AsyncTaskManager;
import com.chestnut.common.utils.HtmlUtils;
import com.chestnut.common.utils.SpringUtils;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.core.AbstractContent;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.enums.ContentCopyType;
import com.chestnut.contentcore.service.IResourceService;
import com.chestnut.contentcore.util.ResourceUtils;
import com.chestnut.system.fixed.dict.YesOrNo;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

public class ArticleContent extends AbstractContent<CmsArticleDetail> {

	private IArticleService articleService;

	private IResourceService resourceService;

	@Override
	protected void add0() {
		if (!this.hasExtendEntity()) {
			return;
		}
		CmsArticleDetail articleDetail = this.getExtendEntity();
		articleDetail.setContentId(this.getContentEntity().getContentId());
		articleDetail.setSiteId(this.getContentEntity().getSiteId());
		// 处理内部链接
		String contentHtml = ResourceUtils.dealHtmlInternalUrl(articleDetail.getContentHtml());
		// 处理文章正文远程图片
		if (YesOrNo.isYes(articleDetail.getDownloadRemoteImage())) {
			AsyncTaskManager.setTaskPercent(90);
			contentHtml = this.getResourceService().downloadRemoteImages(contentHtml, this.getSite(),
					this.getOperatorUName());
		}
		articleDetail.setContentHtml(contentHtml);
		// 正文首图作为logo
		if (StringUtils.isEmpty(this.getContentEntity().getImages())
				&& AutoArticleLogo.getValue(this.getSite().getConfigProps())) {
			String firstImage = this.getFirstImage(articleDetail.getContentHtml());
			if (Objects.nonNull(firstImage)) {
				this.getContentEntity().setImages(List.of(firstImage));
			}
		}
		this.getArticleService().dao().save(articleDetail);
	}

	@Override
	protected void save0() {
		// 非映射内容或标题内容修改文章详情
		if (!this.hasExtendEntity()) {
			this.getArticleService().dao().removeById(this.getContentEntity().getContentId());
			return;
		}
		CmsArticleDetail articleDetail = this.getExtendEntity();
		// 处理内部链接
		String contentHtml = ResourceUtils.dealHtmlInternalUrl(articleDetail.getContentHtml());
		// 处理文章正文远程图片
		if (YesOrNo.isYes(articleDetail.getDownloadRemoteImage())) {
			AsyncTaskManager.setTaskPercent(90);
			contentHtml = this.getResourceService().downloadRemoteImages(contentHtml, this.getSite(),
					this.getOperatorUName());
		}
		articleDetail.setContentHtml(contentHtml);
		// 正文首图作为logo
		if (StringUtils.isEmpty(this.getContentEntity().getImages())
				&& AutoArticleLogo.getValue(this.getSite().getConfigProps())) {
			String firstImage = this.getFirstImage(articleDetail.getContentHtml());
			if (Objects.nonNull(firstImage)) {
				this.getContentEntity().setImages(List.of(firstImage));
			}
		}
		this.getArticleService().dao().saveOrUpdate(articleDetail);
	}

	/**
	 * 获取文章正文第一张图片地址
	 */
	private String getFirstImage(String contentHtml) {
		if (StringUtils.isEmpty(contentHtml)) {
			return contentHtml;
		}
		Matcher matcher = ResourceUtils.ImgHtmlTagPattern.matcher(contentHtml);
		if (matcher.find()) {
			String imgSrc = matcher.group(1);
			if (StringUtils.isNotEmpty(imgSrc)) {
				return imgSrc;
			}
		}
		return null;
	}

	@Override
	protected void delete0() {
		if (this.hasExtendEntity()) {
			this.getArticleService().dao()
					.deleteByIdAndBackup(this.getExtendEntity(), this.getOperatorUName());
		}
	}

	@Override
	public void copyTo0(CmsContent newContent, Integer copyType) {
		if (this.hasExtendEntity() && ContentCopyType.isIndependency(copyType)) {
			CmsArticleDetail newArticleDetail = new CmsArticleDetail();
			BeanUtils.copyProperties(this.getExtendEntity(), newArticleDetail, "contentId");
			newArticleDetail.setContentId(newContent.getContentId());
			this.getArticleService().dao().save(newArticleDetail);
		}
    }

	@Override
	public String getFullText() {
		if (this.getContentEntity().isLinkContent()) {
			return super.getFullText();
		}
		return super.getFullText() + StringUtils.SPACE + HtmlUtils.clean(this.getExtendEntity().getContentHtml());
	}

	public IArticleService getArticleService() {
		if (Objects.isNull(this.articleService)) {
			this.articleService = SpringUtils.getBean(IArticleService.class);
		}
		return this.articleService;
	}

	public IResourceService getResourceService() {
		if (Objects.isNull(this.resourceService)) {
			this.resourceService = SpringUtils.getBean(IResourceService.class);
		}
		return this.resourceService;
	}
}
