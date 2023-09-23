package com.chestnut.contentcore.domain.vo;

import com.chestnut.contentcore.domain.CmsContent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ContentDynamicDataVO {

    private Long contentId;

    private Long favorites;

    private Long likes;

    private Long comments;

    private Long views;

    private Long contributorId;

    private Boolean updated;

    public ContentDynamicDataVO(CmsContent content) {
        this.contentId = content.getContentId();
        this.favorites = content.getFavoriteCount();
        this.likes = content.getLikeCount();
        this.comments = content.getCommentCount();
        this.views = content.getViewCount();
        this.contributorId = content.getContributorId();
    }

    public void increase(DynamicDataType type) {
        switch(type) {
            case Favorite -> this.favorites++;
            case Like -> this.likes++;
            case Comment -> this.comments++;
            case View -> this.views++;
        }
        this.updated = true;
    }

    public void decrease(DynamicDataType type) {
        switch(type) {
            case Favorite -> this.favorites--;
            case Like -> this.likes--;
            case Comment -> this.comments--;
            case View -> this.views--;
        }
        this.updated = true;
    }

    public enum DynamicDataType {
        Favorite, Like, Comment, View
    }
}