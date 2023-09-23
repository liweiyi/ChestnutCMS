package com.chestnut.cms.member.domain.vo;

import com.chestnut.contentcore.domain.vo.ContentDynamicDataVO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@NoArgsConstructor
public class ContentDynamicDataWithContributorVO extends ContentDynamicDataVO {

    private ContributorVO contributor;

    public ContentDynamicDataWithContributorVO(ContentDynamicDataVO vo) {
        this.setContentId(vo.getContentId());
        this.setContributorId(vo.getContributorId());
        this.setLikes(vo.getLikes());
        this.setComments(vo.getComments());
        this.setViews(vo.getViews());
        this.setFavorites(vo.getFavorites());
    }
}
