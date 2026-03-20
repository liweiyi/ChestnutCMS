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
package com.chestnut.article.domain.dto;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.common.annotation.XComment;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter 
@Setter
@XComment("{API.DOC.CMS.ARTICLE.DTO}")
public class ArticleDTO extends ContentDTO {

	@XComment("{API.DOC.CMS.ARTICLE.CONTENT_HTML}")
    private String contentHtml;

	@XComment("{API.DOC.CMS.ARTICLE.DOWNLOAD_REMOTE_IMAGE}")
    private String downloadRemoteImage;

	@XComment("{API.DOC.CMS.ARTICLE.PAGE_TITLES}")
    private String pageTitles;

	@XComment("{API.DOC.CMS.ARTICLE.FORMAT}")
	private String format;

	public static ArticleDTO newInstance(CmsContent content, CmsArticleDetail articleDetail, boolean preview) {
		ArticleDTO dto = new ArticleDTO();
		dto.initByContent(content, preview);
		BeanUtils.copyProperties(articleDetail, dto);
		return dto;
	}
}
