package com.chestnut.cms.stat.domain.vo;

import com.chestnut.contentcore.domain.CmsContent;
import lombok.Getter;
import lombok.Setter;

/**
 * 内容动态统计数据
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ContentDynamicStatDataVO {

    private Long contentId;

    private String title;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 评论数
     */
    private Long commentCount;

    /**
     * 收藏数
     */
    private Long favoriteCount;

    /**
     * 文章浏览数
     */
    private Long viewCount;

    public static ContentDynamicStatDataVO newInstance(CmsContent content) {
        ContentDynamicStatDataVO vo = new ContentDynamicStatDataVO();
        vo.setContentId(content.getContentId());
        vo.setTitle(content.getTitle());
        vo.setViewCount(content.getViewCount());
        vo.setFavoriteCount(content.getFavoriteCount());
        vo.setLikeCount(content.getLikeCount());
        vo.setCommentCount(content.getCommentCount());
        return vo;
    }
}
