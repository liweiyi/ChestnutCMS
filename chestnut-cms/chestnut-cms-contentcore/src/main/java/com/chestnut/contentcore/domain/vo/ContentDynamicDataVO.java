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