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
package com.chestnut.article.domain.vo;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.vo.ContentVO;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter 
@Setter
public class ArticleVO extends ContentVO {

	/**
	 * 文章正文（html格式）
	 */
    private String contentHtml;

    /**
     * 是否下载远程图片
     */
    private String downloadRemoteImage;

    /**
     * 分页标题
     */
    private String pageTitles;

	/**
	 * 文档格式
	 */
	private String format;

	public static ArticleVO newInstance(CmsContent content, CmsArticleDetail articleDetail) {
		ArticleVO vo = new ArticleVO();
		vo.initByContent(content, true);
		if (Objects.nonNull(articleDetail)) {
			vo.setContentHtml(articleDetail.getContentHtml());
			vo.setDownloadRemoteImage(articleDetail.getDownloadRemoteImage());
			vo.setPageTitles(articleDetail.getPageTitles());
			vo.setFormat(articleDetail.getFormat());
		}
		return vo;
	}
}
