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
package com.chestnut.cms.member.listener;

import com.chestnut.cms.member.CmsMemberConstants;
import com.chestnut.cms.member.impl.ContributeMemberStatData;
import com.chestnut.common.exception.CommonErrorCode;
import com.chestnut.common.utils.Assert;
import com.chestnut.common.utils.IdUtils;
import com.chestnut.contentcore.domain.CmsContent;
import com.chestnut.contentcore.listener.event.AfterContentDeleteEvent;
import com.chestnut.contentcore.listener.event.AfterContentSaveEvent;
import com.chestnut.contentcore.service.IContentService;
import com.chestnut.contentcore.service.impl.ContentDynamicDataService;
import com.chestnut.member.listener.event.*;
import com.chestnut.member.service.IMemberStatDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberListener {

    private final IContentService contentService;

    private final ContentDynamicDataService contentDynamicDataService;

    private final IMemberStatDataService memberStatDataService;

    @EventListener
    public void afterContentSaveEvent(AfterContentSaveEvent event) {
        Long contributorId = event.getContent().getContentEntity().getContributorId();
        if (event.isAdd() && IdUtils.validate(contributorId)) {
            // 更新会员文章数量
            memberStatDataService.changeMemberStatData(contributorId, ContributeMemberStatData.TYPE, 1);
        }
    }

    @EventListener
    public void afterContentDeleteEvent(AfterContentDeleteEvent event) {
        Long contributorId = event.getContent().getContentEntity().getContributorId();
        if (IdUtils.validate(contributorId)) {
            // 更新会员文章数量
            memberStatDataService.changeMemberStatData(contributorId, ContributeMemberStatData.TYPE, -1);
        }
    }

    @EventListener
    public void beforeMemberFavorite(BeforeMemberFavoriteEvent event) {
        if (CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE.equals(event.getDataType())) {
            CmsContent content = this.contentService.getById(event.getDataId());
            Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dataId", event.getDataId()));
        }
    }

    @EventListener
    public void afterMemberFavorite(AfterMemberFavoriteEvent event) {
        if (CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE.equals(event.getMemberFavorites().getDataType())) {
            // 内容收藏数+1
            this.contentDynamicDataService.increaseFavoriteCount(event.getMemberFavorites().getDataId());
        }
    }

    @EventListener
    public void afterCancelMemberFavorite(AfterMemberCancelFavoriteEvent event) {
        if (CmsMemberConstants.MEMBER_FAVORITES_DATA_TYPE.equals(event.getMemberFavorites().getDataType())) {
            // 内容收藏数-1
            this.contentDynamicDataService.decreaseFavoriteCount(event.getMemberFavorites().getDataId());
        }
    }

    @EventListener
    public void beforeMemberLike(BeforeMemberLikeEvent event) {
        if (CmsMemberConstants.MEMBER_LIKE_DATA_TYPE.equals(event.getDataType())) {
            CmsContent content = this.contentService.getById(event.getDataId());
            Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dataId", event.getDataId()));
        }
    }

    @EventListener
    public void afterMemberLike(AfterMemberLikeEvent event) {
        if (CmsMemberConstants.MEMBER_LIKE_DATA_TYPE.equals(event.getMemberLike().getDataType())) {
            // 内容点赞数+1
            this.contentDynamicDataService.increaseLikeCount(event.getMemberLike().getDataId());
        }
    }

    @EventListener
    public void afterCancelMemberFavorite(AfterMemberCancelLikeEvent event) {
        if (CmsMemberConstants.MEMBER_LIKE_DATA_TYPE.equals(event.getMemberLike().getDataType())) {
            // 内容点赞数-1
            this.contentDynamicDataService.decreaseLikeCount(event.getMemberLike().getDataId());
        }
    }
}
