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
package com.chestnut.cms.member.domain.vo;

import com.chestnut.article.domain.CmsArticleDetail;
import com.chestnut.contentcore.domain.CmsContent;
import lombok.Getter;
import lombok.Setter;

/**
 * 投稿文章数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ContributeArticleVO {

    private Long contentId;

    private Long catalogId;

    private String title;

    private String summary;

    private String[] tags;

    private String logo;

    private String logoSrc;

    private String contentHtml;

    public static ContributeArticleVO newInstance(CmsContent content, CmsArticleDetail articleDetail) {
        ContributeArticleVO vo = new ContributeArticleVO();
        vo.setContentId(content.getContentId());
        vo.setCatalogId(content.getCatalogId());
        vo.setTitle(content.getTitle());
        vo.setSummary(content.getSummary());
        vo.setTags(content.getTags());
        vo.setLogo(content.getLogo());
        vo.setContentHtml(articleDetail.getContentHtml());
        return vo;
    }
}
