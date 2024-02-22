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
package com.chestnut.article.domain.dto;

import org.springframework.beans.BeanUtils;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.common.utils.StringUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.domain.dto.ContentDTO;
import com.chestnut.contentcore.fixed.dict.ContentAttribute;
import com.chestnut.contentcore.util.InternalUrlUtils;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class ArticleDTO extends ContentDTO {

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

	public static ArticleDTO newInstance(CmsContent content, CmsArticleDetail articleDetail) {
		ArticleDTO dto = new ArticleDTO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
		BeanUtils.copyProperties(articleDetail, dto);
		return dto;
	}
}
