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
